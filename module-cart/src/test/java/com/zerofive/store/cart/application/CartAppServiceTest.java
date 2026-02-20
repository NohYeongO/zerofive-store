package com.zerofive.store.cart.application;

import com.zerofive.store.cart.application.dto.CartResult;
import com.zerofive.store.cart.domain.entity.Cart;
import com.zerofive.store.cart.domain.entity.CartItem;
import com.zerofive.store.cart.infra.repository.CartItemRepository;
import com.zerofive.store.cart.infra.repository.CartRepository;
import com.zerofive.store.core.exception.NotFoundException;
import com.zerofive.store.core.port.ProductPort;
import com.zerofive.store.core.port.ProductPort.ProductInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartAppServiceTest {

    @InjectMocks
    private CartAppService cartAppService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private ProductPort productPort;

    private static final Long ACCOUNT_ID = 1L;
    private static final Long PRODUCT_ID = 100L;
    private static final Long CART_ID = 10L;
    private static final Long CART_ITEM_ID = 1000L;

    @Nested
    @DisplayName("getCart")
    class GetCart {

        @Test
        @DisplayName("장바구니가 있으면 장바구니를 반환한다")
        void existingCart() {
            // given
            Cart cart = createCartWithItem();
            given(cartRepository.findByAccountIdWithItems(ACCOUNT_ID)).willReturn(Optional.of(cart));
            given(productPort.getProduct(PRODUCT_ID)).willReturn(new ProductInfo(PRODUCT_ID, "테스트 상품", 10000, 100));

            // when
            CartResult result = cartAppService.getCart(ACCOUNT_ID);

            // then
            assertThat(result.id()).isEqualTo(CART_ID);
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).productName()).isEqualTo("테스트 상품");
            assertThat(result.totalPrice()).isEqualTo(20000);
        }

        @Test
        @DisplayName("장바구니가 없으면 빈 장바구니를 반환한다")
        void emptyCart() {
            // given
            given(cartRepository.findByAccountIdWithItems(ACCOUNT_ID)).willReturn(Optional.empty());

            // when
            CartResult result = cartAppService.getCart(ACCOUNT_ID);

            // then
            assertThat(result.id()).isNull();
            assertThat(result.items()).isEmpty();
            assertThat(result.totalPrice()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("addItem")
    class AddItem {

        @Test
        @DisplayName("새 상품을 장바구니에 추가한다")
        void addNewItem() {
            // given
            Cart cart = createEmptyCart();
            given(cartRepository.findByAccountId(ACCOUNT_ID)).willReturn(Optional.of(cart));
            given(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID)).willReturn(Optional.empty());
            given(productPort.getProduct(PRODUCT_ID)).willReturn(new ProductInfo(PRODUCT_ID, "테스트 상품", 10000, 100));

            // when
            CartResult result = cartAppService.addItem(ACCOUNT_ID, PRODUCT_ID, 2);

            // then
            assertThat(result.items()).hasSize(1);
            assertThat(result.items().get(0).quantity()).isEqualTo(2);
        }

        @Test
        @DisplayName("이미 있는 상품이면 수량을 증가시킨다")
        void addExistingItem() {
            // given
            Cart cart = createCartWithItem();
            CartItem existingItem = cart.getItems().get(0);
            given(cartRepository.findByAccountId(ACCOUNT_ID)).willReturn(Optional.of(cart));
            given(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID)).willReturn(Optional.of(existingItem));
            given(productPort.getProduct(PRODUCT_ID)).willReturn(new ProductInfo(PRODUCT_ID, "테스트 상품", 10000, 100));

            // when
            CartResult result = cartAppService.addItem(ACCOUNT_ID, PRODUCT_ID, 3);

            // then
            assertThat(existingItem.getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("장바구니가 없으면 새로 생성한다")
        void createNewCart() {
            // given
            Cart newCart = createEmptyCart();
            given(cartRepository.findByAccountId(ACCOUNT_ID)).willReturn(Optional.empty());
            given(cartRepository.save(any(Cart.class))).willReturn(newCart);
            given(cartItemRepository.findByCartIdAndProductId(CART_ID, PRODUCT_ID)).willReturn(Optional.empty());
            given(productPort.getProduct(PRODUCT_ID)).willReturn(new ProductInfo(PRODUCT_ID, "테스트 상품", 10000, 100));

            // when
            CartResult result = cartAppService.addItem(ACCOUNT_ID, PRODUCT_ID, 1);

            // then
            verify(cartRepository).save(any(Cart.class));
        }
    }

    @Nested
    @DisplayName("updateItemQuantity")
    class UpdateItemQuantity {

        @Test
        @DisplayName("장바구니 항목 수량을 변경한다")
        void updateQuantity() {
            // given
            Cart cart = createCartWithItem();
            given(cartRepository.findByAccountIdWithItems(ACCOUNT_ID)).willReturn(Optional.of(cart));
            given(productPort.getProduct(PRODUCT_ID)).willReturn(new ProductInfo(PRODUCT_ID, "테스트 상품", 10000, 100));

            // when
            CartResult result = cartAppService.updateItemQuantity(ACCOUNT_ID, CART_ITEM_ID, 5);

            // then
            assertThat(cart.getItems().get(0).getQuantity()).isEqualTo(5);
        }

        @Test
        @DisplayName("장바구니가 없으면 예외를 던진다")
        void cartNotFound() {
            // given
            given(cartRepository.findByAccountIdWithItems(ACCOUNT_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartAppService.updateItemQuantity(ACCOUNT_ID, CART_ITEM_ID, 5))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("장바구니를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("장바구니 항목이 없으면 예외를 던진다")
        void itemNotFound() {
            // given
            Cart cart = createEmptyCart();
            given(cartRepository.findByAccountIdWithItems(ACCOUNT_ID)).willReturn(Optional.of(cart));

            // when & then
            assertThatThrownBy(() -> cartAppService.updateItemQuantity(ACCOUNT_ID, CART_ITEM_ID, 5))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("장바구니 항목을 찾을 수 없습니다");
        }
    }

    @Nested
    @DisplayName("deleteItem")
    class DeleteItem {

        @Test
        @DisplayName("장바구니 항목을 삭제한다")
        void deleteItem() {
            // given
            Cart cart = createCartWithItem();
            given(cartRepository.findByAccountIdWithItems(ACCOUNT_ID)).willReturn(Optional.of(cart));

            // when
            cartAppService.deleteItem(ACCOUNT_ID, CART_ITEM_ID);

            // then
            assertThat(cart.getItems()).isEmpty();
        }

        @Test
        @DisplayName("장바구니가 없으면 예외를 던진다")
        void cartNotFound() {
            // given
            given(cartRepository.findByAccountIdWithItems(ACCOUNT_ID)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> cartAppService.deleteItem(ACCOUNT_ID, CART_ITEM_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("장바구니를 찾을 수 없습니다");
        }

        @Test
        @DisplayName("장바구니 항목이 없으면 예외를 던진다")
        void itemNotFound() {
            // given
            Cart cart = createEmptyCart();
            given(cartRepository.findByAccountIdWithItems(ACCOUNT_ID)).willReturn(Optional.of(cart));

            // when & then
            assertThatThrownBy(() -> cartAppService.deleteItem(ACCOUNT_ID, CART_ITEM_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("장바구니 항목을 찾을 수 없습니다");
        }
    }

    private Cart createEmptyCart() {
        Cart cart = new Cart(ACCOUNT_ID);
        ReflectionTestUtils.setField(cart, "id", CART_ID);
        return cart;
    }

    private Cart createCartWithItem() {
        Cart cart = createEmptyCart();
        CartItem item = new CartItem(PRODUCT_ID, 2);
        ReflectionTestUtils.setField(item, "id", CART_ITEM_ID);
        cart.addItem(item);
        return cart;
    }
}
