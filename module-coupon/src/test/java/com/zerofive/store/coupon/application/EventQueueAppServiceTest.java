package com.zerofive.store.coupon.application;

import com.zerofive.store.core.exception.NotFoundException;
import com.zerofive.store.coupon.application.dto.CouponIssueResult;
import com.zerofive.store.coupon.application.dto.QueueStatus;
import com.zerofive.store.coupon.domain.CouponService;
import com.zerofive.store.coupon.infra.redis.EventCouponRedisRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EventQueueAppServiceTest {

    @InjectMocks
    private EventQueueAppService eventQueueAppService;

    @Mock
    private CouponService couponService;

    @Mock
    private EventCouponRedisRepository redisRepository;

    private static final Long COUPON_ID = 1L;
    private static final Long ACCOUNT_ID = 100L;

    @Nested
    @DisplayName("requestIssue")
    class RequestIssue {

        @Test
        @DisplayName("재고가 있으면 쿠폰을 발급한다")
        void success() {
            // given
            given(redisRepository.isAlreadyIssued(COUPON_ID, ACCOUNT_ID)).willReturn(false);
            given(redisRepository.decrementStock(COUPON_ID)).willReturn("SUCCESS");

            // when
            CouponIssueResult result = eventQueueAppService.requestIssue(COUPON_ID, ACCOUNT_ID);

            // then
            assertThat(result.status()).isEqualTo(QueueStatus.SUCCESS);
            verify(couponService).issueEventCoupon(COUPON_ID, ACCOUNT_ID);
            verify(redisRepository).markAsIssued(COUPON_ID, ACCOUNT_ID);
            verify(redisRepository, never()).incrementStock(COUPON_ID);
        }

        @Test
        @DisplayName("재고가 없으면 SOLD_OUT을 반환한다")
        void soldOut() {
            // given
            given(redisRepository.isAlreadyIssued(COUPON_ID, ACCOUNT_ID)).willReturn(false);
            given(redisRepository.decrementStock(COUPON_ID)).willReturn("SOLD_OUT");

            // when
            CouponIssueResult result = eventQueueAppService.requestIssue(COUPON_ID, ACCOUNT_ID);

            // then
            assertThat(result.status()).isEqualTo(QueueStatus.SOLD_OUT);
            verify(couponService, never()).issueEventCoupon(COUPON_ID, ACCOUNT_ID);
            verify(redisRepository, never()).incrementStock(COUPON_ID);
        }

        @Test
        @DisplayName("이미 발급된 쿠폰이면 DUPLICATE를 반환한다")
        void alreadyIssued() {
            // given
            given(redisRepository.isAlreadyIssued(COUPON_ID, ACCOUNT_ID)).willReturn(true);

            // when
            CouponIssueResult result = eventQueueAppService.requestIssue(COUPON_ID, ACCOUNT_ID);

            // then
            assertThat(result.status()).isEqualTo(QueueStatus.DUPLICATE);
            verify(redisRepository, never()).decrementStock(COUPON_ID);
            verify(couponService, never()).issueEventCoupon(COUPON_ID, ACCOUNT_ID);
        }

        @Test
        @DisplayName("DB 중복 발급 시 DUPLICATE를 반환하고 재고를 복구한다")
        void duplicate() {
            // given
            given(redisRepository.isAlreadyIssued(COUPON_ID, ACCOUNT_ID)).willReturn(false);
            given(redisRepository.decrementStock(COUPON_ID)).willReturn("SUCCESS");
            willThrow(new DataIntegrityViolationException("Duplicate entry"))
                    .given(couponService).issueEventCoupon(COUPON_ID, ACCOUNT_ID);

            // when
            CouponIssueResult result = eventQueueAppService.requestIssue(COUPON_ID, ACCOUNT_ID);

            // then
            assertThat(result.status()).isEqualTo(QueueStatus.DUPLICATE);
            verify(redisRepository).incrementStock(COUPON_ID);
            verify(redisRepository).markAsIssued(COUPON_ID, ACCOUNT_ID);
        }

        @Test
        @DisplayName("쿠폰이 존재하지 않으면 예외를 던지고 재고를 복구한다")
        void couponNotFound() {
            // given
            given(redisRepository.isAlreadyIssued(COUPON_ID, ACCOUNT_ID)).willReturn(false);
            given(redisRepository.decrementStock(COUPON_ID)).willReturn("SUCCESS");
            willThrow(new NotFoundException("쿠폰을 찾을 수 없습니다: " + COUPON_ID))
                    .given(couponService).issueEventCoupon(COUPON_ID, ACCOUNT_ID);

            // when & then
            assertThatThrownBy(() -> eventQueueAppService.requestIssue(COUPON_ID, ACCOUNT_ID))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessageContaining("쿠폰을 찾을 수 없습니다");
            verify(redisRepository).incrementStock(COUPON_ID);
        }

        @Test
        @DisplayName("DB 저장 중 예외 발생 시 재고를 복구하고 예외를 던진다")
        void dbError() {
            // given
            given(redisRepository.isAlreadyIssued(COUPON_ID, ACCOUNT_ID)).willReturn(false);
            given(redisRepository.decrementStock(COUPON_ID)).willReturn("SUCCESS");
            willThrow(new RuntimeException("DB error"))
                    .given(couponService).issueEventCoupon(COUPON_ID, ACCOUNT_ID);

            // when & then
            assertThatThrownBy(() -> eventQueueAppService.requestIssue(COUPON_ID, ACCOUNT_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("DB error");
            verify(redisRepository).incrementStock(COUPON_ID);
        }
    }
}
