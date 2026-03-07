package com.zerofive.store.api.controller.order.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PaymentRequest(
        @NotBlank(message = "세션 ID는 필수입니다")
        String sessionId,

        Long couponId,

        @NotNull(message = "배송지 ID는 필수입니다")
        Long addressId
) {
    public com.zerofive.store.order.application.dto.PaymentRequest toServiceDto() {
        return new com.zerofive.store.order.application.dto.PaymentRequest(sessionId, couponId, addressId);
    }
}
