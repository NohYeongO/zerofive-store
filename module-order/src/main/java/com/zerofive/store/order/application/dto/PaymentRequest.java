package com.zerofive.store.order.application.dto;

public record PaymentRequest(
        String sessionId,
        Long couponId,
        Long addressId
) {
}
