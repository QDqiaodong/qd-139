package com.polar.experience.dto;

import com.polar.experience.enums.InspectionStatus;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 送检台账行：状态只会是 PENDING（待接单）或 REPAIRED（已修复）。
 */
@Data
public class InspectionOrderDTO {

    private Long id;

    private String orderNo;

    private Long equipmentId;

    private String equipmentNo;

    private String equipmentName;

    private String coldResistanceSpec;

    private String inspectionNote;

    private InspectionStatus status;

    private String operator;

    private LocalDateTime createdAt;

    private LocalDateTime repairedAt;
}
