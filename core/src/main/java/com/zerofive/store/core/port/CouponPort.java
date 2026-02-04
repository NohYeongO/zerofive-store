package com.zerofive.store.core.port;

public interface CouponPort {

    CouponInfo validateAndApply(Long couponId, Long accountId);

    record CouponInfo(Long couponId, String couponName, int discountAmount) {
    }
}
