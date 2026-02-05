package com.zerofive.store.api.controller.coupon.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "쿠폰 대기열 진입 응답")
public record CouponQueueResponse(
        @Schema(description = "대기열 등록 상태", example = "QUEUED")
        String status,

        @Schema(description = "대기 순번", example = "42")
        Long position
) {
}
