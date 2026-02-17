package com.zerofive.store.coupon.infra.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class EventCouponRedisRepository {

    private final StringRedisTemplate queueRedis;
    private final StringRedisTemplate issueRedis;

    private final DefaultRedisScript<List> enterQueueScript;
    private final DefaultRedisScript<List> getQueueStatusScript;
    private final DefaultRedisScript<String> requestIssueScript;

    private static final String ACTIVE_EVENTS_KEY = "queue:active:events";
    private static final String ACTIVE_COUPONS_KEY = "queue:active:coupons";

    public EventCouponRedisRepository(
            StringRedisTemplate queueRedisTemplate,
            @Qualifier("issueRedisTemplate") StringRedisTemplate issueRedisTemplate,
            @Qualifier("enterQueueScript") DefaultRedisScript<List> enterQueueScript,
            @Qualifier("getQueueStatusScript") DefaultRedisScript<List> getQueueStatusScript,
            @Qualifier("requestIssueScript") DefaultRedisScript<String> requestIssueScript) {
        this.queueRedis = queueRedisTemplate;
        this.issueRedis = issueRedisTemplate;
        this.enterQueueScript = enterQueueScript;
        this.getQueueStatusScript = getQueueStatusScript;
        this.requestIssueScript = requestIssueScript;
    }

    public QueueEntryResult enterQueue(Long eventId, Long accountId) {
        var keys = EventKey.of(eventId);
        var result = queueRedis.execute(
                enterQueueScript,
                List.of(keys.queue(), keys.admitted()),
                accountId.toString(),
                String.valueOf(System.currentTimeMillis())
        );
        return QueueEntryResult.from(result);
    }

    public QueueStatusResult getQueueStatus(Long eventId, Long accountId) {
        var keys = EventKey.of(eventId);
        var result = queueRedis.execute(
                getQueueStatusScript,
                List.of(keys.queue(), keys.admitted()),
                accountId.toString()
        );
        return QueueStatusResult.from(result);
    }

    public Optional<Integer> getThreshold(Long eventId) {
        return Optional.ofNullable(queueRedis.opsForValue().get(EventKey.of(eventId).threshold()))
                .map(Integer::parseInt);
    }

    public void setThreshold(Long eventId, int threshold) {
        queueRedis.opsForValue().set(EventKey.of(eventId).threshold(), String.valueOf(threshold));
    }

    public long getAdmittedCount(Long eventId) {
        return Optional.of(queueRedis.opsForHash().size(EventKey.of(eventId).admitted()))
                .orElse(0L);
    }

    public Set<String> getQueueCandidates(Long eventId, int count) {
        return queueRedis.opsForZSet().range(EventKey.of(eventId).queue(), 0, count - 1);
    }

    public void admitFromQueue(Long eventId, String accountId) {
        var keys = EventKey.of(eventId);
        queueRedis.opsForHash().put(keys.admitted(), accountId, String.valueOf(System.currentTimeMillis()));
        queueRedis.opsForZSet().remove(keys.queue(), accountId);
    }

    public Map<Object, Object> getAdmittedEntries(Long eventId) {
        return queueRedis.opsForHash().entries(EventKey.of(eventId).admitted());
    }

    public void removeFromAdmitted(Long eventId, String accountId) {
        queueRedis.opsForHash().delete(EventKey.of(eventId).admitted(), accountId);
    }

    public void addActiveEvent(Long eventId) {
        queueRedis.opsForSet().add(ACTIVE_EVENTS_KEY, eventId.toString());
    }

    public void removeActiveEvent(Long eventId) {
        queueRedis.opsForSet().remove(ACTIVE_EVENTS_KEY, eventId.toString());
    }

    public Set<Long> getActiveEvents() {
        Set<String> members = queueRedis.opsForSet().members(ACTIVE_EVENTS_KEY);
        if (members == null || members.isEmpty()) {
            return Collections.emptySet();
        }
        return members.stream()
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    public void addActiveCoupon(Long couponId) {
        issueRedis.opsForSet().add(ACTIVE_COUPONS_KEY, couponId.toString());
    }

    public void removeActiveCoupon(Long couponId) {
        issueRedis.opsForSet().remove(ACTIVE_COUPONS_KEY, couponId.toString());
    }

    public Set<Long> getActiveCoupons() {
        Set<String> members = issueRedis.opsForSet().members(ACTIVE_COUPONS_KEY);
        if (members == null || members.isEmpty()) {
            return Collections.emptySet();
        }
        return members.stream()
                .map(Long::parseLong)
                .collect(Collectors.toSet());
    }

    public void setStock(Long couponId, int stock) {
        issueRedis.opsForValue().set(CouponKey.of(couponId).stock(), String.valueOf(stock));
    }

    public String decrementStock(Long couponId) {
        var keys = CouponKey.of(couponId);
        return issueRedis.execute(
                requestIssueScript,
                List.of(keys.stock())
        );
    }

    public void incrementStock(Long couponId) {
        issueRedis.opsForValue().increment(CouponKey.of(couponId).stock());
    }

    public boolean isAlreadyIssued(Long couponId, Long accountId) {
        return Boolean.TRUE.equals(
                issueRedis.opsForSet().isMember(
                        CouponKey.of(couponId).issued(),
                        accountId.toString()
                )
        );
    }

    public void markAsIssued(Long couponId, Long accountId) {
        issueRedis.opsForSet().add(
                CouponKey.of(couponId).issued(),
                accountId.toString()
        );
    }

    private record EventKey(Long eventId) {
        static EventKey of(Long eventId) {
            return new EventKey(eventId);
        }

        String queue() {
            return "event:%d:queue".formatted(eventId);
        }

        String admitted() {
            return "event:%d:admitted".formatted(eventId);
        }

        String threshold() {
            return "event:%d:threshold".formatted(eventId);
        }
    }

    private record CouponKey(Long couponId) {
        static CouponKey of(Long couponId) {
            return new CouponKey(couponId);
        }

        String stock() {
            return "coupon:%d:stock".formatted(couponId);
        }

        String issued() {
            return "coupon:%d:issued".formatted(couponId);
        }
    }

    public record QueueEntryResult(String status, Long position) {
        static QueueEntryResult from(List<Object> result) {
            return new QueueEntryResult(
                    (String) result.get(0),
                    ((Number) result.get(1)).longValue()
            );
        }
    }

    public record QueueStatusResult(String status, Long position) {
        static QueueStatusResult from(List<Object> result) {
            return new QueueStatusResult(
                    (String) result.get(0),
                    ((Number) result.get(1)).longValue()
            );
        }
    }
}
