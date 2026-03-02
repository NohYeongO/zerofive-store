package com.zerofive.store.order.application.dto;

public record OrderItemRequest(
        Long productId,
        int quantity
) {
    public static OrderItemRequest of(Long productId, int quantity) {
        return new OrderItemRequest(productId, quantity);
    }
}
