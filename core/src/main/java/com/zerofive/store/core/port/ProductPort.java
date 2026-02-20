package com.zerofive.store.core.port;

public interface ProductPort {

    ProductInfo getProduct(Long productId);

    record ProductInfo(Long id, String name, int price, int stock) {
    }
}
