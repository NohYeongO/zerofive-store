package com.zerofive.store.api.controller.cart;

import com.zerofive.store.api.controller.cart.request.CartItemAddRequest;
import com.zerofive.store.api.controller.cart.request.CartItemUpdateRequest;
import com.zerofive.store.api.controller.cart.response.CartItemResponse;
import com.zerofive.store.api.controller.cart.response.CartResponse;
import com.zerofive.store.cart.application.CartAppService;
import com.zerofive.store.cart.application.dto.CartResult;
import com.zerofive.store.core.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Cart", description = "장바구니 API")
@RestController
@RequestMapping("/api/carts")
@RequiredArgsConstructor
public class CartController {

    private final CartAppService cartAppService;

    @Operation(summary = "장바구니 조회")
    @GetMapping
    public ApiResult<CartResponse> getCart(@AuthenticationPrincipal Long accountId) {
        CartResult result = cartAppService.getCart(accountId);
        return ApiResult.ok(toResponse(result));
    }

    @Operation(summary = "장바구니 상품 추가")
    @PostMapping("/items")
    public ApiResult<CartResponse> addItem(
            @AuthenticationPrincipal Long accountId,
            @RequestBody @Valid CartItemAddRequest request) {
        CartResult result = cartAppService.addItem(accountId, request.productId(), request.quantity());
        return ApiResult.ok(toResponse(result));
    }

    @Operation(summary = "장바구니 상품 수량 변경")
    @PatchMapping("/items/{cartItemId}")
    public ApiResult<CartResponse> updateItem(
            @AuthenticationPrincipal Long accountId,
            @Parameter(description = "장바구니 항목 ID") @PathVariable Long cartItemId,
            @RequestBody @Valid CartItemUpdateRequest request) {
        CartResult result = cartAppService.updateItemQuantity(accountId, cartItemId, request.quantity());
        return ApiResult.ok(toResponse(result));
    }

    @Operation(summary = "장바구니 상품 삭제")
    @DeleteMapping("/items/{cartItemId}")
    public ApiResult<Void> deleteItem(
            @AuthenticationPrincipal Long accountId,
            @Parameter(description = "장바구니 항목 ID") @PathVariable Long cartItemId) {
        cartAppService.deleteItem(accountId, cartItemId);
        return ApiResult.ok(null);
    }

    private CartResponse toResponse(CartResult result) {
        return new CartResponse(
                result.id(),
                result.items().stream()
                        .map(item -> new CartItemResponse(
                                item.id(),
                                item.productId(),
                                item.productName(),
                                item.productPrice(),
                                item.quantity()
                        ))
                        .toList(),
                result.totalPrice()
        );
    }
}
