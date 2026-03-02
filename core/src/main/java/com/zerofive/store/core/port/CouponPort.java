package com.zerofive.store.core.port;

import java.util.List;

public interface CouponPort {

    CouponInfo validateAndApply(Long couponId, Long accountId);

    List<CouponInfo> getAvailableCoupons(Long accountId);

    record CouponInfo(Long couponId, String couponName, int discountAmount) {
    }
}
