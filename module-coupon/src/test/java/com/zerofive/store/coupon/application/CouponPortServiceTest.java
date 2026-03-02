package com.zerofive.store.coupon.application;

import com.zerofive.store.core.MysqlRedisTestContainer;
import com.zerofive.store.core.port.CouponPort;
import com.zerofive.store.coupon.domain.entity.Coupon;
import com.zerofive.store.coupon.domain.entity.CouponType;
import com.zerofive.store.coupon.domain.entity.IssuedCoupon;
import com.zerofive.store.coupon.infra.repository.CouponRepository;
import com.zerofive.store.coupon.infra.repository.IssuedCouponRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(CouponPortService.class)
@DisplayName("쿠폰 관련 Repository 테스트")
class CouponPortServiceTest extends MysqlRedisTestContainer {

    @Autowired
    private CouponPortService couponPortService;

    @Autowired
    private CouponRepository couponRepository;

    @Autowired
    private IssuedCouponRepository issuedCouponRepository;

    @Test
    @DisplayName("사용 가능한 쿠폰 목록 조회 테스트")
    void getAvailableCoupons() {
        // given
        Long accountId = 1L;

        Coupon coupon1 = Coupon.builder()
                .name("신규 회원 할인")
                .discountAmount(5000)
                .couponType(CouponType.NORMAL)
                .totalQuantity(100)
                .issuedQuantity(10)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();

        Coupon coupon2 = Coupon.builder()
                .name("봄맞이 할인")
                .discountAmount(3000)
                .couponType(CouponType.NORMAL)
                .totalQuantity(50)
                .issuedQuantity(5)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();

        couponRepository.saveAll(List.of(coupon1, coupon2));

        IssuedCoupon issuedCoupon1 = IssuedCoupon.builder()
                .coupon(coupon1)
                .accountId(accountId)
                .build();

        IssuedCoupon issuedCoupon2 = IssuedCoupon.builder()
                .coupon(coupon2)
                .accountId(accountId)
                .build();

        issuedCouponRepository.saveAll(List.of(issuedCoupon1, issuedCoupon2));

        // when
        List<CouponPort.CouponInfo> coupons = couponPortService.getAvailableCoupons(accountId);

        // then
        assertThat(coupons).hasSize(2);
        assertThat(coupons.getFirst().couponName()).isEqualTo("신규 회원 할인");
        assertThat(coupons.getFirst().discountAmount()).isEqualTo(5000);
    }

    @Test
    @DisplayName("사용된 쿠폰은 조회되지 않음")
    void excludeUsedCoupons() {
        // given
        Long accountId = 1L;

        Coupon coupon = Coupon.builder()
                .name("테스트 쿠폰")
                .discountAmount(1000)
                .couponType(CouponType.NORMAL)
                .totalQuantity(100)
                .issuedQuantity(1)
                .validFrom(LocalDateTime.now().minusDays(1))
                .validUntil(LocalDateTime.now().plusDays(30))
                .active(true)
                .build();

        couponRepository.save(coupon);

        IssuedCoupon issuedCoupon = IssuedCoupon.builder()
                .coupon(coupon)
                .accountId(accountId)
                .build();
        issuedCoupon.use();

        issuedCouponRepository.save(issuedCoupon);

        // when
        List<CouponPort.CouponInfo> coupons = couponPortService.getAvailableCoupons(accountId);

        // then
        assertThat(coupons).isEmpty();
    }

    @Test
    @DisplayName("만료된 쿠폰은 조회되지 않음")
    void excludeExpiredCoupons() {
        // given
        Long accountId = 1L;

        Coupon expiredCoupon = Coupon.builder()
                .name("만료된 쿠폰")
                .discountAmount(1000)
                .couponType(CouponType.NORMAL)
                .totalQuantity(100)
                .issuedQuantity(1)
                .validFrom(LocalDateTime.now().minusDays(30))
                .validUntil(LocalDateTime.now().minusDays(1))
                .active(true)
                .build();

        couponRepository.save(expiredCoupon);

        IssuedCoupon issuedCoupon = IssuedCoupon.builder()
                .coupon(expiredCoupon)
                .accountId(accountId)
                .build();

        issuedCouponRepository.save(issuedCoupon);

        // when
        List<CouponPort.CouponInfo> coupons = couponPortService.getAvailableCoupons(accountId);

        // then
        assertThat(coupons).isEmpty();
    }
}
