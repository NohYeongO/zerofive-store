package com.zerofive.store.api.controller.cart.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장바구니 상품 항목 응답")
public record CartItemResponse(
        @Schema(description = "장바구니 항목 ID", example = "1")
        Long id,

        @Schema(description = "상품 ID", example = "1")
        Long productId,

        @Schema(description = "상품명", example = "무선 이어폰")
        String productName,

        @Schema(description = "상품 가격", example = "59000")
        int productPrice,

        @Schema(description = "수량", example = "2")
        int quantity
) {
}
