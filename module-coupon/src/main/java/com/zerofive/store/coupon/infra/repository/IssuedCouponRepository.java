package com.zerofive.store.coupon.infra.repository;

import com.zerofive.store.coupon.domain.entity.IssuedCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IssuedCouponRepository extends JpaRepository<IssuedCoupon, Long> {

    List<IssuedCoupon> findByAccountId(Long accountId);

    Optional<IssuedCoupon> findByCouponIdAndAccountId(Long couponId, Long accountId);

    boolean existsByCouponIdAndAccountId(Long couponId, Long accountId);
}
