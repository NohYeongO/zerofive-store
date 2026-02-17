package com.zerofive.store.api.controller.coupon.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "쿠폰 발급 응답")
public record CouponIssueResponse(
        @Schema(description = "발급 상태", example = "ISSUED")
        String status
) {
    public static CouponIssueResponse issued() {
        return new CouponIssueResponse("ISSUED");
    }
}
