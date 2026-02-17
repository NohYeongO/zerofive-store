package com.zerofive.store.coupon.domain.entity;

import com.zerofive.store.core.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "coupon")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    private String name;

    private int discountAmount;

    @Enumerated(EnumType.STRING)
    private CouponType couponType;

    private int totalQuantity;

    private int issuedQuantity;

    private LocalDateTime validFrom;

    private LocalDateTime validUntil;

    private boolean active;

    @ManyToOne(fetch = FetchType.LAZY)
    private Event event;

    @Builder
    public Coupon(String name, int discountAmount, CouponType couponType,
                  int totalQuantity, int issuedQuantity,
                  LocalDateTime validFrom, LocalDateTime validUntil,
                  boolean active, Event event) {
        this.name = name;
        this.discountAmount = discountAmount;
        this.couponType = couponType;
        this.totalQuantity = totalQuantity;
        this.issuedQuantity = issuedQuantity;
        this.validFrom = validFrom;
        this.validUntil = validUntil;
        this.active = active;
        this.event = event;
    }

    public void increaseIssuedQuantity() {
        if (this.issuedQuantity >= this.totalQuantity) {
            throw new IllegalStateException("쿠폰이 모두 소진되었습니다.");
        }
        this.issuedQuantity++;
    }

    public boolean isSoldOut() {
        return this.issuedQuantity >= this.totalQuantity;
    }

    public boolean isEventCoupon() {
        return this.couponType == CouponType.EVENT;
    }

    public boolean isUsable() {
        LocalDateTime now = LocalDateTime.now();
        return active
                && (validFrom == null || now.isAfter(validFrom))
                && (validUntil == null || now.isBefore(validUntil));
    }

    public boolean isExpired() {
        return validUntil != null && LocalDateTime.now().isAfter(validUntil);
    }
}
