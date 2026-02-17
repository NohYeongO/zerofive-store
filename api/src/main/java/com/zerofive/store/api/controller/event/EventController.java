package com.zerofive.store.api.controller.event;

import com.zerofive.store.api.controller.event.response.EventCouponIssueResponse;
import com.zerofive.store.api.controller.event.response.EventQueueEntryResponse;
import com.zerofive.store.api.controller.event.response.EventQueueStatusResponse;
import com.zerofive.store.core.response.ApiResult;
import com.zerofive.store.coupon.application.EventQueueAppService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Event", description = "이벤트 대기열 및 쿠폰 발급 API")
@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventController {

    private final EventQueueAppService appService;

    @Operation(summary = "대기열 진입", description = "이벤트 대기열에 진입합니다.")
    @PostMapping("/{eventId}/queue")
    public ApiResult<EventQueueEntryResponse> enterQueue(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            @AuthenticationPrincipal Long accountId) {

        var result = appService.enterQueue(eventId, accountId);
        return ApiResult.ok(EventQueueEntryResponse.from(result));
    }

    @Operation(summary = "대기열 상태 조회", description = "HTTP Polling으로 대기열 상태를 확인합니다.")
    @GetMapping("/{eventId}/queue/status")
    public ApiResult<EventQueueStatusResponse> getQueueStatus(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            @AuthenticationPrincipal Long accountId) {

        var result = appService.getQueueStatus(eventId, accountId);
        return ApiResult.ok(EventQueueStatusResponse.from(result));
    }

    @Operation(summary = "쿠폰 발급 요청", description = "이벤트 쿠폰 발급을 요청합니다.")
    @PostMapping("/{eventId}/coupons/{couponId}/issue")
    public ApiResult<EventCouponIssueResponse> issueCoupon(
            @Parameter(description = "이벤트 ID") @PathVariable Long eventId,
            @Parameter(description = "쿠폰 ID") @PathVariable Long couponId,
            @AuthenticationPrincipal Long accountId) {

        var result = appService.requestIssue(couponId, accountId);
        return ApiResult.ok(EventCouponIssueResponse.from(result));
    }
}
