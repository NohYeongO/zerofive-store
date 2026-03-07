package com.zerofive.store.order.application;

import com.zerofive.store.core.exception.NotFoundException;
import com.zerofive.store.core.exception.PaymentException;
import com.zerofive.store.core.exception.StockException;
import com.zerofive.store.core.port.AddressPort;
import com.zerofive.store.core.port.CouponPort;
import com.zerofive.store.core.port.PaymentPort;
import com.zerofive.store.core.port.ProductStockPort;
import com.zerofive.store.order.application.dto.OrderItemRequest;
import com.zerofive.store.order.application.dto.OrderResult;
import com.zerofive.store.order.application.dto.OrderSessionResult;
import com.zerofive.store.order.application.dto.PaymentRequest;
import com.zerofive.store.order.domain.OrderService;
import com.zerofive.store.order.domain.OrderSessionService;
import com.zerofive.store.order.domain.entity.Order;
import com.zerofive.store.order.domain.entity.OrderSession;
import com.zerofive.store.order.infra.repository.OrderSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderAppService {

    private final OrderSessionService orderSessionService;
    private final OrderSessionRepository orderSessionRepository;
    private final OrderService orderService;
    private final ProductStockPort productStockPort;
    private final CouponPort couponPort;
    private final AddressPort addressPort;
    private final PaymentPort paymentPort;

    public OrderSessionResult createOrder(Long accountId, List<OrderItemRequest> items) {
        List<OrderItemRequest> sortedItems = items.stream()
                .sorted(Comparator.comparing(OrderItemRequest::productId))
                .toList();

        List<ProductStockPort.StockItem> stockItems = sortedItems.stream()
                .map(item -> new ProductStockPort.StockItem(item.productId(), item.quantity()))
                .toList();

        try {
            productStockPort.deductStock(stockItems);
        } catch (StockException e) {
            log.error("재고 차감 실패: errorCode={}, productId={}, message={}",
                    e.getErrorCode(), e.getProductId(), e.getMessage());
            throw e;
        }

        OrderSession session;
        try {
            session = orderSessionService.createSession(accountId, sortedItems);
        } catch (Exception e) {
            log.error("세션 저장 실패, 재고 복구 요청: {}", e.getMessage());
            productStockPort.restoreStock(stockItems);
            throw e;
        }

        List<CouponPort.CouponInfo> coupons = couponPort.getAvailableCoupons(accountId);
        List<AddressPort.AddressInfo> addresses = addressPort.getAddresses(accountId);

        return OrderSessionResult.of(session, coupons, addresses);
    }

    @Transactional
    public void polling(String sessionId) {
        OrderSession session = orderSessionRepository.findBySessionIdWithItems(sessionId)
                .orElseThrow(() -> new NotFoundException("세션을 찾을 수 없습니다. sessionId: " + sessionId));

        session.updatePolledAt();
    }

    @Transactional
    public void cancelOrder(String sessionId) {
        OrderSession session = orderSessionRepository.findBySessionIdWithItems(sessionId)
                .orElseThrow(() -> new NotFoundException("세션을 찾을 수 없습니다. sessionId: " + sessionId));

        List<ProductStockPort.StockItem> stockItems = session.getItems().stream()
                .map(item -> new ProductStockPort.StockItem(item.getProductId(), item.getQuantity()))
                .toList();

        productStockPort.restoreStock(stockItems);
        orderSessionRepository.delete(session);
    }

    public OrderResult processPayment(PaymentRequest request) {
        OrderSession session = orderSessionRepository.findBySessionIdWithItems(request.sessionId())
                .orElseThrow(() -> new NotFoundException("주문 세션을 찾을 수 없습니다. sessionId: " + request.sessionId()));

        int totalPrice = session.getItems().stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();

        int discountAmount = 0;
        if (request.couponId() != null) {
            CouponPort.CouponInfo couponInfo = couponPort.validateAndApply(request.couponId(), session.getAccountId());
            discountAmount = couponInfo.discountAmount();
        }

        int paymentAmount = Math.max(0, totalPrice - discountAmount);

        String orderId = session.getSessionId();
        PaymentPort.PaymentResult paymentResult = paymentPort.processPayment(
                new PaymentPort.PaymentRequest(session.getAccountId(), paymentAmount, orderId)
        );

        if (!paymentResult.success()) {
            if (request.couponId() != null) {
                couponPort.cancelUsage(request.couponId(), session.getAccountId());
                log.info("결제 실패로 쿠폰 사용 취소: couponId={}", request.couponId());
            }
            log.error("결제 실패: sessionId={}, reason={}", request.sessionId(), paymentResult.failReason());
            throw PaymentException.failed(paymentResult.failReason());
        }

        Order order = orderService.createOrder(
                session,
                totalPrice,
                discountAmount,
                request.couponId(),
                request.addressId(),
                paymentResult.transactionId()
        );

        log.info("주문 완료: orderId={}, transactionId={}", order.getId(), paymentResult.transactionId());

        return OrderResult.from(order);
    }
}
