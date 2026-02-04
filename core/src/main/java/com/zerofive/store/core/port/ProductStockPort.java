package com.zerofive.store.core.port;

public interface ProductStockPort {

    boolean checkStock(Long productId, int quantity);

    void deductStock(Long productId, int quantity);
}
