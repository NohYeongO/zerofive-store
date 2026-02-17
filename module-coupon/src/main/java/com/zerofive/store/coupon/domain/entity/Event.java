package com.zerofive.store.coupon.domain.entity;

import com.zerofive.store.core.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
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
@Table(name = "event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Event extends BaseEntity {

    private String name;

    private String description;

    private LocalDateTime startAt;

    private LocalDateTime endAt;

    private boolean active;

    private int threshold = 200;

    @OneToMany(mappedBy = "event", fetch = FetchType.LAZY)
    private List<Coupon> coupons = new ArrayList<>();

    @Builder
    public Event(String name, String description, LocalDateTime startAt, LocalDateTime endAt, boolean active, Integer threshold) {
        this.name = name;
        this.description = description;
        this.startAt = startAt;
        this.endAt = endAt;
        this.active = active;
        this.threshold = threshold != null ? threshold : 200;
    }

    public boolean isOngoing() {
        LocalDateTime now = LocalDateTime.now();
        return active && now.isAfter(startAt) && now.isBefore(endAt);
    }

    public boolean isBeforeStart() {
        return LocalDateTime.now().isBefore(startAt);
    }

    public boolean isEnded() {
        return LocalDateTime.now().isAfter(endAt);
    }
}
