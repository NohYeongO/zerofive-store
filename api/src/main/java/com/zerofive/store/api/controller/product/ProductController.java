package com.zerofive.store.api.controller.product;

import com.zerofive.store.api.controller.product.response.ProductResponse;
import com.zerofive.store.core.response.ApiResponse;
import com.zerofive.store.core.response.PageResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Product", description = "상품 API")
@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Operation(summary = "상품 목록 조회 (페이지네이션)")
    @GetMapping
    public ApiResponse<PageResponse<ProductResponse>> getProducts(
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size,
            @Parameter(description = "카테고리") @RequestParam(required = false) String category) {

        List<ProductResponse> products = List.of(
                new ProductResponse(1L, "무선 이어폰", 59000, 100, "ELECTRONICS"),
                new ProductResponse(2L, "스마트 워치", 189000, 50, "ELECTRONICS")
        );
        PageResponse<ProductResponse> mock = new PageResponse<>(products, page, size, 2, 1);
        return ApiResponse.ok(mock);
    }

    @Operation(summary = "상품 상세 조회")
    @GetMapping("/{productId}")
    public ApiResponse<ProductResponse> getProduct(
            @Parameter(description = "상품 ID") @PathVariable Long productId) {

        ProductResponse mock = new ProductResponse(productId, "무선 이어폰", 59000, 100, "ELECTRONICS");
        return ApiResponse.ok(mock);
    }

    @Operation(summary = "인기 상품 조회")
    @GetMapping("/popular")
    public ApiResponse<List<ProductResponse>> getPopularProducts() {
        List<ProductResponse> mock = List.of(
                new ProductResponse(1L, "무선 이어폰", 59000, 100, "ELECTRONICS"),
                new ProductResponse(3L, "프리미엄 텀블러", 32000, 200, "LIFESTYLE"),
                new ProductResponse(5L, "기계식 키보드", 129000, 30, "ELECTRONICS")
        );
        return ApiResponse.ok(mock);
    }

    @Operation(summary = "상품 검색")
    @GetMapping("/search")
    public ApiResponse<PageResponse<ProductResponse>> searchProducts(
            @Parameter(description = "검색 키워드") @RequestParam String keyword,
            @Parameter(description = "페이지 번호") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "페이지 크기") @RequestParam(defaultValue = "20") int size) {

        List<ProductResponse> products = List.of(
                new ProductResponse(1L, "무선 이어폰", 59000, 100, "ELECTRONICS")
        );
        PageResponse<ProductResponse> mock = new PageResponse<>(products, page, size, 1, 1);
        return ApiResponse.ok(mock);
    }
}
