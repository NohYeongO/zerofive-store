package com.zerofive.store.order.infra;

import com.zerofive.store.core.MysqlRedisTestContainer;
import com.zerofive.store.order.domain.entity.OrderSession;
import com.zerofive.store.order.domain.entity.OrderSessionItem;
import com.zerofive.store.order.infra.repository.OrderSessionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class OrderSessionRepositoryTest extends MysqlRedisTestContainer {

    @Autowired
    private OrderSessionRepository orderSessionRepository;

    @Test
    @DisplayName("주문 세션 저장 및 조회 테스트")
    void saveAndRetrieveOrderSession() {
        // given
        Long accountId = 1L;
        String sessionId = accountId + "_" + System.currentTimeMillis();

        OrderSession session = OrderSession.builder()
                .sessionId(sessionId)
                .accountId(accountId)
                .build();

        OrderSessionItem item1 = OrderSessionItem.builder()
                .productId(1L)
                .productName("무선 이어폰")
                .price(59000)
                .quantity(2)
                .build();

        OrderSessionItem item2 = OrderSessionItem.builder()
                .productId(2L)
                .productName("프리미엄 텀블러")
                .price(32000)
                .quantity(1)
                .build();

        session.addItem(item1);
        session.addItem(item2);

        // when
        orderSessionRepository.save(session);

        Optional<OrderSession> found = orderSessionRepository.findBySessionIdWithItems(sessionId);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getSessionId()).isEqualTo(sessionId);
        assertThat(found.get().getAccountId()).isEqualTo(accountId);
        assertThat(found.get().getItems()).hasSize(2);
    }

    @Test
    @DisplayName("세션 폴링 시간 업데이트 테스트")
    void updatePolledAt() {
        // given
        Long accountId = 1L;
        String sessionId = accountId + "_" + System.currentTimeMillis();

        OrderSession session = OrderSession.builder()
                .sessionId(sessionId)
                .accountId(accountId)
                .build();

        orderSessionRepository.save(session);

        LocalDateTime beforePoll = session.getLastPolledAt();

        // when
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        session.updatePolledAt();
        orderSessionRepository.save(session);

        OrderSession updated = orderSessionRepository.findBySessionIdWithItems(sessionId).orElseThrow();

        // then
        assertThat(updated.getLastPolledAt()).isAfter(beforePoll);
    }

    @Test
    @DisplayName("만료된 세션 조회 테스트")
    void findExpiredSessions() {
        // given
        Long accountId = 1L;
        OrderSession activeSession = OrderSession.builder()
                .sessionId(accountId + "_" + System.currentTimeMillis())
                .accountId(accountId)
                .build();

        orderSessionRepository.save(activeSession);

        // when
        LocalDateTime cutoffTime = LocalDateTime.now().minusSeconds(30);
        List<OrderSession> expiredSessions = orderSessionRepository.findByLastPolledAtBefore(cutoffTime);

        // then
        assertThat(expiredSessions).isEmpty();
    }
}
