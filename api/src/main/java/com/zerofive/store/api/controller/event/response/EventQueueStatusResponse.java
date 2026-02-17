package com.zerofive.store.api.controller.event.response;

import com.zerofive.store.coupon.application.dto.QueueStatus;
import com.zerofive.store.coupon.application.dto.QueueStatusResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이벤트 대기열 상태 조회 응답")
public record EventQueueStatusResponse(
        @Schema(description = "상태 (WAITING / ADMITTED / NOT_IN_QUEUE)", example = "WAITING")
        QueueStatus status,

        @Schema(description = "현재 대기 순번 (WAITING 상태일 때만 유효)", example = "12")
        Long position
) {
    public static EventQueueStatusResponse from(QueueStatusResult result) {
        return new EventQueueStatusResponse(result.status(), result.position());
    }
}
