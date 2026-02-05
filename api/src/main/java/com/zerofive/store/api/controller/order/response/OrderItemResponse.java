package com.zerofive.store.api.controller.order.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "주문 상품 항목 응답")
public record OrderItemResponse(
        @Schema(description = "주문 항목 ID", example = "1")
        Long id,

        @Schema(description = "상품 ID", example = "1")
        Long productId,

        @Schema(description = "상품명", example = "무선 이어폰")
        String productName,

        @Schema(description = "주문 시 가격", example = "59000")
        int price,

        @Schema(description = "수량", example = "2")
        int quantity
) {
}
