package com.zerofive.store.api.controller.order;

import com.zerofive.store.api.controller.order.request.OrderItemRequest;
import com.zerofive.store.api.controller.order.request.PaymentRequest;
import com.zerofive.store.api.controller.order.response.OrderResponse;
import com.zerofive.store.api.controller.order.response.OrderSessionResponse;
import com.zerofive.store.core.response.ApiResult;
import com.zerofive.store.order.application.OrderAppService;
import com.zerofive.store.order.application.dto.OrderResult;
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

    @Operation(summary = "결제 및 주문 확정")
    @PostMapping("/payment")
    public ApiResult<OrderResponse> processPayment(
            @RequestBody @Valid PaymentRequest request) {
        OrderResult result = orderAppService.processPayment(request.toServiceDto());
        return ApiResult.ok(OrderResponse.from(result));
    }

    @Operation(summary = "주문 목록 조회")
    @GetMapping
    public ApiResult<List<OrderResponse>> getOrders(
            @AuthenticationPrincipal Long accountId) {
        List<OrderResponse> orders = orderAppService.getOrders(accountId).stream()
                .map(OrderResponse::from)
                .toList();
        return ApiResult.ok(orders);
    }

    @Operation(summary = "주문 상세 조회")
    @GetMapping("/{orderId}")
    public ApiResult<OrderResponse> getOrder(
            @AuthenticationPrincipal Long accountId,
            @Parameter(description = "주문 ID") @PathVariable Long orderId) {
        OrderResult result = orderAppService.getOrder(orderId, accountId);
        return ApiResult.ok(OrderResponse.from(result));
    }

}
