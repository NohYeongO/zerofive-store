package com.zerofive.store.api.controller.event.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "임계값 응답")
public record ThresholdResponse(
        @Schema(description = "이벤트 ID")
        Long eventId,

        @Schema(description = "현재 임계값")
        int threshold
) {
    public static ThresholdResponse of(Long eventId, int threshold) {
        return new ThresholdResponse(eventId, threshold);
    }
}
