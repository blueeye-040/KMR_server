package com.kmr.marketplace.repository;

import com.kmr.marketplace.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByOrderId(Long orderId);
    List<Settlement> findByShopIdOrderByIdDesc(Long shopId);
    List<Settlement> findByStatusOrderByIdDesc(String status);
    boolean existsByOrderId(Long orderId);
}
