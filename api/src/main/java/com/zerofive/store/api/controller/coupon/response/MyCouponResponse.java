package com.zerofive.store.api.controller.coupon.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "내 쿠폰 응답")
public record MyCouponResponse(
        @Schema(description = "쿠폰 ID", example = "1")
        Long couponId,

        @Schema(description = "쿠폰명", example = "신규 가입 할인 쿠폰")
        String couponName,

        @Schema(description = "할인 금액", example = "5000")
        int discountAmount,

        @Schema(description = "사용 여부", example = "false")
        boolean used,

        @Schema(description = "발급 일시")
        LocalDateTime issuedAt
) {
}
