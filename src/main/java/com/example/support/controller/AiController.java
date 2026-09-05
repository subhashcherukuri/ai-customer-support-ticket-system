package com.example.support.controller;

import com.example.support.dto.AiTicketAnalysisResponse;
import com.example.support.service.AiService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiService aiService;

    public AiController(AiService aiService) {
        this.aiService = aiService;
    }

    @GetMapping("/test")
    public String testAI(@RequestParam String prompt) {

        return aiService.askAI(prompt);
    }

    @GetMapping("/analyze")
    public AiTicketAnalysisResponse analyzeTicket(
            @RequestParam String title,
            @RequestParam String description) {

        return aiService.analyzeTicket(title, description);
    }
}