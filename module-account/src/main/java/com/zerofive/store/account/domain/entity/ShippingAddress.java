package com.zerofive.store.account.domain.entity;

import com.zerofive.store.core.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shipping_addresses")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ShippingAddress extends BaseEntity {

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String phone;

    @Column(nullable = false)
    private String address;

    private String detailAddress;

    @Column(nullable = false)
    private String postalCode;

    @Column(nullable = false)
    private boolean isDefault;

    @Builder
    public ShippingAddress(Long accountId, String name, String phone,
                           String address, String detailAddress, String postalCode,
                           boolean isDefault) {
        this.accountId = accountId;
        this.name = name;
        this.phone = phone;
        this.address = address;
        this.detailAddress = detailAddress;
        this.postalCode = postalCode;
        this.isDefault = isDefault;
    }

    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
