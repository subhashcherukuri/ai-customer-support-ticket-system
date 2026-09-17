package com.example.support.service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;
import com.example.support.dto.AiTicketAnalysisResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class AiService {

    private final ChatClient chatClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AiService(
            ChatClient.Builder chatClientBuilder) {

        this.chatClient = chatClientBuilder.build();
    }

    public String askAI(String prompt) {

        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }

    public AiTicketAnalysisResponse analyzeTicket(
            String title,
            String description) {

        String prompt = """
            Analyze the following customer support ticket.

            Title:
            %s

            Description:
            %s

            Determine:

            1. Category - choose exactly one:
               PAYMENT, REFUND, ORDER, DELIVERY, ACCOUNT,
               TECHNICAL, PRODUCT, OTHER

            2. Priority - choose exactly one:
               LOW, MEDIUM, HIGH, CRITICAL

            3. Sentiment - choose exactly one:
               POSITIVE, NEUTRAL, NEGATIVE

            4. Summary - give a short summary of the customer's issue.

            5. Suggested Reply - write a professional customer support reply.

            Return ONLY valid JSON.
            Do not use markdown.
            Do not use ```.

            JSON format:
            {
              "category": "PAYMENT",
              "priority": "HIGH",
              "sentiment": "NEGATIVE",
              "summary": "Short summary here",
              "suggestedReply": "Professional reply here"
            }
            """.formatted(title, description);

        String response = chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();

        try {
            return objectMapper.readValue(
                    response,
                    AiTicketAnalysisResponse.class
            );
        } catch (Exception e) {
            throw new RuntimeException(
                    "Failed to parse AI response", e
            );
        }
    }

    public String generateSuggestedReply(String conversation) {

        String prompt = """
            You are an AI customer support assistant.

            Generate a professional, helpful, and context-aware reply
            to the latest customer message.

            Consider the complete conversation before writing the reply.

            Conversation:
            %s

            Instructions:
            - Reply directly to the latest customer message.
            - Use information already provided in the conversation.
            - Do not ask for information that the customer has already provided.
            - Do not invent order details, refunds, delivery dates, or company policies.
            - If required information is missing, politely ask for it.
            - Do not mention that you are an AI.
            - Do not use markdown.
            - Return only the reply text.
            """.formatted(conversation);

        return chatClient
                .prompt()
                .user(prompt)
                .call()
                .content();
    }
}