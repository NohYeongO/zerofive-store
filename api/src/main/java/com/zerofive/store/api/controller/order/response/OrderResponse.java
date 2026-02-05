package com.zerofive.store.api.controller.order.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주문 상세 응답")
public record OrderResponse(
        @Schema(description = "주문 ID", example = "1")
        Long id,

        @Schema(description = "주문 상태", example = "PENDING")
        String status,

        @Schema(description = "주문 상품 목록")
        List<OrderItemResponse> items,

        @Schema(description = "총 금액", example = "118000")
        int totalPrice,

        @Schema(description = "할인 금액", example = "5000")
        int discountAmount,

        @Schema(description = "결제 금액", example = "113000")
        int paymentAmount,

        @Schema(description = "주문 일시")
        LocalDateTime orderedAt
) {
}
