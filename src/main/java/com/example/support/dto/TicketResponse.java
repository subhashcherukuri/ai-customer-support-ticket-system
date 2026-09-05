package com.example.support.dto;

import com.example.support.enums.Sentiment;
import com.example.support.enums.TicketCategory;
import com.example.support.enums.TicketPriority;
import com.example.support.enums.TicketStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class TicketResponse {

    private Long id;

    private String title;

    private String description;

    private TicketCategory category;

    private TicketPriority priority;

    private TicketStatus status;

    private Sentiment sentiment;

    private String summary;

    private String suggestedReply;

    private Long customerId;
    private Long assignedAgentId;
    private String assignedAgentName;
}