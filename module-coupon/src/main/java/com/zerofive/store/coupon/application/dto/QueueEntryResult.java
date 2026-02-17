package com.zerofive.store.coupon.application.dto;

import com.zerofive.store.coupon.infra.redis.EventCouponRedisRepository;

public record QueueEntryResult(
        QueueStatus status,
        Long position
) {
    public static QueueEntryResult from(EventCouponRedisRepository.QueueEntryResult result) {
        QueueStatus status = "ADMITTED".equals(result.status())
                ? QueueStatus.ADMITTED
                : QueueStatus.QUEUED;
        return new QueueEntryResult(status, result.position());
    }
}
