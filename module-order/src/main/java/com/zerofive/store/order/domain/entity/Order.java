package com.zerofive.store.order.domain.entity;

import com.zerofive.store.core.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Column(nullable = false)
    private Long accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = false)
    private int totalPrice;

    private int discountAmount;

    @Column(nullable = false)
    private int paymentAmount;

    private Long couponId;

    @Column(nullable = false)
    private Long addressId;

    private String transactionId;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items = new ArrayList<>();

    @Builder
    public Order(Long accountId, int totalPrice, int discountAmount, int paymentAmount,
                 Long couponId, Long addressId, String transactionId) {
        this.accountId = accountId;
        this.status = OrderStatus.PAYMENT_COMPLETED;
        this.totalPrice = totalPrice;
        this.discountAmount = discountAmount;
        this.paymentAmount = paymentAmount;
        this.couponId = couponId;
        this.addressId = addressId;
        this.transactionId = transactionId;
    }

    public void addItem(OrderItem item) {
        items.add(item);
        item.setOrder(this);
    }

    public void updateStatus(OrderStatus status) {
        this.status = status;
    }
}
