package com.zerofive.store.core.port;

import java.util.List;

public interface AddressPort {

    List<AddressInfo> getAddresses(Long accountId);

    record AddressInfo(
            Long id,
            String name,
            String phone,
            String address,
            String detailAddress,
            String postalCode,
            boolean isDefault
    ) {
    }
}
