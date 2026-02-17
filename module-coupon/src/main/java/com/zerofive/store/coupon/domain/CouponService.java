package com.zerofive.store.coupon.domain;

import com.zerofive.store.core.exception.BusinessException;
import com.zerofive.store.core.exception.NotFoundException;
import com.zerofive.store.coupon.domain.entity.Coupon;
import com.zerofive.store.coupon.domain.entity.CouponType;
import com.zerofive.store.coupon.domain.entity.IssuedCoupon;
import com.zerofive.store.coupon.infra.repository.CouponRepository;
import com.zerofive.store.coupon.infra.repository.IssuedCouponRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponService {

    private final CouponRepository couponRepository;
    private final IssuedCouponRepository issuedCouponRepository;

    @Transactional
    public Coupon createCoupon(String name, int discountAmount, CouponType couponType, int totalQuantity) {
        Coupon coupon = Coupon.builder()
                .name(name)
                .discountAmount(discountAmount)
                .couponType(couponType)
                .totalQuantity(totalQuantity)
                .issuedQuantity(0)
                .active(true)
                .build();

        return couponRepository.save(coupon);
    }

    @Transactional
    public void issueNormalCoupon(Long couponId, Long accountId) {
        Coupon coupon = getCoupon(couponId);

        if (coupon.isEventCoupon()) {
            throw new BusinessException(400, "이벤트 쿠폰은 대기열을 통해 발급받으세요.");
        }

        if (!coupon.isActive()) {
            throw new BusinessException(400, "비활성화된 쿠폰입니다.");
        }

        if (coupon.isSoldOut()) {
            throw new BusinessException(400, "쿠폰이 모두 소진되었습니다.");
        }

        if (issuedCouponRepository.existsByCouponIdAndAccountId(couponId, accountId)) {
            throw new BusinessException(409, "이미 발급받은 쿠폰입니다.");
        }

        IssuedCoupon issuedCoupon = IssuedCoupon.builder()
                .coupon(coupon)
                .accountId(accountId)
                .build();

        coupon.increaseIssuedQuantity();

        issuedCouponRepository.save(issuedCoupon);
    }

    @Transactional(readOnly = true)
    public List<IssuedCoupon> getMyCoupons(Long accountId) {
        return issuedCouponRepository.findByAccountId(accountId);
    }

    @Transactional(readOnly = true)
    public Coupon getCoupon(Long couponId) {
        return couponRepository.findById(couponId)
                .orElseThrow(() -> new NotFoundException("쿠폰을 찾을 수 없습니다: " + couponId));
    }

    @Transactional
    public void issueEventCoupon(Long couponId, Long accountId) {
        Coupon coupon = couponRepository.findByIdWithLock(couponId)
                .orElseThrow(() -> new NotFoundException("쿠폰을 찾을 수 없습니다: " + couponId));

        IssuedCoupon issuedCoupon = IssuedCoupon.builder()
                .coupon(coupon)
                .accountId(accountId)
                .build();

        issuedCouponRepository.save(issuedCoupon);
        coupon.increaseIssuedQuantity();

        log.info("쿠폰 발급 완료: couponId={}, accountId={}", couponId, accountId);
    }
}
