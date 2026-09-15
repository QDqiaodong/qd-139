package com.polar.experience.service;

import com.polar.experience.dto.ExperienceFeedbackCreateDTO;
import com.polar.experience.dto.ExperienceFeedbackDTO;
import com.polar.experience.entity.ExperienceFeedback;
import com.polar.experience.entity.Session;
import com.polar.experience.repository.ExperienceFeedbackRepository;
import com.polar.experience.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExperienceFeedbackService {

    private final ExperienceFeedbackRepository feedbackRepository;
    private final SessionRepository sessionRepository;

    /**
     * 散场登记：场次须在用；同一天、同一场次、同一位游客只记一条。
     * 先查后写，数据库唯一约束兜底两人同时提交同一位游客的情况。
     */
    @Transactional
    public ExperienceFeedbackDTO create(ExperienceFeedbackCreateDTO dto) {
        Session session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new RuntimeException("所选场次不存在，请重新选择"));
        if (session.getStatus() == null || session.getStatus() != 1) {
            throw new RuntimeException("所选场次已停用，不能登记");
        }

        String visitorName = normalizeVisitorName(dto.getVisitorName());
        LocalDate feedbackDate = dto.getFeedbackDate() != null ? dto.getFeedbackDate() : LocalDate.now();

        if (feedbackRepository.existsBySessionIdAndVisitorNameAndFeedbackDate(
                session.getId(), visitorName, feedbackDate)) {
            throw duplicateException(visitorName, session.getName());
        }

        ExperienceFeedback feedback = new ExperienceFeedback();
        feedback.setSessionId(session.getId());
        feedback.setSessionNo(session.getSessionNo());
        feedback.setSessionName(session.getName());
        feedback.setVisitorName(visitorName);
        feedback.setTempFeeling(dto.getTempFeeling());
        feedback.setNumbness(dto.getNumbness());
        feedback.setRegistrar(dto.getRegistrar().trim());
        feedback.setFeedbackDate(feedbackDate);

        try {
            ExperienceFeedback saved = feedbackRepository.saveAndFlush(feedback);
            log.info("体验感受登记: sessionNo={}, visitor={}, date={}, tempFeeling={}, numbness={}, registrar={}",
                    saved.getSessionNo(), saved.getVisitorName(), saved.getFeedbackDate(),
                    saved.getTempFeeling(), saved.getNumbness(), saved.getRegistrar());
            return convertToDTO(saved);
        } catch (DataIntegrityViolationException e) {
            // 并发撞上唯一索引：拦下并转成口头能懂的提示
            throw duplicateException(visitorName, session.getName());
        }
    }

    /**
     * 名单查询：可按场次、按登记日期筛选，两个条件可单独或组合使用。
     */
    public List<ExperienceFeedbackDTO> query(Long sessionId, LocalDate feedbackDate) {
        List<ExperienceFeedback> list;
        if (sessionId != null && feedbackDate != null) {
            list = feedbackRepository.findBySessionIdAndFeedbackDateOrderByCreatedAtDesc(sessionId, feedbackDate);
        } else if (sessionId != null) {
            list = feedbackRepository.findBySessionIdOrderByCreatedAtDesc(sessionId);
        } else if (feedbackDate != null) {
            list = feedbackRepository.findByFeedbackDateOrderByCreatedAtDesc(feedbackDate);
        } else {
            list = feedbackRepository.findAllByOrderByCreatedAtDesc();
        }
        return list.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    /** 游客称呼归一化：去首尾空格、中间连续空格并成一个，避免同一人因空格不同被记成两条 */
    private String normalizeVisitorName(String name) {
        return name == null ? "" : name.trim().replaceAll("\\s+", " ");
    }

    private RuntimeException duplicateException(String visitorName, String sessionName) {
        return new RuntimeException("「" + visitorName + "」今天在「" + sessionName
                + "」已登记过，同一场同一游客当天只记一条");
    }

    private ExperienceFeedbackDTO convertToDTO(ExperienceFeedback feedback) {
        ExperienceFeedbackDTO dto = new ExperienceFeedbackDTO();
        dto.setId(feedback.getId());
        dto.setSessionId(feedback.getSessionId());
        dto.setSessionNo(feedback.getSessionNo());
        dto.setSessionName(feedback.getSessionName());
        dto.setVisitorName(feedback.getVisitorName());
        dto.setTempFeeling(feedback.getTempFeeling());
        dto.setNumbness(feedback.getNumbness());
        dto.setRegistrar(feedback.getRegistrar());
        dto.setFeedbackDate(feedback.getFeedbackDate());
        dto.setCreatedAt(feedback.getCreatedAt());
        return dto;
    }
}
