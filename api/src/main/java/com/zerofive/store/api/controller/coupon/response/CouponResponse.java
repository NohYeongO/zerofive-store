package com.zerofive.store.api.controller.coupon.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "쿠폰 응답")
public record CouponResponse(
        @Schema(description = "쿠폰 ID", example = "1")
        Long id,

        @Schema(description = "쿠폰명", example = "신규 가입 할인 쿠폰")
        String name,

        @Schema(description = "할인 금액", example = "5000")
        int discountAmount,

        @Schema(description = "총 발급 수량", example = "100")
        int totalQuantity,

        @Schema(description = "발급된 수량", example = "0")
        int issuedQuantity
) {
}
