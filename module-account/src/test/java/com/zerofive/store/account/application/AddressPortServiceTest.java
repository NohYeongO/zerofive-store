package com.zerofive.store.account.application;

import com.zerofive.store.account.domain.entity.ShippingAddress;
import com.zerofive.store.account.infra.ShippingAddressRepository;
import com.zerofive.store.core.MysqlRedisTestContainer;
import com.zerofive.store.core.port.AddressPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(AddressPortService.class)
@DisplayName("배송지 관련 Repository 테스트")
class AddressPortServiceTest extends MysqlRedisTestContainer {

    @Autowired
    private AddressPortService addressPortService;

    @Autowired
    private ShippingAddressRepository addressRepository;

    @Test
    @DisplayName("배송지 저장 및 조회 테스트")
    void saveAndRetrieveAddresses() {
        // given
        Long accountId = 1L;

        ShippingAddress defaultAddress = ShippingAddress.builder()
                .accountId(accountId)
                .name("홍길동")
                .phone("010-1234-5678")
                .address("서울시 강남구 테헤란로 123")
                .detailAddress("101동 1001호")
                .postalCode("06234")
                .isDefault(true)
                .build();

        ShippingAddress normalAddress = ShippingAddress.builder()
                .accountId(accountId)
                .name("홍길동")
                .phone("010-1234-5678")
                .address("서울시 서초구 서초대로 456")
                .detailAddress("202동 2002호")
                .postalCode("06500")
                .isDefault(false)
                .build();

        addressRepository.saveAll(List.of(defaultAddress, normalAddress));

        // when
        List<AddressPort.AddressInfo> addresses = addressPortService.getAddresses(accountId);

        // then
        assertThat(addresses).hasSize(2);
        assertThat(addresses.getFirst().isDefault()).isTrue();
        assertThat(addresses.getFirst().name()).isEqualTo("홍길동");
        assertThat(addresses.getFirst().address()).isEqualTo("서울시 강남구 테헤란로 123");
    }

    @Test
    @DisplayName("계정별 배송지 분리 조회 테스트")
    void retrieveAddressesByAccount() {
        // given
        Long accountId1 = 1L;
        Long accountId2 = 2L;

        addressRepository.save(ShippingAddress.builder()
                .accountId(accountId1)
                .name("홍길동")
                .phone("010-1234-5678")
                .address("서울시 강남구")
                .postalCode("06234")
                .isDefault(true)
                .build());

        addressRepository.save(ShippingAddress.builder()
                .accountId(accountId2)
                .name("김철수")
                .phone("010-9999-9999")
                .address("부산시 해운대구")
                .postalCode("48099")
                .isDefault(true)
                .build());

        // when
        List<AddressPort.AddressInfo> addresses1 = addressPortService.getAddresses(accountId1);
        List<AddressPort.AddressInfo> addresses2 = addressPortService.getAddresses(accountId2);

        // then
        assertThat(addresses1).hasSize(1);
        assertThat(addresses1.getFirst().name()).isEqualTo("홍길동");

        assertThat(addresses2).hasSize(1);
        assertThat(addresses2.getFirst().name()).isEqualTo("김철수");
    }
}
