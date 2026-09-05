package com.example.support.controller;

import com.example.support.dto.CreateTicketRequest;
import com.example.support.dto.TicketResponse;
import com.example.support.service.TicketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import com.example.support.dto.CreateMessageRequest;
import com.example.support.dto.MessageResponse;
import com.example.support.enums.TicketStatus;
import com.example.support.enums.TicketPriority;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;

@RestController
@RequestMapping("/api/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    public ResponseEntity<TicketResponse> createTicket(
            @Valid @RequestBody CreateTicketRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        TicketResponse response =
                ticketService.createTicket(request, email);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/my")
    public ResponseEntity<List<TicketResponse>> getMyTickets(
            Authentication authentication) {

        String email = authentication.getName();

        List<TicketResponse> tickets =
                ticketService.getCustomerTickets(email);

        return ResponseEntity.ok(tickets);
    }

    @GetMapping("/{ticketId}")
    public ResponseEntity<TicketResponse> getTicket(
            @PathVariable Long ticketId,
            Authentication authentication) {

        String email = authentication.getName();

        TicketResponse response =
                ticketService.getTicket(ticketId, email);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{ticketId}/messages")
    public ResponseEntity<MessageResponse> addMessage(
            @PathVariable Long ticketId,
            @Valid @RequestBody CreateMessageRequest request,
            Authentication authentication) {

        String email = authentication.getName();

        MessageResponse response =
                ticketService.addMessage(ticketId, email, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{ticketId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable Long ticketId,
            Authentication authentication) {

        String email = authentication.getName();

        List<MessageResponse> messages =
                ticketService.getTicketMessages(ticketId, email);

        return ResponseEntity.ok(messages);
    }

    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @GetMapping("/all")
    public ResponseEntity<List<TicketResponse>> getAllTickets() {

        List<TicketResponse> tickets =
                ticketService.getAllTickets();

        return ResponseEntity.ok(tickets);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{ticketId}/assign")
    public ResponseEntity<TicketResponse> assignTicket(
            @PathVariable Long ticketId,
            @RequestParam Long agentId) {

        TicketResponse response =
                ticketService.assignTicket(ticketId, agentId);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @PutMapping("/{ticketId}/status")
    public ResponseEntity<TicketResponse> updateStatus(
            @PathVariable Long ticketId,
            @RequestParam TicketStatus status,
            Authentication authentication) {

        String email = authentication.getName();

        TicketResponse response =
                ticketService.updateStatus(ticketId, status, email);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @PutMapping("/{ticketId}/priority")
    public ResponseEntity<TicketResponse> updatePriority(
            @PathVariable Long ticketId,
            @RequestParam TicketPriority priority,
            Authentication authentication) {

        String email = authentication.getName();

        TicketResponse response =
                ticketService.updatePriority(
                        ticketId,
                        priority,
                        email
                );

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('AGENT')")
    @GetMapping("/agent")
    public ResponseEntity<List<TicketResponse>> getAgentTickets(
            Authentication authentication) {

        String email = authentication.getName();

        List<TicketResponse> tickets =
                ticketService.getAgentTickets(email);

        return ResponseEntity.ok(tickets);
    }

    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @PostMapping("/{ticketId}/analyze")
    public ResponseEntity<TicketResponse> analyzeTicket(
            @PathVariable Long ticketId,
            Authentication authentication) {

        String email = authentication.getName();

        TicketResponse response =
                ticketService.analyzeTicket(ticketId, email);

        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasAnyRole('AGENT', 'ADMIN')")
    @PostMapping("/{ticketId}/reanalyze")
    public ResponseEntity<TicketResponse> reAnalyzeTicket(
            @PathVariable Long ticketId,
            Authentication authentication) {

        String email = authentication.getName();

        TicketResponse response =
                ticketService.reAnalyzeTicket(ticketId, email);

        return ResponseEntity.ok(response);
    }
}