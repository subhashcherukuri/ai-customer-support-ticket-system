package com.example.support.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class MessageResponse {

    private Long id;
    private String message;
    private Long senderId;
    private String senderName;
    private LocalDateTime createdAt;
}