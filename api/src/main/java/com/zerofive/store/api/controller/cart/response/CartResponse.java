package com.zerofive.store.api.controller.cart.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "장바구니 응답")
public record CartResponse(
        @Schema(description = "장바구니 ID", example = "1")
        Long id,

        @Schema(description = "장바구니 항목 목록")
        List<CartItemResponse> items,

        @Schema(description = "총 금액", example = "248000")
        int totalPrice
) {
}
