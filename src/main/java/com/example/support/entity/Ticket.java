package com.example.support.entity;

import com.example.support.enums.*;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tickets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    private TicketCategory category;

    @Enumerated(EnumType.STRING)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    private TicketStatus status;

    @Enumerated(EnumType.STRING)
    private Sentiment sentiment;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String suggestedReply;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    // Customer who created the ticket
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;

    // Agent assigned to the ticket
    @ManyToOne
    @JoinColumn(name = "assigned_agent_id")
    private User assignedAgent;
}