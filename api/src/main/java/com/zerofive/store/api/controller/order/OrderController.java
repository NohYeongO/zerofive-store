package com.zerofive.store.api.controller.order;

import com.zerofive.store.api.controller.order.request.OrderItemRequest;
import com.zerofive.store.api.controller.order.response.OrderItemResponse;
import com.zerofive.store.api.controller.order.response.OrderResponse;
import com.zerofive.store.api.controller.order.response.OrderSessionResponse;
import com.zerofive.store.api.controller.order.response.OrderSummaryResponse;
import com.zerofive.store.core.response.ApiResult;
import com.zerofive.store.order.application.OrderAppService;
import com.zerofive.store.order.application.dto.OrderSessionResult;
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

import java.time.LocalDateTime;
import java.util.List;

@Tag(name = "Order", description = "주문 API")
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderAppService orderAppService;

    @Operation(summary = "주문 생성 (재고 차감, 쿠폰/배송지 조회)")
    @PostMapping
    public ApiResult<OrderSessionResponse> createOrder(
            @AuthenticationPrincipal Long accountId,
            @RequestBody @Valid List<OrderItemRequest> request) {

        OrderSessionResult result = orderAppService.createOrder(
                accountId,
                OrderItemRequest.toServiceDtos(request)
        );
        return ApiResult.ok(OrderSessionResponse.from(result));
    }

    @Operation(summary = "세션 폴링 (세션 유지)")
    @PostMapping("/sessions/{sessionId}/polling")
    public ApiResult<String> polling(
            @Parameter(description = "세션 ID") @PathVariable String sessionId) {
        orderAppService.polling(sessionId);
        return ApiResult.ok(sessionId);
    }

    @Operation(summary = "주문 취소 (재고 복구)")
    @PostMapping("/sessions/{sessionId}/cancel")
    public ApiResult<String> cancelOrder(
            @Parameter(description = "세션 ID") @PathVariable String sessionId) {
        orderAppService.cancelOrder(sessionId);
        return ApiResult.ok(sessionId);
    }

    private static final OrderResponse MOCK_ORDER = new OrderResponse(
            "mock-session-id",
            "PENDING",
            List.of(
                    new OrderItemResponse(1L, 1L, "무선 이어폰", 59000, 2),
                    new OrderItemResponse(2L, 3L, "프리미엄 텀블러", 32000, 1)
            ),
            150000,
            145000,
            LocalDateTime.now()
    );

    @Operation(summary = "결제 (주문 완료) - Mock")
    @PostMapping("/{orderId}/payment")
    public ApiResult<OrderResponse> payment(
            @Parameter(description = "주문 ID") @PathVariable Long orderId) {
        OrderResponse paid = new OrderResponse(
                MOCK_ORDER.sessionId(),
                "PAID",
                MOCK_ORDER.items(),
                MOCK_ORDER.totalPrice(),
                MOCK_ORDER.paymentAmount(),
                MOCK_ORDER.orderedAt()
        );
        return ApiResult.ok(paid);
    }

    @Operation(summary = "주문 목록 조회 - Mock")
    @GetMapping
    public ApiResult<List<OrderSummaryResponse>> getOrders() {
        List<OrderSummaryResponse> mock = List.of(
                new OrderSummaryResponse(1L, "PAID", 3, 145000, LocalDateTime.now().minusDays(1)),
                new OrderSummaryResponse(2L, "PENDING", 1, 59000, LocalDateTime.now())
        );
        return ApiResult.ok(mock);
    }

    @Operation(summary = "주문 상세 조회 - Mock")
    @GetMapping("/{orderId}")
    public ApiResult<OrderResponse> getOrder(
            @Parameter(description = "주문 ID") @PathVariable Long orderId) {
        return ApiResult.ok(MOCK_ORDER);
    }
}
