package com.example.support.service;

import com.example.support.dto.*;
import com.example.support.entity.Ticket;
import com.example.support.entity.User;
import com.example.support.enums.Role;
import com.example.support.enums.TicketStatus;
import com.example.support.exception.BadRequestException;
import com.example.support.exception.ResourceNotFoundException;
import com.example.support.repository.TicketRepository;
import com.example.support.repository.UserRepository;
import com.example.support.entity.TicketMessage;
import com.example.support.repository.TicketMessageRepository;
import com.example.support.enums.TicketPriority;
import org.springframework.stereotype.Service;
import com.example.support.exception.AccessDeniedException;
import java.time.LocalDateTime;

import java.util.List;

@Service
public class TicketService {

    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketMessageRepository ticketMessageRepository;
    private final AiService aiService;

    public TicketService(TicketRepository ticketRepository,
                         UserRepository userRepository,TicketMessageRepository ticketMessageRepository,AiService aiService) {
        this.ticketRepository = ticketRepository;
        this.userRepository = userRepository;
        this.ticketMessageRepository = ticketMessageRepository;
        this.aiService = aiService;
    }

    public TicketResponse createTicket(
            CreateTicketRequest request,
            String email) {

        User customer = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // Create basic ticket
        Ticket ticket = Ticket.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(TicketStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .customer(customer)
                .build();

        // Ask AI to analyze the ticket
        AiTicketAnalysisResponse analysis =
                aiService.analyzeTicket(
                        request.getTitle(),
                        request.getDescription()
                );

        // Store AI results
        ticket.setCategory(analysis.getCategory());
        ticket.setPriority(analysis.getPriority());
        ticket.setSentiment(analysis.getSentiment());
        ticket.setSummary(analysis.getSummary());
        ticket.setSuggestedReply(analysis.getSuggestedReply());

        // Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);

        return new TicketResponse(
                savedTicket.getId(),
                savedTicket.getTitle(),
                savedTicket.getDescription(),
                savedTicket.getCategory(),
                savedTicket.getPriority(),
                savedTicket.getStatus(),
                savedTicket.getSentiment(),
                savedTicket.getSummary(),
                savedTicket.getSuggestedReply(),
                savedTicket.getCustomer().getId(),
                savedTicket.getAssignedAgent() != null
                        ? savedTicket.getAssignedAgent().getId()
                        : null,
                savedTicket.getAssignedAgent() != null
                        ? savedTicket.getAssignedAgent().getName()
                        : null
        );
    }

    public List<TicketResponse> getCustomerTickets(String email) {

        User customer = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found"));

        List<Ticket> tickets =
                ticketRepository.findByCustomer(customer);

        return tickets.stream()
                .map(ticket -> new TicketResponse(
                        ticket.getId(),
                        ticket.getTitle(),
                        ticket.getDescription(),
                        ticket.getCategory(),
                        ticket.getPriority(),
                        ticket.getStatus(),
                        ticket.getSentiment(),
                        ticket.getSummary(),
                        ticket.getSuggestedReply(),
                        ticket.getCustomer().getId(),
                        ticket.getAssignedAgent() != null
                                ? ticket.getAssignedAgent().getId()
                                : null,
                        ticket.getAssignedAgent() != null
                                ? ticket.getAssignedAgent().getName()
                                : null
                ))
                .toList();
    }

    public TicketResponse getTicket(
            Long ticketId,
            String email) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ticket not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // CUSTOMER → can view only their own tickets
        if (user.getRole() == Role.CUSTOMER) {

            if (!ticket.getCustomer().getId().equals(user.getId())) {

                throw new AccessDeniedException(
                        "You are not allowed to access this ticket");
            }
        }

        // AGENT → can view only tickets assigned to them
        else if (user.getRole() == Role.AGENT) {

            if (ticket.getAssignedAgent() == null ||
                    !ticket.getAssignedAgent()
                            .getId()
                            .equals(user.getId())) {

                throw new AccessDeniedException(
                        "You are not assigned to this ticket");
            }
        }

        // ADMIN → can view any ticket
        else if (user.getRole() == Role.ADMIN) {

            // Admin is allowed
        }
            return new TicketResponse(
                    ticket.getId(),
                    ticket.getTitle(),
                    ticket.getDescription(),
                    ticket.getCategory(),
                    ticket.getPriority(),
                    ticket.getStatus(),
                    ticket.getSentiment(),
                    ticket.getSummary(),
                    ticket.getSuggestedReply(),
                    ticket.getCustomer().getId(),
                    ticket.getAssignedAgent() != null
                            ? ticket.getAssignedAgent().getId()
                            : null,
                    ticket.getAssignedAgent() != null
                            ? ticket.getAssignedAgent().getName()
                            : null
            );
    }

    public MessageResponse addMessage(
            Long ticketId,
            String email,
            CreateMessageRequest request) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User sender = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Sender not found"));

        // Customer can send message only to their own ticket
        if (sender.getRole() == Role.CUSTOMER) {

            if (!ticket.getCustomer().getId().equals(sender.getId())) {
                throw new AccessDeniedException(
                        "You are not allowed to send a message to this ticket");
            }
        }

        // Agent can send message only if the ticket is assigned to them
        else if (sender.getRole() == Role.AGENT) {

            if (ticket.getAssignedAgent() == null ||
                    !ticket.getAssignedAgent().getId().equals(sender.getId())) {

                throw new AccessDeniedException(
                        "You are not assigned to this ticket");
            }
        }

        if (ticket.getStatus() == TicketStatus.CLOSED) {
            throw new BadRequestException(
                    "Cannot send messages to a closed ticket");
        }

        TicketMessage ticketMessage = TicketMessage.builder()
                .message(request.getMessage())
                .createdAt(LocalDateTime.now())
                .ticket(ticket)
                .sender(sender)
                .build();

        TicketMessage savedMessage =
                ticketMessageRepository.save(ticketMessage);

        return new MessageResponse(
                savedMessage.getId(),
                savedMessage.getMessage(),
                savedMessage.getSender().getId(),
                savedMessage.getSender().getName(),
                savedMessage.getCreatedAt()
        );
    }

    public List<MessageResponse> getTicketMessages(
            Long ticketId,
            String email) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        boolean isCustomer =
                user.getRole() == Role.CUSTOMER &&
                        ticket.getCustomer().getId().equals(user.getId());

        boolean isAssignedAgent =
                user.getRole() == Role.AGENT &&
                        ticket.getAssignedAgent() != null &&
                        ticket.getAssignedAgent().getId().equals(user.getId());

        boolean isAdmin =
                user.getRole() == Role.ADMIN;

        if (!isCustomer && !isAssignedAgent && !isAdmin) {
            throw new AccessDeniedException(
                    "You are not allowed to access this conversation");
        }

        List<TicketMessage> messages =
                ticketMessageRepository
                        .findByTicketOrderByCreatedAtAsc(ticket);

        return messages.stream()
                .map(message -> new MessageResponse(
                        message.getId(),
                        message.getMessage(),
                        message.getSender().getId(),
                        message.getSender().getName(),
                        message.getCreatedAt()
                ))
                .toList();
    }

    public List<TicketResponse> getAllTickets() {

        List<Ticket> tickets = ticketRepository.findAll();

        return tickets.stream()
                .map(ticket -> new TicketResponse(
                        ticket.getId(),
                        ticket.getTitle(),
                        ticket.getDescription(),
                        ticket.getCategory(),
                        ticket.getPriority(),
                        ticket.getStatus(),
                        ticket.getSentiment(),
                        ticket.getSummary(),
                        ticket.getSuggestedReply(),
                        ticket.getCustomer().getId(),
                        ticket.getAssignedAgent() != null
                                ? ticket.getAssignedAgent().getId()
                                : null,
                        ticket.getAssignedAgent() != null
                                ? ticket.getAssignedAgent().getName()
                                : null
                ))
                .toList();
    }

    public TicketResponse assignTicket(
            Long ticketId,
            Long agentId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User agent = userRepository.findById(agentId)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        if (agent.getRole() != com.example.support.enums.Role.AGENT) {
            throw new BadRequestException("User is not an agent");
        }

        ticket.setAssignedAgent(agent);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        return new TicketResponse(
                savedTicket.getId(),
                savedTicket.getTitle(),
                savedTicket.getDescription(),
                savedTicket.getCategory(),
                savedTicket.getPriority(),
                savedTicket.getStatus(),
                savedTicket.getSentiment(),
                savedTicket.getSummary(),
                savedTicket.getSuggestedReply(),
                savedTicket.getCustomer().getId(),
                savedTicket.getAssignedAgent() != null
                        ? savedTicket.getAssignedAgent().getId()
                        : null,
                savedTicket.getAssignedAgent() != null
                        ? savedTicket.getAssignedAgent().getName()
                        : null
        );
    }

    public TicketResponse updateStatus(
            Long ticketId,
            TicketStatus status,
            String email) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ticket not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        // Check user permission
        if (user.getRole() == Role.ADMIN) {

            // Admin is allowed to update any ticket

        } else if (user.getRole() == Role.AGENT) {

            // Agent can update only tickets assigned to them
            if (ticket.getAssignedAgent() == null ||
                    !ticket.getAssignedAgent()
                            .getId()
                            .equals(user.getId())) {

                throw new AccessDeniedException(
                        "You can update status only for tickets assigned to you");
            }

        } else {

            // Customer cannot update ticket status
            throw new AccessDeniedException(
                    "You are not allowed to update ticket status");
        }

        // Check if the ticket already has the requested status
        if (ticket.getStatus() == status) {
            throw new BadRequestException(
                    "Ticket is already in " + status + " status");
        }

        // Check whether the status transition is valid
        if (!isValidStatusTransition(
                ticket.getStatus(),
                status)) {

            throw new BadRequestException(
                    "Invalid status transition from "
                            + ticket.getStatus()
                            + " to "
                            + status);
        }

        // Update status
        ticket.setStatus(status);

        // Update modification time
        ticket.setUpdatedAt(LocalDateTime.now());

        // Save ticket
        Ticket savedTicket = ticketRepository.save(ticket);

        // Return response
        return new TicketResponse(
                savedTicket.getId(),
                savedTicket.getTitle(),
                savedTicket.getDescription(),
                savedTicket.getCategory(),
                savedTicket.getPriority(),
                savedTicket.getStatus(),
                savedTicket.getSentiment(),
                savedTicket.getSummary(),
                savedTicket.getSuggestedReply(),
                savedTicket.getCustomer().getId(),
                savedTicket.getAssignedAgent() != null
                        ? savedTicket.getAssignedAgent().getId()
                        : null,
                savedTicket.getAssignedAgent() != null
                        ? savedTicket.getAssignedAgent().getName()
                        : null
        );
    }

    public TicketResponse updatePriority(
            Long ticketId,
            TicketPriority priority,
            String email) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Admin can update any ticket
        if (user.getRole() == Role.ADMIN) {
            ticket.setPriority(priority);
        }

        // Agent can update only their assigned tickets
        else if (user.getRole() == Role.AGENT) {

            if (ticket.getAssignedAgent() == null ||
                    !ticket.getAssignedAgent().getId().equals(user.getId())) {

                throw new AccessDeniedException(
                        "You can update priority only for tickets assigned to you");
            }

            ticket.setPriority(priority);
            ticket.setUpdatedAt(LocalDateTime.now());
        }

        else {
            throw new AccessDeniedException(
                    "You are not allowed to update ticket priority");
        }

        Ticket savedTicket = ticketRepository.save(ticket);

        return new TicketResponse(
                savedTicket.getId(),
                savedTicket.getTitle(),
                savedTicket.getDescription(),
                savedTicket.getCategory(),
                savedTicket.getPriority(),
                savedTicket.getStatus(),
                savedTicket.getSentiment(),
                savedTicket.getSummary(),
                savedTicket.getSuggestedReply(),
                savedTicket.getCustomer().getId(),
                savedTicket.getAssignedAgent() != null
                        ? savedTicket.getAssignedAgent().getId()
                        : null,
                savedTicket.getAssignedAgent() != null
                        ? savedTicket.getAssignedAgent().getName()
                        : null
        );
    }

    public List<TicketResponse> getAgentTickets(String email) {

        User agent = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found"));

        if (agent.getRole() != Role.AGENT) {
            throw new AccessDeniedException("User is not an agent");
        }

        List<Ticket> tickets =
                ticketRepository.findByAssignedAgent(agent);

        return tickets.stream()
                .map(ticket -> new TicketResponse(
                        ticket.getId(),
                        ticket.getTitle(),
                        ticket.getDescription(),
                        ticket.getCategory(),
                        ticket.getPriority(),
                        ticket.getStatus(),
                        ticket.getSentiment(),
                        ticket.getSummary(),
                        ticket.getSuggestedReply(),
                        ticket.getCustomer().getId(),
                        ticket.getAssignedAgent() != null
                                ? ticket.getAssignedAgent().getId()
                                : null,
                        ticket.getAssignedAgent() != null
                                ? ticket.getAssignedAgent().getName()
                                : null
                ))
                .toList();
    }

    public TicketResponse analyzeTicket(
            Long ticketId,
            String email) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ticket not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (user.getRole() == Role.ADMIN) {
            // Admin can analyze any ticket
        }

        else if (user.getRole() == Role.AGENT) {

            if (ticket.getAssignedAgent() == null ||
                    !ticket.getAssignedAgent().getId().equals(user.getId())) {

                throw new AccessDeniedException(
                        "You can analyze only tickets assigned to you");
            }
        }

        else {
            throw new AccessDeniedException(
                    "You are not allowed to analyze tickets");
        }

        AiTicketAnalysisResponse analysis =
                aiService.analyzeTicket(
                        ticket.getTitle(),
                        ticket.getDescription()
                );

        ticket.setCategory(analysis.getCategory());
        ticket.setPriority(analysis.getPriority());
        ticket.setSentiment(analysis.getSentiment());
        ticket.setSummary(analysis.getSummary());
        ticket.setSuggestedReply(analysis.getSuggestedReply());

        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        return new TicketResponse(
                savedTicket.getId(),
                savedTicket.getTitle(),
                savedTicket.getDescription(),
                savedTicket.getCategory(),
                savedTicket.getPriority(),
                savedTicket.getStatus(),
                savedTicket.getSentiment(),
                savedTicket.getSummary(),
                savedTicket.getSuggestedReply(),
                savedTicket.getCustomer().getId(),
                savedTicket.getAssignedAgent() != null
                        ? savedTicket.getAssignedAgent().getId()
                        : null,
                savedTicket.getAssignedAgent() != null
                        ? savedTicket.getAssignedAgent().getName()
                        : null
        );
    }

    public TicketResponse reAnalyzeTicket(Long ticketId, String email) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // ADMIN can re-analyze any ticket
        if (user.getRole() == Role.ADMIN) {

            // allowed

        }
        // AGENT can re-analyze only assigned tickets
        else if (user.getRole() == Role.AGENT) {

            if (ticket.getAssignedAgent() == null ||
                    !ticket.getAssignedAgent().getId().equals(user.getId())) {

                throw new AccessDeniedException(
                        "You can re-analyze only tickets assigned to you");
            }

        }
        // CUSTOMER cannot re-analyze
        else {

            throw new AccessDeniedException(
                    "You are not allowed to re-analyze tickets");
        }

        // Call AI again
        AiTicketAnalysisResponse analysis =
                aiService.analyzeTicket(
                        ticket.getTitle(),
                        ticket.getDescription()
                );

        // Update AI information
        ticket.setCategory(analysis.getCategory());
        ticket.setPriority(analysis.getPriority());
        ticket.setSentiment(analysis.getSentiment());
        ticket.setSummary(analysis.getSummary());
        ticket.setSuggestedReply(analysis.getSuggestedReply());
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        return new TicketResponse(
                savedTicket.getId(),
                savedTicket.getTitle(),
                savedTicket.getDescription(),
                savedTicket.getCategory(),
                savedTicket.getPriority(),
                savedTicket.getStatus(),
                savedTicket.getSentiment(),
                savedTicket.getSummary(),
                savedTicket.getSuggestedReply(),
                savedTicket.getCustomer().getId(),
                savedTicket.getAssignedAgent() != null
                        ? savedTicket.getAssignedAgent().getId()
                        : null,
                savedTicket.getAssignedAgent() != null
                        ? savedTicket.getAssignedAgent().getName()
                        : null
        );
    }

    private boolean isValidStatusTransition(
            TicketStatus currentStatus,
            TicketStatus newStatus) {

        // Same status is not considered a valid transition
        if (currentStatus == newStatus) {
            return false;
        }

        // Closed tickets cannot be changed
        if (currentStatus == TicketStatus.CLOSED) {
            return false;
        }

        // OPEN → IN_PROGRESS
        if (currentStatus == TicketStatus.OPEN) {
            return newStatus == TicketStatus.IN_PROGRESS;
        }

        // IN_PROGRESS → RESOLVED
        if (currentStatus == TicketStatus.IN_PROGRESS) {
            return newStatus == TicketStatus.RESOLVED;
        }

        // RESOLVED → CLOSED
        // RESOLVED → IN_PROGRESS (reopen)
        if (currentStatus == TicketStatus.RESOLVED) {
            return newStatus == TicketStatus.CLOSED
                    || newStatus == TicketStatus.IN_PROGRESS;
        }

        return false;
    }

    public TicketResponse assignAgent(Long ticketId, Long agentId) {

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Ticket not found"));

        User agent = userRepository.findById(agentId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Agent not found"));

        if (agent.getRole() != Role.AGENT) {
            throw new BadRequestException("Selected user is not an agent");
        }

        ticket.setAssignedAgent(agent);
        ticket.setUpdatedAt(LocalDateTime.now());

        Ticket savedTicket = ticketRepository.save(ticket);

        return new TicketResponse(
                savedTicket.getId(),
                savedTicket.getTitle(),
                savedTicket.getDescription(),
                savedTicket.getCategory(),
                savedTicket.getPriority(),
                savedTicket.getStatus(),
                savedTicket.getSentiment(),
                savedTicket.getSummary(),
                savedTicket.getSuggestedReply(),
                savedTicket.getCustomer().getId(),
                savedTicket.getAssignedAgent() != null
                        ? savedTicket.getAssignedAgent().getId()
                        : null,
                savedTicket.getAssignedAgent() != null
                        ? savedTicket.getAssignedAgent().getName()
                        : null
        );
    }

}