package com.zerofive.store.api.controller.cart.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "장바구니 상품 추가 요청")
public record CartItemAddRequest(
        @Schema(description = "상품 ID", example = "1")
        @NotNull
        Long productId,

        @Schema(description = "수량", example = "2")
        @NotNull @Min(1)
        Integer quantity
) {
}
