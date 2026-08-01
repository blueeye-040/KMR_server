package com.kmr.marketplace.repository;

import com.kmr.marketplace.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Long> {
    Optional<Coupon> findByCodeIgnoreCase(String code);
    boolean existsByCodeIgnoreCase(String code);
    List<Coupon> findByActiveTrueOrderByMinCartValueAsc();
    List<Coupon> findAllByOrderByIdDesc();
}
