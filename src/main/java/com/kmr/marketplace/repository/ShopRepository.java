package com.kmr.marketplace.repository;

import com.kmr.marketplace.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShopRepository extends JpaRepository<Shop, Long> {
    long countByApprovedTrue();
}
