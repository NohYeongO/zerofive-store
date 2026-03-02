package com.zerofive.store.api.controller.order.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주문 상세 응답")
public record OrderResponse(
        @Schema(description = "주문 패이지 Session ID", example = "1")
        String sessionId,

        @Schema(description = "주문 상태", example = "PENDING")
        String status,

        @Schema(description = "주문 상품 목록")
        List<OrderItemResponse> items,

        @Schema(description = "총 금액", example = "118000")
        int totalPrice,

        @Schema(description = "결제 예정 금액", example = "113000")
        int paymentAmount,

        @Schema(description = "주문 일시")
        LocalDateTime orderedAt
) {
}
