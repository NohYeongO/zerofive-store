package com.zerofive.store.api.controller.account;

import com.zerofive.store.account.application.AccountAppService;
import com.zerofive.store.api.controller.account.request.LoginRequest;
import com.zerofive.store.api.controller.account.request.SignupRequest;
import com.zerofive.store.api.controller.account.response.AccountResponse;
import com.zerofive.store.api.controller.account.response.ErrorResponse;
import com.zerofive.store.api.controller.account.response.LoginResponse;
import com.zerofive.store.core.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Account", description = "계정 API")
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountAppService accountAppService;

    @Operation(summary = "회원가입")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "회원가입 성공"
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "이미 사용 중인 이메일 또는 유효성 검사 실패",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/signup")
    public ApiResult<AccountResponse> signup(@RequestBody @Valid SignupRequest request) {
        var result = accountAppService.signup(request.email(), request.password(), request.name());
        return ApiResult.ok(AccountResponse.from(result));
    }

    @Operation(summary = "로그인")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "로그인 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "이메일 또는 비밀번호가 올바르지 않습니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @PostMapping("/login")
    public ApiResult<LoginResponse> login(@RequestBody @Valid LoginRequest request) {
        var result = accountAppService.login(request.email(), request.password());
        return ApiResult.ok(LoginResponse.from(result));
    }

    @Operation(summary = "내 정보 조회")
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "조회 성공"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증되지 않은 요청",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "계정을 찾을 수 없습니다",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    @GetMapping("/me")
    public ApiResult<AccountResponse> getMyInfo(@AuthenticationPrincipal Long accountId) {
        var result = accountAppService.getAccount(accountId);
        return ApiResult.ok(AccountResponse.from(result));
    }
}
