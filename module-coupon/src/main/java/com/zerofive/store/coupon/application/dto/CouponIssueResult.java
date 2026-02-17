package com.zerofive.store.coupon.application.dto;

import com.zerofive.store.core.exception.BusinessException;

public record CouponIssueResult(
        QueueStatus status
) {
    public static CouponIssueResult success() {
        return new CouponIssueResult(QueueStatus.SUCCESS);
    }

    public static CouponIssueResult duplicate() {
        return new CouponIssueResult(QueueStatus.DUPLICATE);
    }

    public static CouponIssueResult soldOut() {
        return new CouponIssueResult(QueueStatus.SOLD_OUT);
    }

    public static CouponIssueResult from(String result) {
        QueueStatus status = switch (result) {
            case "SUCCESS" -> QueueStatus.SUCCESS;
            case "SOLD_OUT" -> QueueStatus.SOLD_OUT;
            default -> throw new BusinessException(500, "알 수 없는 발급 상태: " + result);
        };
        return new CouponIssueResult(status);
    }
}
