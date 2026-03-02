package com.zerofive.store.product.application;

import com.zerofive.store.core.exception.NotFoundException;
import com.zerofive.store.core.exception.StockException;
import com.zerofive.store.core.port.ProductStockPort;
import com.zerofive.store.product.domain.entity.Product;
import com.zerofive.store.product.infra.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductStockPortService implements ProductStockPort {

    private final ProductRepository productRepository;

    @Override
    @Transactional
    public void deductStock(List<StockItem> items) {
        for (StockItem item : items) {
           Product product = productRepository.findByIdWithLock(item.productId()).orElseThrow(() -> new NotFoundException(item.productId() + " 상품이 존재하지 않습니다"));

            try {
                product.deductStock(item.quantity());
                productRepository.save(product);
                log.info("재고 차감: productId={}, quantity={}, remainingStock={}",
                        product.getId(), item.quantity(), product.getStock());
            } catch (Exception e) {
                throw StockException.insufficientStock(product.getId());
            }
        }

        log.info("재고 차감 완료: {} 건", items.size());
    }

    @Override
    @Transactional
    public void restoreStock(List<StockItem> items) {
        List<StockItem> sortedItems = items.stream()
                .sorted(Comparator.comparing(StockItem::productId))
                .toList();

        for (StockItem item : sortedItems) {
            Product product = productRepository.findByIdWithLock(item.productId()).orElseThrow(() -> new NotFoundException(item.productId() + " 상품이 존재하지 않습니다"));
            product.restoreStock(item.quantity());
            productRepository.save(product);
        }

        log.info("재고 복구 완료: {} 건", items.size());
    }
}
