package com.zerofive.store.order.application.dto;

import com.zerofive.store.core.port.AddressPort;
import com.zerofive.store.core.port.CouponPort;
import com.zerofive.store.order.domain.entity.OrderSession;

import java.util.List;

public record OrderSessionResult(
        String sessionId,
        List<OrderItemInfo> items,
        int totalPrice,
        List<CouponInfo> coupons,
        List<AddressInfo> addresses
) {
    public static OrderSessionResult of(
            OrderSession session,
            List<CouponPort.CouponInfo> coupons,
            List<AddressPort.AddressInfo> addresses
    ) {
        List<OrderItemInfo> items = session.getItems().stream()
                .map(item -> new OrderItemInfo(
                        item.getProductId(),
                        item.getProductName(),
                        item.getPrice(),
                        item.getQuantity()
                ))
                .toList();

        int totalPrice = session.getItems().stream()
                .mapToInt(item -> item.getPrice() * item.getQuantity())
                .sum();

        List<CouponInfo> couponInfos = coupons.stream()
                .map(c -> new CouponInfo(c.couponId(), c.couponName(), c.discountAmount()))
                .toList();

        List<AddressInfo> addressInfos = addresses.stream()
                .map(a -> new AddressInfo(
                        a.id(), a.name(), a.phone(),
                        a.address(), a.detailAddress(), a.postalCode(), a.isDefault()
                ))
                .toList();

        return new OrderSessionResult(session.getSessionId(), items, totalPrice, couponInfos, addressInfos);
    }

    public record OrderItemInfo(Long productId, String productName, int price, int quantity) {
    }

    public record CouponInfo(Long couponId, String couponName, int discountAmount) {
    }

    public record AddressInfo(Long id, String name, String phone, String address,
                               String detailAddress, String postalCode, boolean isDefault) {
    }
}
