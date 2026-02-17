package com.zerofive.store.api.controller.event.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "임계값 변경 요청")
public record ThresholdUpdateRequest(
        @Schema(description = "새로운 임계값", example = "200")
        int threshold
) {
}
