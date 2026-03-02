package com.zerofive.store.api.controller.order.response;

import com.zerofive.store.order.application.dto.OrderSessionResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "주문 세션 응답")
public record OrderSessionResponse(
        @Schema(description = "세션 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        String sessionId,

        @Schema(description = "주문 상품 목록")
        List<OrderItemInfo> items,

        @Schema(description = "총 금액", example = "150000")
        int totalPrice,

        @Schema(description = "사용 가능한 쿠폰 목록")
        List<CouponInfo> coupons,

        @Schema(description = "배송지 목록")
        List<AddressInfo> addresses
) {
    public static OrderSessionResponse from(OrderSessionResult result) {
        return new OrderSessionResponse(
                result.sessionId(),
                result.items().stream()
                        .map(i -> new OrderItemInfo(i.productId(), i.productName(), i.price(), i.quantity()))
                        .toList(),
                result.totalPrice(),
                result.coupons().stream()
                        .map(c -> new CouponInfo(c.couponId(), c.couponName(), c.discountAmount()))
                        .toList(),
                result.addresses().stream()
                        .map(a -> new AddressInfo(a.id(), a.name(), a.phone(),
                                a.address(), a.detailAddress(), a.postalCode(), a.isDefault()))
                        .toList()
        );
    }

    @Schema(description = "주문 상품 정보")
    public record OrderItemInfo(
            @Schema(description = "상품 ID", example = "1")
            Long productId,
            @Schema(description = "상품명", example = "무선 이어폰")
            String productName,
            @Schema(description = "가격", example = "59000")
            int price,
            @Schema(description = "수량", example = "2")
            int quantity
    ) {
    }

    @Schema(description = "쿠폰 정보")
    public record CouponInfo(
            @Schema(description = "쿠폰 ID", example = "1")
            Long couponId,
            @Schema(description = "쿠폰명", example = "신규 회원 할인")
            String couponName,
            @Schema(description = "할인 금액", example = "5000")
            int discountAmount
    ) {
    }

    @Schema(description = "배송지 정보")
    public record AddressInfo(
            @Schema(description = "배송지 ID", example = "1")
            Long id,
            @Schema(description = "수령인", example = "홍길동")
            String name,
            @Schema(description = "연락처", example = "010-1234-5678")
            String phone,
            @Schema(description = "주소", example = "서울시 강남구 테헤란로 123")
            String address,
            @Schema(description = "상세주소", example = "101동 1001호")
            String detailAddress,
            @Schema(description = "우편번호", example = "06234")
            String postalCode,
            @Schema(description = "기본 배송지 여부", example = "true")
            boolean isDefault
    ) {
    }
}
