package com.zerofive.store.api.controller.account.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "에러 응답")
public record ErrorResponse(
        @Schema(description = "에러 코드", example = "400")
        int code,

        @Schema(description = "에러 메시지", example = "이미 사용 중인 이메일입니다.")
        String message,

        @Schema(description = "데이터", nullable = true)
        Object data
) {
}
