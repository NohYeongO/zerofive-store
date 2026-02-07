package com.zerofive.store.api.controller.cart;

import com.zerofive.store.api.controller.cart.request.CartItemAddRequest;
import com.zerofive.store.api.controller.cart.request.CartItemUpdateRequest;
import com.zerofive.store.api.controller.cart.response.CartItemResponse;
import com.zerofive.store.api.controller.cart.response.CartResponse;
import com.zerofive.store.core.response.ApiResult;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Cart", description = "장바구니 API")
@RestController
@RequestMapping("/api/carts")
public class CartController {

    private static final CartResponse MOCK_CART = new CartResponse(
            1L,
            List.of(
                    new CartItemResponse(1L, 1L, "무선 이어폰", 59000, 2),
                    new CartItemResponse(2L, 3L, "프리미엄 텀블러", 32000, 1)
            ),
            150000
    );

    @Operation(summary = "장바구니 조회")
    @GetMapping
    public ApiResult<CartResponse> getCart() {
        return ApiResult.ok(MOCK_CART);
    }

    @Operation(summary = "장바구니 상품 추가")
    @PostMapping("/items")
    public ApiResult<CartResponse> addItem(@RequestBody @Valid CartItemAddRequest request) {
        return ApiResult.ok(MOCK_CART);
    }

    @Operation(summary = "장바구니 상품 수량 변경")
    @PatchMapping("/items/{cartItemId}")
    public ApiResult<CartResponse> updateItem(
            @Parameter(description = "장바구니 항목 ID") @PathVariable Long cartItemId,
            @RequestBody @Valid CartItemUpdateRequest request) {
        return ApiResult.ok(MOCK_CART);
    }

    @Operation(summary = "장바구니 상품 삭제")
    @DeleteMapping("/items/{cartItemId}")
    public ApiResult<Void> deleteItem(
            @Parameter(description = "장바구니 항목 ID") @PathVariable Long cartItemId) {
        return ApiResult.ok(null);
    }
}
