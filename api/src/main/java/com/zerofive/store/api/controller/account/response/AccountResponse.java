package com.zerofive.store.api.controller.account.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "계정 응답")
public record AccountResponse(
        @Schema(description = "계정 ID", example = "1")
        Long id,

        @Schema(description = "이메일", example = "user@example.com")
        String email,

        @Schema(description = "이름", example = "홍길동")
        String name,

        @Schema(description = "역할", example = "USER")
        String role
) {
}
