package com.polar.experience.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class NotificationDTO {

    private Long id;

    private String title;

    private String content;

    private Long sessionId;

    private Boolean readFlag;

    private LocalDateTime createdAt;

    private LocalDateTime readAt;
}
