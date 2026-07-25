package com.kmr.marketplace.repository;

import com.kmr.marketplace.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByUserIdOrderByIdDesc(Long userId);

    Optional<Order> findByIdAndUserId(Long id, Long userId);

    // Order detail with line items + product/shop, avoiding N+1.
    @Query("SELECT o FROM Order o " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.product " +
           "LEFT JOIN FETCH i.shop " +
           "WHERE o.id = :id AND o.user.id = :uid")
    Optional<Order> findDetailByIdAndUserId(@Param("id") Long id, @Param("uid") Long userId);
}
