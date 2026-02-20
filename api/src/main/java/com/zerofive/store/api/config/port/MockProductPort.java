package com.zerofive.store.api.config.port;

import com.zerofive.store.core.port.ProductPort;
import org.springframework.stereotype.Component;

@Component
public class MockProductPort implements ProductPort {

    @Override
    public ProductInfo getProduct(Long productId) {
        return new ProductInfo(
                productId,
                "상품 " + productId,
                10000 * productId.intValue(),
                100
        );
    }
}
