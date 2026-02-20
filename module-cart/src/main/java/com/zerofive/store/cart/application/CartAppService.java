package com.zerofive.store.cart.application;

import com.zerofive.store.cart.application.dto.CartResult;
import com.zerofive.store.cart.domain.entity.Cart;
import com.zerofive.store.cart.domain.entity.CartItem;
import com.zerofive.store.cart.infra.repository.CartItemRepository;
import com.zerofive.store.cart.infra.repository.CartRepository;
import com.zerofive.store.core.exception.NotFoundException;
import com.zerofive.store.core.port.ProductPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartAppService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductPort productPort;

    @Transactional(readOnly = true)
    public CartResult getCart(Long accountId) {
        Cart cart = cartRepository.findByAccountIdWithItems(accountId)
                .orElseGet(() -> new Cart(accountId));

        return CartResult.from(cart, productPort);
    }

    @Transactional
    public CartResult addItem(Long accountId, Long productId, int quantity) {
        Cart cart = cartRepository.findByAccountId(accountId)
                .orElseGet(() -> cartRepository.save(new Cart(accountId)));

        CartItem existingItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElse(null);

        if (existingItem != null) {
            existingItem.updateQuantity(existingItem.getQuantity() + quantity);
        } else {
            CartItem newItem = new CartItem(productId, quantity);
            cart.addItem(newItem);
        }

        return CartResult.from(cart, productPort);
    }

    @Transactional
    public CartResult updateItemQuantity(Long accountId, Long cartItemId, int quantity) {
        Cart cart = cartRepository.findByAccountIdWithItems(accountId)
                .orElseThrow(() -> new NotFoundException("장바구니를 찾을 수 없습니다."));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("장바구니 항목을 찾을 수 없습니다."));

        item.updateQuantity(quantity);

        return CartResult.from(cart, productPort);
    }

    @Transactional
    public void deleteItem(Long accountId, Long cartItemId) {
        Cart cart = cartRepository.findByAccountIdWithItems(accountId)
                .orElseThrow(() -> new NotFoundException("장바구니를 찾을 수 없습니다."));

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("장바구니 항목을 찾을 수 없습니다."));

        cart.removeItem(item);
    }
}
