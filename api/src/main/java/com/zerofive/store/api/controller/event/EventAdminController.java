package com.zerofive.store.api.controller.event;

import com.zerofive.store.api.controller.event.request.ThresholdUpdateRequest;
import com.zerofive.store.api.controller.event.response.ThresholdResponse;
import com.zerofive.store.core.response.ApiResult;
import com.zerofive.store.coupon.application.EventQueueAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@Tag(name = "Event Admin", description = "이벤트 대기열 관리자 API")
@RestController
@RequestMapping("/api/admin/events")
@RequiredArgsConstructor
public class EventAdminController {

    private final EventQueueAdminService adminService;

    @Operation(summary = "이벤트 대기열 활성화", description = "스케줄러가 해당 이벤트 처리를 시작합니다.")
    @PostMapping("/{eventId}/queue/activate")
    public ApiResult<Void> activateEventQueue(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId) {

        adminService.activateEventQueue(eventId);
        return ApiResult.ok(null);
    }

    @Operation(summary = "이벤트 대기열 비활성화", description = "스케줄러가 해당 이벤트 처리를 중단합니다.")
    @DeleteMapping("/{eventId}/queue/activate")
    public ApiResult<Void> deactivateEventQueue(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId) {

        adminService.deactivateEventQueue(eventId);
        return ApiResult.ok(null);
    }

    @Operation(summary = "쿠폰 발급 활성화", description = "스케줄러가 해당 쿠폰 발급 처리를 시작합니다.")
    @PostMapping("/coupons/{couponId}/activate")
    public ApiResult<Void> activateCouponIssue(
            @Parameter(description = "쿠폰 ID") @PathVariable Long couponId,
            @Parameter(description = "재고 수량") @RequestParam int stock) {

        adminService.activateCouponIssue(couponId, stock);
        return ApiResult.ok(null);
    }

    @Operation(summary = "쿠폰 발급 비활성화", description = "스케줄러가 해당 쿠폰 발급 처리를 중단합니다.")
    @DeleteMapping("/coupons/{couponId}/activate")
    public ApiResult<Void> deactivateCouponIssue(
            @Parameter(description = "쿠폰 ID") @PathVariable Long couponId) {

        adminService.deactivateCouponIssue(couponId);
        return ApiResult.ok(null);
    }

    @Operation(summary = "활성화된 이벤트 목록", description = "현재 스케줄러가 처리 중인 이벤트 목록")
    @GetMapping("/queue/active")
    public ApiResult<Set<Long>> getActiveEvents() {
        return ApiResult.ok(adminService.getActiveEvents());
    }

    @Operation(summary = "활성화된 쿠폰 목록", description = "현재 스케줄러가 처리 중인 쿠폰 목록")
    @GetMapping("/coupons/active")
    public ApiResult<Set<Long>> getActiveCoupons() {
        return ApiResult.ok(adminService.getActiveCoupons());
    }

    @Operation(summary = "입장 임계값 조회")
    @GetMapping("/{eventId}/threshold")
    public ApiResult<ThresholdResponse> getThreshold(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId) {

        int threshold = adminService.getThreshold(eventId);
        return ApiResult.ok(ThresholdResponse.of(eventId, threshold));
    }

    @Operation(summary = "입장 임계값 변경")
    @PutMapping("/{eventId}/threshold")
    public ApiResult<ThresholdResponse> updateThreshold(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            @RequestBody ThresholdUpdateRequest request) {

        adminService.updateThreshold(eventId, request.threshold());
        return ApiResult.ok(ThresholdResponse.of(eventId, request.threshold()));
    }
}
