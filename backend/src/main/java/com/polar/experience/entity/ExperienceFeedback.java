package com.polar.experience.entity;

import com.polar.experience.enums.NumbnessLevel;
import com.polar.experience.enums.TempFeeling;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 体验感受登记：低温体验散场时，记下游客随口说的冷不冷、手脚麻不麻。
 * 同一天、同一场次、同一位游客只保留一条（数据库唯一约束兜底）。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "experience_feedback",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_feedback_session_visitor_date",
                columnNames = {"session_id", "visitor_name", "feedback_date"}))
public class ExperienceFeedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 场次信息在登记时快照留存，场次后续调整不影响已登记的名单 */
    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "session_no", nullable = false, length = 50)
    private String sessionNo;

    @Column(name = "session_name", nullable = false, length = 100)
    private String sessionName;

    /** 游客称呼：散场时口头登记，允许写“张女士”“带孩子的爸爸”这类称呼 */
    @Column(name = "visitor_name", nullable = false, length = 50)
    private String visitorName;

    @Enumerated(EnumType.STRING)
    @Column(name = "temp_feeling", nullable = false, length = 20)
    private TempFeeling tempFeeling;

    @Enumerated(EnumType.STRING)
    @Column(name = "numbness", nullable = false, length = 20)
    private NumbnessLevel numbness;

    /** 登记人：隔天对班组交代时知道这条是谁记的 */
    @Column(name = "registrar", nullable = false, length = 50)
    private String registrar;

    /** 登记日期：体验发生的当天，“同一游客当天只记一条”按此日期判定 */
    @Column(name = "feedback_date", nullable = false)
    private LocalDate feedbackDate;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
