package com.polar.experience.entity;

import com.polar.experience.enums.AgeGroup;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class ChangeLog {

    @Id
    @GeneratedValue(strategy = GenerationType.TABLE)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "old_age_group", length = 20)
    private AgeGroup oldAgeGroup;

    @Enumerated(EnumType.STRING)
    @Column(name = "new_age_group", nullable = false, length = 20)
    private AgeGroup newAgeGroup;

    @Column(name = "change_reason", length = 500)
    private String changeReason;

    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}