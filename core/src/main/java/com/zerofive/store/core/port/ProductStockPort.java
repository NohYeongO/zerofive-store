package com.zerofive.store.core.port;

import java.util.List;

public interface ProductStockPort {

    void deductStock(List<StockItem> items);

    void restoreStock(List<StockItem> items);

    record StockItem(Long productId, int quantity) {
    }
}
