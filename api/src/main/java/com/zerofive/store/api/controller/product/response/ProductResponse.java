package com.zerofive.store.api.controller.product.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "상품 응답")
public record ProductResponse(
        @Schema(description = "상품 ID", example = "1")
        Long id,

        @Schema(description = "상품명", example = "무선 이어폰")
        String name,

        @Schema(description = "가격", example = "59000")
        int price,

        @Schema(description = "재고 수량", example = "100")
        int stockQuantity,

        @Schema(description = "카테고리", example = "ELECTRONICS")
        String category
) {
}
