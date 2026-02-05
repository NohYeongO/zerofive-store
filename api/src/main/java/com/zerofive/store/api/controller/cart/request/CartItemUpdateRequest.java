package com.zerofive.store.api.controller.cart.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "장바구니 상품 수량 변경 요청")
public record CartItemUpdateRequest(
        @Schema(description = "수량", example = "3")
        @NotNull @Min(1)
        Integer quantity
) {
}
