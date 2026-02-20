package com.zerofive.store.cart.application.dto;

import com.zerofive.store.cart.domain.entity.Cart;
import com.zerofive.store.core.port.ProductPort;

import java.util.List;

public record CartResult(
        Long id,
        List<CartItemResult> items,
        int totalPrice
) {

    public static CartResult from(Cart cart, ProductPort productPort) {
        List<CartItemResult> items = cart.getItems().stream()
                .map(item -> CartItemResult.from(item, productPort.getProduct(item.getProductId())))
                .toList();

        int totalPrice = items.stream()
                .mapToInt(item -> item.productPrice() * item.quantity())
                .sum();

        return new CartResult(cart.getId(), items, totalPrice);
    }
}
