package com.kmr.marketplace.service;

import com.kmr.marketplace.dto.CreateTicketRequest;
import com.kmr.marketplace.dto.TicketDto;
import com.kmr.marketplace.entity.SupportTicket;
import com.kmr.marketplace.entity.User;
import com.kmr.marketplace.repository.SupportTicketRepository;
import com.kmr.marketplace.security.AuthHelper;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@Transactional
public class SupportService {

    private final SupportTicketRepository ticketRepo;
    private final AuthHelper authHelper;

    public SupportService(SupportTicketRepository ticketRepo, AuthHelper authHelper) {
        this.ticketRepo = ticketRepo;
        this.authHelper = authHelper;
    }

    public TicketDto create(CreateTicketRequest req) {
        User user = authHelper.currentUser();
        SupportTicket t = new SupportTicket();
        t.setUser(user);
        t.setOrderId(req.orderId());
        t.setType(req.type() == null ? "ISSUE" : req.type());
        t.setSubject(req.subject().trim());
        t.setMessage(req.message());
        t.setStatus("OPEN");
        return toDto(ticketRepo.save(t));
    }

    @Transactional(readOnly = true)
    public List<TicketDto> list() {
        User user = authHelper.currentUser();
        return ticketRepo.findByUserIdOrderByIdDesc(user.getId())
                .stream().map(SupportService::toDto).toList();
    }

    @Transactional(readOnly = true)
    public TicketDto get(Long id) {
        User user = authHelper.currentUser();
        SupportTicket t = ticketRepo.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Ticket not found"));
        return toDto(t);
    }

    static TicketDto toDto(SupportTicket t) {
        return new TicketDto(t.getId(), t.getOrderId(), t.getType(),
                t.getSubject(), t.getMessage(), t.getStatus(),
                t.getCreatedAt() != null ? t.getCreatedAt().toString() : null);
    }
}
