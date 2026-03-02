package com.zerofive.store.order.infra.repository;

import com.zerofive.store.order.domain.entity.OrderSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderSessionRepository extends JpaRepository<OrderSession, Long> {

    @Query("SELECT os FROM OrderSession os LEFT JOIN FETCH os.items WHERE os.sessionId = :sessionId")
    Optional<OrderSession> findBySessionIdWithItems(@Param("sessionId") String sessionId);

    List<OrderSession> findByLastPolledAtBefore(LocalDateTime time);
}
