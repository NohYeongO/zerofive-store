package com.zerofive.store.order.domain;

import com.zerofive.store.core.exception.NotFoundException;
import com.zerofive.store.order.domain.entity.Order;
import com.zerofive.store.order.infra.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderReadService {

    private final OrderRepository orderRepository;

    @Transactional(readOnly = true)
    public List<Order> getOrdersByAccountId(Long accountId) {
        return orderRepository.findByAccountIdOrderByCreatedAtDesc(accountId);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long orderId, Long accountId) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new NotFoundException("주문을 찾을 수 없습니다. orderId: " + orderId));

        if (!order.getAccountId().equals(accountId)) {
            throw new NotFoundException("주문을 찾을 수 없습니다. orderId: " + orderId);
        }

        return order;
    }
}
