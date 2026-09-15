package com.polar.experience.dto;

import com.polar.experience.enums.NumbnessLevel;
import com.polar.experience.enums.TempFeeling;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 体验感受名单行：登记日期、场次快照、游客感受与登记人。
 */
@Data
public class ExperienceFeedbackDTO {

    private Long id;

    private Long sessionId;

    private String sessionNo;

    private String sessionName;

    private String visitorName;

    private TempFeeling tempFeeling;

    private NumbnessLevel numbness;

    private String registrar;

    private LocalDate feedbackDate;

    private LocalDateTime createdAt;
}
