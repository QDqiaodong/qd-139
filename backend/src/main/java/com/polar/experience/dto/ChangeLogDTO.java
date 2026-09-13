package com.polar.experience.dto;

import com.polar.experience.enums.AgeGroup;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ChangeLogDTO {

    @NotNull(message = "场次ID不能为空")
    private Long sessionId;

    @NotNull(message = "器材ID不能为空")
    private Long equipmentId;

    @NotNull(message = "新年龄段不能为空")
    private AgeGroup newAgeGroup;

    private String changeReason;

    private String operator;
}