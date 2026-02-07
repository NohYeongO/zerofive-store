package com.zerofive.store.api.controller.coupon;

import com.zerofive.store.api.controller.coupon.request.CouponCreateRequest;
import com.zerofive.store.api.controller.coupon.response.CouponIssueResponse;
import com.zerofive.store.api.controller.coupon.response.CouponQueueResponse;
import com.zerofive.store.api.controller.coupon.response.CouponQueueStatusResponse;
import com.zerofive.store.api.controller.coupon.response.CouponResponse;
import com.zerofive.store.api.controller.coupon.response.MyCouponResponse;
import com.zerofive.store.core.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Coupon", description = "쿠폰 API")
@RestController
@RequestMapping("/api/coupons")
public class CouponController {

    @Operation(summary = "쿠폰 생성 (관리자)")
    @PostMapping
    public ApiResult<CouponResponse> createCoupon(@RequestBody @Valid CouponCreateRequest request) {
        CouponResponse mock = new CouponResponse(
                1L, request.name(), request.discountAmount(), request.totalQuantity(), 0
        );
        return ApiResult.ok(mock);
    }

    @Operation(summary = "쿠폰 발급 요청 (순차 처리)")
    @PostMapping("/{couponId}/issue")
    public ApiResult<CouponIssueResponse> issueCoupon(
            @Parameter(description = "쿠폰 ID") @PathVariable Long couponId) {
        CouponIssueResponse mock = new CouponIssueResponse("ISSUED");
        return ApiResult.ok(mock);
    }

    @Operation(summary = "쿠폰 대기열 진입 (이벤트 페이지 접속)")
    @PostMapping("/{couponId}/queue")
    public ApiResult<CouponQueueResponse> enterQueue(
            @Parameter(description = "쿠폰 ID") @PathVariable Long couponId) {
        CouponQueueResponse mock = new CouponQueueResponse("QUEUED", 42L);
        return ApiResult.ok(mock);
    }

    @Operation(summary = "쿠폰 대기열 상태 조회 (HTTP polling)")
    @GetMapping("/{couponId}/queue/status")
    public ApiResult<CouponQueueStatusResponse> getQueueStatus(
            @Parameter(description = "쿠폰 ID") @PathVariable Long couponId) {
        CouponQueueStatusResponse mock = new CouponQueueStatusResponse("WAITING", 12L);
        return ApiResult.ok(mock);
    }

    @Operation(summary = "내 쿠폰 목록 조회")
    @GetMapping("/me")
    public ApiResult<List<MyCouponResponse>> getMyCoupons() {
        List<MyCouponResponse> mock = List.of(
                new MyCouponResponse(1L, "신규 가입 할인 쿠폰", 5000, false, LocalDateTime.now()),
                new MyCouponResponse(2L, "여름 특별 할인", 3000, true, LocalDateTime.now().minusDays(7))
        );
        return ApiResult.ok(mock);
    }
}
