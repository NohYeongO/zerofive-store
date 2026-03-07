package com.zerofive.store.order.application.dto;

public record OrderItemResult(
        Long productId,
        String productName,
        int price,
        int quantity
) {
}
