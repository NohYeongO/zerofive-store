package com.zerofive.store.api.controller.account.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "회원가입 요청")
public record SignupRequest(
        @Schema(description = "이메일", example = "user@example.com")
        @NotBlank @Email
        String email,

        @Schema(description = "비밀번호", example = "password123")
        @NotBlank
        String password,

        @Schema(description = "이름", example = "홍길동")
        @NotBlank
        String name
) {
}
