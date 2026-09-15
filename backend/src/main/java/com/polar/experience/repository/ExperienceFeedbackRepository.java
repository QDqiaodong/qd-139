package com.polar.experience.repository;

import com.polar.experience.entity.ExperienceFeedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ExperienceFeedbackRepository extends JpaRepository<ExperienceFeedback, Long> {

    List<ExperienceFeedback> findAllByOrderByCreatedAtDesc();

    List<ExperienceFeedback> findByFeedbackDateOrderByCreatedAtDesc(LocalDate feedbackDate);

    List<ExperienceFeedback> findBySessionIdOrderByCreatedAtDesc(Long sessionId);

    List<ExperienceFeedback> findBySessionIdAndFeedbackDateOrderByCreatedAtDesc(Long sessionId, LocalDate feedbackDate);

    /**
     * 同一天、同一场次、同一位游客是否已登记过（业务上当天只记一条）。
     */
    boolean existsBySessionIdAndVisitorNameAndFeedbackDate(Long sessionId, String visitorName, LocalDate feedbackDate);
}
