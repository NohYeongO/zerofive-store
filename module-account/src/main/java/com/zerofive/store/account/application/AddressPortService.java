package com.zerofive.store.account.application;

import com.zerofive.store.account.domain.entity.ShippingAddress;
import com.zerofive.store.account.infra.ShippingAddressRepository;
import com.zerofive.store.core.port.AddressPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AddressPortService implements AddressPort {

    private final ShippingAddressRepository addressRepository;

    @Override
    @Transactional(readOnly = true)
    public List<AddressInfo> getAddresses(Long accountId) {
        return addressRepository.findByAccountIdOrderByIsDefaultDescCreatedAtDesc(accountId)
                .stream()
                .map(this::toAddressInfo)
                .toList();
    }

    private AddressInfo toAddressInfo(ShippingAddress address) {
        return new AddressInfo(
                address.getId(),
                address.getName(),
                address.getPhone(),
                address.getAddress(),
                address.getDetailAddress(),
                address.getPostalCode(),
                address.isDefault()
        );
    }
}
