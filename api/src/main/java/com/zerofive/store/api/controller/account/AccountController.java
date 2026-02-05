package com.zerofive.store.api.controller.account;

import com.zerofive.store.api.controller.account.request.LoginRequest;
import com.zerofive.store.api.controller.account.request.SignupRequest;
import com.zerofive.store.api.controller.account.response.AccountResponse;
import com.zerofive.store.api.controller.account.response.LoginResponse;
import com.zerofive.store.core.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Account", description = "계정 API")
@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    @Operation(summary = "회원가입")
    @PostMapping("/signup")
    public ApiResponse<AccountResponse> signup(@RequestBody @Valid SignupRequest request) {
        AccountResponse mock = new AccountResponse(1L, request.email(), request.name(), "USER");
        return ApiResponse.ok(mock);
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse mock = new LoginResponse("eyJhbGciOiJIUzI1NiJ9.mock-token");
        return ApiResponse.ok(mock);
    }

    @Operation(summary = "내 정보 조회")
    @GetMapping("/me")
    public ApiResponse<AccountResponse> getMyInfo() {
        AccountResponse mock = new AccountResponse(1L, "user@example.com", "홍길동", "USER");
        return ApiResponse.ok(mock);
    }
}
