package com.zerofive.store.cart.application.dto;

import com.zerofive.store.cart.domain.entity.CartItem;
import com.zerofive.store.core.port.ProductPort.ProductInfo;

public record CartItemResult(
        Long id,
        Long productId,
        String productName,
        int productPrice,
        int quantity
) {

    public static CartItemResult from(CartItem item, ProductInfo product) {
        return new CartItemResult(
                item.getId(),
                item.getProductId(),
                product.name(),
                product.price(),
                item.getQuantity()
        );
    }
}
