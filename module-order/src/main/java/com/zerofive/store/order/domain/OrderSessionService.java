package com.zerofive.store.order.domain;

import com.zerofive.store.core.port.ProductPort;
import com.zerofive.store.order.application.dto.OrderItemRequest;
import com.zerofive.store.order.domain.entity.OrderSession;
import com.zerofive.store.order.domain.entity.OrderSessionItem;
import com.zerofive.store.order.infra.repository.OrderSessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderSessionService {

    private final OrderSessionRepository orderSessionRepository;
    private final ProductPort productPort;

    public OrderSession createSession(Long accountId, List<OrderItemRequest> items) {
        String sessionId = accountId + "_" + System.currentTimeMillis();

        OrderSession session = OrderSession.builder()
                .sessionId(sessionId)
                .accountId(accountId)
                .build();

        for (OrderItemRequest item : items) {
            ProductPort.ProductInfo productInfo = productPort.getProduct(item.productId());

            OrderSessionItem sessionItem = OrderSessionItem.builder()
                    .productId(item.productId())
                    .productName(productInfo.name())
                    .price(productInfo.price())
                    .quantity(item.quantity())
                    .build();

            session.addItem(sessionItem);
        }

        return orderSessionRepository.save(session);
    }
}
