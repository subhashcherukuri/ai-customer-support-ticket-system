package com.example.support.repository;

import com.example.support.entity.Ticket;
import com.example.support.entity.User;
import com.example.support.enums.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findByCustomer(User customer);

    List<Ticket> findByAssignedAgent(User assignedAgent);

    List<Ticket> findByStatus(TicketStatus status);
}