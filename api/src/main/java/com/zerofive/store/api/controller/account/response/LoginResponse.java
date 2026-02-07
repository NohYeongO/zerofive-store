package com.zerofive.store.api.controller.account.response;

import com.zerofive.store.account.application.dto.LoginResult;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인 응답")
public record LoginResponse(
        @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken
) {
    public static LoginResponse from(LoginResult result) {
        return new LoginResponse(result.accessToken());
    }
}
