package com.zerofive.store.api.controller.order;

import com.zerofive.store.api.controller.order.request.OrderCreateRequest;
import com.zerofive.store.api.controller.order.response.OrderItemResponse;
import com.zerofive.store.api.controller.order.response.OrderResponse;
import com.zerofive.store.api.controller.order.response.OrderSummaryResponse;
import com.zerofive.store.core.response.ApiResponse;
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

@Tag(name = "Order", description = "주문 API")
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private static final OrderResponse MOCK_ORDER = new OrderResponse(
            1L,
            "PENDING",
            List.of(
                    new OrderItemResponse(1L, 1L, "무선 이어폰", 59000, 2),
                    new OrderItemResponse(2L, 3L, "프리미엄 텀블러", 32000, 1)
            ),
            150000,
            5000,
            145000,
            LocalDateTime.now()
    );

    @Operation(summary = "주문 생성 (주문페이지 이동, 재고 차감)")
    @PostMapping
    public ApiResponse<OrderResponse> createOrder(@RequestBody @Valid OrderCreateRequest request) {
        return ApiResponse.ok(MOCK_ORDER);
    }

    @Operation(summary = "결제 (주문 완료)")
    @PostMapping("/{orderId}/payment")
    public ApiResponse<OrderResponse> payment(
            @Parameter(description = "주문 ID") @PathVariable Long orderId) {
        OrderResponse paid = new OrderResponse(
                orderId, "PAID",
                MOCK_ORDER.items(),
                MOCK_ORDER.totalPrice(),
                MOCK_ORDER.discountAmount(),
                MOCK_ORDER.paymentAmount(),
                MOCK_ORDER.orderedAt()
        );
        return ApiResponse.ok(paid);
    }

    @Operation(summary = "주문 목록 조회")
    @GetMapping
    public ApiResponse<List<OrderSummaryResponse>> getOrders() {
        List<OrderSummaryResponse> mock = List.of(
                new OrderSummaryResponse(1L, "PAID", 3, 145000, LocalDateTime.now().minusDays(1)),
                new OrderSummaryResponse(2L, "PENDING", 1, 59000, LocalDateTime.now())
        );
        return ApiResponse.ok(mock);
    }

    @Operation(summary = "주문 상세 조회")
    @GetMapping("/{orderId}")
    public ApiResponse<OrderResponse> getOrder(
            @Parameter(description = "주문 ID") @PathVariable Long orderId) {
        return ApiResponse.ok(MOCK_ORDER);
    }
}
