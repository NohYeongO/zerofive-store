package com.zerofive.store.coupon.infra.repository;

import com.zerofive.store.coupon.domain.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findByActiveTrueAndStartAtBeforeAndEndAtAfter(LocalDateTime now1, LocalDateTime now2);
}
