package com.zerofive.store.order.scheduler;

import com.zerofive.store.core.port.ProductStockPort;
import com.zerofive.store.order.domain.entity.OrderSession;
import com.zerofive.store.order.infra.repository.OrderSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderSessionScheduler {

    private final OrderSessionRepository orderSessionRepository;
    private final ProductStockPort productStockPort;

    @Scheduled(fixedRate = 30000)
    @Transactional
    public void cleanupExpiredSessions() {
        LocalDateTime expiredTime = LocalDateTime.now().minusMinutes(1);
        List<OrderSession> expiredSessions = orderSessionRepository.findByLastPolledAtBefore(expiredTime);

        if (expiredSessions.isEmpty()) {
            return;
        }

        for (OrderSession session : expiredSessions) {
            List<ProductStockPort.StockItem> stockItems = session.getItems().stream()
                    .map(item -> new ProductStockPort.StockItem(item.getProductId(), item.getQuantity()))
                    .toList();

            productStockPort.restoreStock(stockItems);
            log.info("재고 복구 요청 완료: sessionId={}", session.getSessionId());

            orderSessionRepository.delete(session);
            log.info("세션 삭제: sessionId={}", session.getSessionId());
        }

        log.info("만료된 세션 정리 완료");
    }
}
