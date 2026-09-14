package com.polar.experience.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "notification")
public class Notification {

    /** 接收角色：当班馆务 */
    public static final String RECIPIENT_ROLE_DUTY_STAFF = "DUTY_STAFF";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false, length = 100)
    private String title;

    @Column(name = "content", nullable = false, length = 500)
    private String content;

    @Column(name = "session_id")
    private Long sessionId;

    @Column(name = "recipient_role", nullable = false, length = 50)
    private String recipientRole = RECIPIENT_ROLE_DUTY_STAFF;

    @Column(name = "read_flag", nullable = false)
    private Boolean readFlag = false;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
