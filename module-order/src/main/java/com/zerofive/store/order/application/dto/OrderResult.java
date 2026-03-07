package com.zerofive.store.order.application.dto;

import com.zerofive.store.order.domain.entity.Order;
import com.zerofive.store.order.domain.entity.OrderStatus;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResult(
        Long orderId,
        OrderStatus status,
        List<OrderItemResult> items,
        int totalPrice,
        int discountAmount,
        int paymentAmount,
        String transactionId,
        LocalDateTime orderedAt
) {
    public static OrderResult from(Order order) {
        List<OrderItemResult> items = order.getItems().stream()
                .map(item -> new OrderItemResult(
                        item.getProductId(),
                        item.getProductName(),
                        item.getPrice(),
                        item.getQuantity()
                ))
                .toList();

        return new OrderResult(
                order.getId(),
                order.getStatus(),
                items,
                order.getTotalPrice(),
                order.getDiscountAmount(),
                order.getPaymentAmount(),
                order.getTransactionId(),
                order.getCreatedAt()
        );
    }
}
