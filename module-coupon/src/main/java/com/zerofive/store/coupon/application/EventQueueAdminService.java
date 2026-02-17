package com.zerofive.store.coupon.application;

import com.zerofive.store.core.exception.NotFoundException;
import com.zerofive.store.coupon.domain.entity.Event;
import com.zerofive.store.coupon.infra.redis.EventCouponRedisRepository;
import com.zerofive.store.coupon.infra.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventQueueAdminService {

    private final EventRepository eventRepository;
    private final EventCouponRedisRepository redisRepository;

    /**
     * 이벤트 대기열 활성화 - 스케줄러가 처리 시작
     */
    public void activateEventQueue(Long eventId) {
        Event event = getEvent(eventId);
        redisRepository.addActiveEvent(eventId);
        redisRepository.setThreshold(eventId, event.getThreshold());
        log.info("이벤트 대기열 활성화: eventId={}, threshold={}", eventId, event.getThreshold());
    }

    /**
     * 이벤트 대기열 비활성화 - 스케줄러가 처리 중단
     */
    public void deactivateEventQueue(Long eventId) {
        redisRepository.removeActiveEvent(eventId);
        log.info("이벤트 대기열 비활성화: eventId={}", eventId);
    }

    /**
     * 쿠폰 발급 처리 활성화
     */
    public void activateCouponIssue(Long couponId, int stock) {
        redisRepository.addActiveCoupon(couponId);
        redisRepository.setStock(couponId, stock);
        log.info("쿠폰 발급 활성화: couponId={}, stock={}", couponId, stock);
    }

    /**
     * 쿠폰 발급 처리 비활성화
     */
    public void deactivateCouponIssue(Long couponId) {
        redisRepository.removeActiveCoupon(couponId);
        log.info("쿠폰 발급 비활성화: couponId={}", couponId);
    }

    /**
     * 현재 활성화된 이벤트 목록 조회
     */
    public Set<Long> getActiveEvents() {
        return redisRepository.getActiveEvents();
    }

    /**
     * 현재 활성화된 쿠폰 목록 조회
     */
    public Set<Long> getActiveCoupons() {
        return redisRepository.getActiveCoupons();
    }

    /**
     * 입장 임계값 조회
     */
    public int getThreshold(Long eventId) {
        Event event = getEvent(eventId);
        return redisRepository.getThreshold(eventId)
                .orElse(event.getThreshold());
    }

    /**
     * 입장 임계값 변경
     */
    public void updateThreshold(Long eventId, int threshold) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("이벤트를 찾을 수 없습니다: " + eventId);
        }
        redisRepository.setThreshold(eventId, threshold);
        log.info("임계값 변경: eventId={}, threshold={}", eventId, threshold);
    }

    private Event getEvent(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("이벤트를 찾을 수 없습니다: " + eventId));
    }
}
