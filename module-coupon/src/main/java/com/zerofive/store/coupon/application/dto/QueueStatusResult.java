package com.zerofive.store.coupon.application.dto;

import com.zerofive.store.coupon.infra.redis.EventCouponRedisRepository;

public record QueueStatusResult(
        QueueStatus status,
        Long position
) {
    public static QueueStatusResult from(EventCouponRedisRepository.QueueStatusResult result) {
        QueueStatus status = switch (result.status()) {
            case "WAITING" -> QueueStatus.WAITING;
            case "ADMITTED" -> QueueStatus.ADMITTED;
            default -> QueueStatus.NOT_IN_QUEUE;
        };
        return new QueueStatusResult(status, result.position());
    }
}
