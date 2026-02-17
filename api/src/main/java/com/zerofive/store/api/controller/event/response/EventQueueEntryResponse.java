package com.zerofive.store.api.controller.event.response;

import com.zerofive.store.coupon.application.dto.QueueEntryResult;
import com.zerofive.store.coupon.application.dto.QueueStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "이벤트 대기열 진입 응답")
public record EventQueueEntryResponse(
        @Schema(description = "상태 (QUEUED / ADMITTED)", example = "QUEUED")
        QueueStatus status,

        @Schema(description = "대기 순번", example = "42")
        Long position
) {
    public static EventQueueEntryResponse from(QueueEntryResult result) {
        return new EventQueueEntryResponse(result.status(), result.position());
    }
}
