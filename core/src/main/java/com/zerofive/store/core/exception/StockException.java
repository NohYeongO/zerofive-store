package com.zerofive.store.core.exception;

import lombok.Getter;

@Getter
public class StockException extends RuntimeException {

    private final String errorCode;
    private final Long productId;

    public StockException(String errorCode, String message, Long productId) {
        super(message);
        this.errorCode = errorCode;
        this.productId = productId;
    }

    public static StockException insufficientStock(Long productId) {
        return new StockException("STOCK_001", "재고가 부족합니다.", productId);
    }

    public static StockException productNotFound(Long productId) {
        return new StockException("STOCK_002", "상품을 찾을 수 없습니다.", productId);
    }
}
