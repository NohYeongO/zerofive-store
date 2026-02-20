package com.zerofive.store.cart.infra.repository;

import com.zerofive.store.cart.domain.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.accountId = :accountId")
    Optional<Cart> findByAccountIdWithItems(@Param("accountId") Long accountId);

    Optional<Cart> findByAccountId(Long accountId);
}
