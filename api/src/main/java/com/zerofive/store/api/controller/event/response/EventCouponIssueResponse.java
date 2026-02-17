package com.zerofive.store.api.controller.event.response;

import com.zerofive.store.coupon.application.dto.CouponIssueResult;
import com.zerofive.store.coupon.application.dto.QueueStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이벤트 쿠폰 발급 요청 응답")
public record EventCouponIssueResponse(
        @Schema(description = "발급 상태 (SUCCESS / DUPLICATE / SOLD_OUT)", example = "SUCCESS")
        QueueStatus status,

        @Schema(description = "메시지", example = "쿠폰이 발급되었습니다.")
        String message
) {
    public static EventCouponIssueResponse from(CouponIssueResult result) {
        String message = switch (result.status()) {
            case SUCCESS -> "쿠폰이 발급되었습니다.";
            case DUPLICATE -> "이미 발급받은 쿠폰입니다.";
            case SOLD_OUT -> "쿠폰이 모두 소진되었습니다.";
            default -> "알 수 없는 상태입니다.";
        };
        return new EventCouponIssueResponse(result.status(), message);
    }
}
