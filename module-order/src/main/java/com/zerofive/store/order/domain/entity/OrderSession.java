package com.zerofive.store.order.domain.entity;

import com.zerofive.store.core.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "order_sessions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderSession extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String sessionId;

    @Column(nullable = false)
    private Long accountId;

    @Column(nullable = false)
    private LocalDateTime lastPolledAt;

    @OneToMany(mappedBy = "orderSession", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderSessionItem> items = new ArrayList<>();

    @Builder
    public OrderSession(String sessionId, Long accountId) {
        this.sessionId = sessionId;
        this.accountId = accountId;
        this.lastPolledAt = LocalDateTime.now();
    }

    public void addItem(OrderSessionItem item) {
        items.add(item);
        item.setOrderSession(this);
    }

    public void updatePolledAt() {
        this.lastPolledAt = LocalDateTime.now();
    }

    public boolean isExpired() {
        return lastPolledAt.plusMinutes(1).isBefore(LocalDateTime.now());
    }
}
