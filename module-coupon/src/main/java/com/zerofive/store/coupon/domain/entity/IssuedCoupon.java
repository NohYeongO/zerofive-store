package com.zerofive.store.coupon.domain.entity;

import com.zerofive.store.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "issued_coupon", uniqueConstraints = {
        @UniqueConstraint(name = "uk_issued_coupon_coupon_account",
                columnNames = {"coupon_id", "account_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class IssuedCoupon extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coupon_id")
    private Coupon coupon;

    @Column(name = "account_id")
    private Long accountId;

    private boolean used;

    @Builder
    public IssuedCoupon(Coupon coupon, Long accountId) {
        this.coupon = coupon;
        this.accountId = accountId;
        this.used = false;
    }

    public void use() {
        if (this.used) {
            throw new IllegalStateException("이미 사용된 쿠폰입니다.");
        }
        this.used = true;
    }

    public void cancelUsage() {
        this.used = false;
    }
}
