package com.zerofive.store.api.controller.coupon.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "쿠폰 대기열 상태 조회 응답 (HTTP polling)")
public record CouponQueueStatusResponse(
        @Schema(description = "상태 (WAITING / ISSUED / FAILED)", example = "WAITING")
        String status,

        @Schema(description = "현재 대기 순번", example = "12")
        Long position
) {
}
