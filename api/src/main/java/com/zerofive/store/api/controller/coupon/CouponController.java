package com.zerofive.store.api.controller.coupon;

import com.zerofive.store.api.controller.coupon.request.CouponCreateRequest;
import com.zerofive.store.api.controller.coupon.response.CouponIssueResponse;
import com.zerofive.store.api.controller.coupon.response.CouponResponse;
import com.zerofive.store.api.controller.coupon.response.MyCouponResponse;
import com.zerofive.store.core.response.ApiResult;
import com.zerofive.store.coupon.domain.CouponService;
import com.zerofive.store.coupon.domain.entity.CouponType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Coupon", description = "일반 쿠폰 API")
@RestController
@RequestMapping("/api/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;

    @Operation(summary = "쿠폰 생성 (관리자)")
    @PostMapping
    public ApiResult<CouponResponse> createCoupon(@RequestBody @Valid CouponCreateRequest request) {
        var coupon = couponService.createCoupon(
                request.name(),
                request.discountAmount(),
                CouponType.NORMAL,
                request.totalQuantity()
        );
        return ApiResult.ok(CouponResponse.from(coupon));
    }

    @Operation(summary = "일반 쿠폰 발급 요청")
    @PostMapping("/{couponId}/issue")
    public ApiResult<CouponIssueResponse> issueCoupon(
            @Parameter(description = "쿠폰 ID") @PathVariable Long couponId,
            @AuthenticationPrincipal Long accountId) {

        couponService.issueNormalCoupon(couponId, accountId);
        return ApiResult.ok(CouponIssueResponse.issued());
    }

    @Operation(summary = "내 쿠폰 목록 조회")
    @GetMapping("/me")
    public ApiResult<List<MyCouponResponse>> getMyCoupons(@AuthenticationPrincipal Long accountId) {
        var response = couponService.getMyCoupons(accountId).stream()
                .map(MyCouponResponse::from)
                .toList();
        return ApiResult.ok(response);
    }
}
