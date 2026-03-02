package com.zerofive.store.account.infra;

import com.zerofive.store.account.domain.entity.ShippingAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {

    List<ShippingAddress> findByAccountIdOrderByIsDefaultDescCreatedAtDesc(Long accountId);
}
