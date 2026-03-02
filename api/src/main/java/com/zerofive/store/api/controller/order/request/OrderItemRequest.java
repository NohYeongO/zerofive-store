package com.zerofive.store.api.controller.order.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "주문 상품 항목 요청")
public record OrderItemRequest(
        @Schema(description = "상품 ID", example = "1")
        @NotNull
        Long productId,

        @Schema(description = "수량", example = "2")
        @NotNull @Min(1)
        Integer quantity
) {
    public static List<com.zerofive.store.order.application.dto.OrderItemRequest> toServiceDtos(
            List<OrderItemRequest> requests) {
        return requests.stream()
                .map(r -> com.zerofive.store.order.application.dto.OrderItemRequest.of(r.productId(), r.quantity()))
                .toList();
    }
}
