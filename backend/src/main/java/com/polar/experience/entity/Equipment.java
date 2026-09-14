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
@Table(name = "equipment")
public class Equipment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "equipment_no", nullable = false, unique = true, length = 50)
    private String equipmentNo;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "cold_resistance_spec", nullable = false, length = 100)
    private String coldResistanceSpec;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_group", nullable = false, length = 20)
    private AgeGroup ageGroup;

    /**
     * 乐观锁版本号：两人同时变更同一器材年龄段时，后提交者版本号过期，按冲突处理。
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(name = "status", nullable = false)
    private Integer status = 1;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}