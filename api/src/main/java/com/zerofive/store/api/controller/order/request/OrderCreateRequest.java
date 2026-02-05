package com.zerofive.store.api.controller.order.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

@Schema(description = "주문 생성 요청")
public record OrderCreateRequest(
        @Schema(description = "주문 상품 목록")
        @NotEmpty @Valid
        List<OrderItemRequest> items,

        @Schema(description = "쿠폰 ID (선택)", example = "1")
        Long couponId
) {
}
