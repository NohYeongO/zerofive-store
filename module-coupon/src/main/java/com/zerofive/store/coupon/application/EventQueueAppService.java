package com.zerofive.store.coupon.application;

import com.zerofive.store.coupon.application.dto.CouponIssueResult;
import com.zerofive.store.coupon.application.dto.QueueEntryResult;
import com.zerofive.store.coupon.application.dto.QueueStatusResult;
import com.zerofive.store.coupon.domain.CouponService;
import com.zerofive.store.coupon.infra.redis.EventCouponRedisRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventQueueAppService {

    private final CouponService couponService;
    private final EventCouponRedisRepository redisRepository;

    public QueueEntryResult enterQueue(Long eventId, Long accountId) {
        var result = redisRepository.enterQueue(eventId, accountId);
        return QueueEntryResult.from(result);
    }

    public QueueStatusResult getQueueStatus(Long eventId, Long accountId) {
        var result = redisRepository.getQueueStatus(eventId, accountId);
        return QueueStatusResult.from(result);
    }

    public CouponIssueResult requestIssue(Long couponId, Long accountId) {
        // 1. Redis 멱등성 체크 (O(1) - 악성 유저 빠른 차단)
        if (redisRepository.isAlreadyIssued(couponId, accountId)) {
            return CouponIssueResult.duplicate();
        }

        // 2. 재고 차감
        String result = redisRepository.decrementStock(couponId);
        if ("SOLD_OUT".equals(result)) {
            return CouponIssueResult.soldOut();
        }

        // 3. DB 저장
        try {
            couponService.issueEventCoupon(couponId, accountId);
            redisRepository.markAsIssued(couponId, accountId);
            return CouponIssueResult.success();
        } catch (DataIntegrityViolationException e) {
            // 4. DB 중복 감지 (Redis 체크 통과한 동시 요청의 안전장치)
            log.warn("중복 발급 시도: couponId={}, accountId={}", couponId, accountId);
            recoverStock(couponId);
            redisRepository.markAsIssued(couponId, accountId);
            return CouponIssueResult.duplicate();
        } catch (Exception e) {
            log.error("쿠폰 발급 실패: couponId={}, accountId={}", couponId, accountId, e);
            recoverStock(couponId);
            throw e;
        }
    }

    private void recoverStock(Long couponId) {
        try {
            redisRepository.incrementStock(couponId);
        } catch (Exception e) {
            log.error("Redis 재고 복구 실패: couponId={}", couponId, e);
        }
    }
}
