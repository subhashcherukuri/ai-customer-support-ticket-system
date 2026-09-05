package com.example.support.dto;

import com.example.support.enums.Sentiment;
import com.example.support.enums.TicketCategory;
import com.example.support.enums.TicketPriority;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AiTicketAnalysisResponse {

    private TicketCategory category;

    private TicketPriority priority;

    private Sentiment sentiment;

    private String summary;

    private String suggestedReply;
}