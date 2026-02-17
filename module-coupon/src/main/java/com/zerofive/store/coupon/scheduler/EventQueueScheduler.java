package com.zerofive.store.coupon.scheduler;

import com.zerofive.store.coupon.infra.redis.EventCouponRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventQueueScheduler {

    private final EventCouponRedisRepository redisRepository;

    @Value("${coupon.queue.heartbeat-timeout:10}")
    private int heartbeatTimeoutSeconds;

    @Scheduled(fixedDelayString = "${coupon.queue.scheduler-delay:1000}")
    public void processAdmission() {
        Set<Long> activeEvents = redisRepository.getActiveEvents();
        if (activeEvents.isEmpty()) {
            return;
        }

        for (Long eventId : activeEvents) {
            try {
                processAdmissionForEvent(eventId);
            } catch (Exception e) {
                log.error("입장 처리 실패: eventId={}", eventId, e);
            }
        }
    }

    private void processAdmissionForEvent(Long eventId) {
        int threshold = redisRepository.getThreshold(eventId).orElse(200);
        long currentAdmitted = redisRepository.getAdmittedCount(eventId);
        int availableSlots = (int) (threshold - currentAdmitted);

        if (availableSlots <= 0) {
            return;
        }

        Set<String> candidates = redisRepository.getQueueCandidates(eventId, availableSlots);
        if (candidates == null || candidates.isEmpty()) {
            return;
        }

        for (String accountId : candidates) {
            redisRepository.admitFromQueue(eventId, accountId);
        }

        log.info("입장 처리 완료: eventId={}, admittedCount={}", eventId, candidates.size());
    }

    @Scheduled(fixedDelayString = "${coupon.queue.scheduler-delay:1000}")
    public void cleanupInactiveAdmitted() {
        Set<Long> activeEvents = redisRepository.getActiveEvents();
        if (activeEvents.isEmpty()) {
            return;
        }

        for (Long eventId : activeEvents) {
            try {
                cleanupInactiveForEvent(eventId);
            } catch (Exception e) {
                log.error("이탈자 정리 실패: eventId={}", eventId, e);
            }
        }
    }

    private void cleanupInactiveForEvent(Long eventId) {
        Map<Object, Object> admittedEntries = redisRepository.getAdmittedEntries(eventId);
        if (admittedEntries.isEmpty()) {
            return;
        }

        long now = System.currentTimeMillis();
        long timeoutMillis = heartbeatTimeoutSeconds * 1000L;
        int removedCount = 0;

        for (Map.Entry<Object, Object> entry : admittedEntries.entrySet()) {
            String accountId = (String) entry.getKey();
            long lastPollingTime = Long.parseLong((String) entry.getValue());

            if (now - lastPollingTime > timeoutMillis) {
                redisRepository.removeFromAdmitted(eventId, accountId);
                removedCount++;
            }
        }

        if (removedCount > 0) {
            log.info("이탈자 정리 완료: eventId={}, removedCount={}", eventId, removedCount);
        }
    }
}
