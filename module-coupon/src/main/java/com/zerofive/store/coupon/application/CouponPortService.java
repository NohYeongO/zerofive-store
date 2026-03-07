package com.zerofive.store.coupon.application;

import com.zerofive.store.core.exception.BusinessException;
import com.zerofive.store.core.exception.NotFoundException;
import com.zerofive.store.core.port.CouponPort;
import com.zerofive.store.coupon.domain.entity.Coupon;
import com.zerofive.store.coupon.domain.entity.IssuedCoupon;
import com.zerofive.store.coupon.infra.repository.IssuedCouponRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CouponPortService implements CouponPort {

    private final IssuedCouponRepository issuedCouponRepository;

    @Override
    @Transactional
    public CouponInfo validateAndApply(Long couponId, Long accountId) {
        IssuedCoupon issuedCoupon = issuedCouponRepository.findByCouponIdAndAccountId(couponId, accountId)
                .orElseThrow(() -> new NotFoundException("발급된 쿠폰을 찾을 수 없습니다."));

        if (issuedCoupon.isUsed()) {
            throw new BusinessException(400, "이미 사용된 쿠폰입니다.");
        }

        Coupon coupon = issuedCoupon.getCoupon();
        if (!coupon.isUsable()) {
            throw new BusinessException(400, "사용할 수 없는 쿠폰입니다.");
        }

        issuedCoupon.use();

        return new CouponInfo(
                coupon.getId(),
                coupon.getName(),
                coupon.getDiscountAmount()
        );
    }

    @Override
    @Transactional
    public void cancelUsage(Long couponId, Long accountId) {
        IssuedCoupon issuedCoupon = issuedCouponRepository.findByCouponIdAndAccountId(couponId, accountId)
                .orElseThrow(() -> new NotFoundException("발급된 쿠폰을 찾을 수 없습니다."));

        issuedCoupon.cancelUsage();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponInfo> getAvailableCoupons(Long accountId) {
        return issuedCouponRepository.findByAccountId(accountId).stream()
                .filter(ic -> !ic.isUsed())
                .filter(ic -> ic.getCoupon().isUsable())
                .map(ic -> new CouponInfo(
                        ic.getCoupon().getId(),
                        ic.getCoupon().getName(),
                        ic.getCoupon().getDiscountAmount()
                ))
                .toList();
    }
}
