package com.zerofive.store.api.controller.order.response;

import com.zerofive.store.order.application.dto.OrderResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "주문 상세 응답")
public record OrderResponse(
        @Schema(description = "주문 ID", example = "1")
        Long orderId,

        @Schema(description = "주문 상태", example = "PAYMENT_COMPLETED")
        String status,

        @Schema(description = "주문 상품 목록")
        List<OrderItemResponse> items,

        @Schema(description = "총 금액", example = "150000")
        int totalPrice,

        @Schema(description = "할인 금액", example = "5000")
        int discountAmount,

        @Schema(description = "결제 금액", example = "145000")
        int paymentAmount,

        @Schema(description = "결제 트랜잭션 ID")
        String transactionId,

        @Schema(description = "주문 일시")
        LocalDateTime orderedAt
) {
    public static OrderResponse from(OrderResult result) {
        List<OrderItemResponse> items = result.items().stream()
                .map(item -> new OrderItemResponse(
                        item.productId(),
                        item.productName(),
                        item.price(),
                        item.quantity()
                ))
                .toList();

        return new OrderResponse(
                result.orderId(),
                result.status().name(),
                items,
                result.totalPrice(),
                result.discountAmount(),
                result.paymentAmount(),
                result.transactionId(),
                result.orderedAt()
        );
    }
}
