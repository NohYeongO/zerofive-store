package com.zerofive.store.order.domain;

import com.zerofive.store.order.domain.entity.Order;
import com.zerofive.store.order.domain.entity.OrderItem;
import com.zerofive.store.order.domain.entity.OrderSession;
import com.zerofive.store.order.domain.entity.OrderSessionItem;
import com.zerofive.store.order.infra.repository.OrderRepository;
import com.zerofive.store.order.infra.repository.OrderSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderSessionRepository orderSessionRepository;

    @Transactional
    public Order createOrder(OrderSession session, int totalPrice, int discountAmount,
                             Long couponId, Long addressId, String transactionId) {
        int paymentAmount = Math.max(0, totalPrice - discountAmount);

        Order order = Order.builder()
                .accountId(session.getAccountId())
                .totalPrice(totalPrice)
                .discountAmount(discountAmount)
                .paymentAmount(paymentAmount)
                .couponId(couponId)
                .addressId(addressId)
                .transactionId(transactionId)
                .build();

        for (OrderSessionItem sessionItem : session.getItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .productId(sessionItem.getProductId())
                    .productName(sessionItem.getProductName())
                    .price(sessionItem.getPrice())
                    .quantity(sessionItem.getQuantity())
                    .build();
            order.addItem(orderItem);
        }

        Order savedOrder = orderRepository.save(order);
        orderSessionRepository.delete(session);

        return savedOrder;
    }
}
