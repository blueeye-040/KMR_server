package com.kmr.marketplace.repository;

import com.kmr.marketplace.entity.SupportTicket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    List<SupportTicket> findByUserIdOrderByIdDesc(Long userId);
    Optional<SupportTicket> findByIdAndUserId(Long id, Long userId);
}
