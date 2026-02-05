package com.zerofive.store.api.controller.coupon.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "쿠폰 생성 요청 (관리자)")
public record CouponCreateRequest(
        @Schema(description = "쿠폰명", example = "신규 가입 할인 쿠폰")
        @NotBlank
        String name,

        @Schema(description = "할인 금액", example = "5000")
        @NotNull @Min(1)
        Integer discountAmount,

        @Schema(description = "총 발급 수량", example = "100")
        @NotNull @Min(1)
        Integer totalQuantity
) {
}
