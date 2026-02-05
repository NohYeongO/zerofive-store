package com.zerofive.store.api.controller.order.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "주문 목록 응답")
public record OrderSummaryResponse(
        @Schema(description = "주문 ID", example = "1")
        Long id,

        @Schema(description = "주문 상태", example = "PAID")
        String status,

        @Schema(description = "총 상품 수", example = "3")
        int totalItemCount,

        @Schema(description = "결제 금액", example = "113000")
        int paymentAmount,

        @Schema(description = "주문 일시")
        LocalDateTime orderedAt
) {
}
