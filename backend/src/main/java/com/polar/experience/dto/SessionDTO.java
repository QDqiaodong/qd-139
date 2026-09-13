package com.polar.experience.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class SessionDTO {

    private Long id;

    @NotBlank(message = "场次编号不能为空")
    private String sessionNo;

    @NotBlank(message = "场次名称不能为空")
    private String name;

    @NotNull(message = "开始时间不能为空")
    private LocalDateTime startTime;

    @NotNull(message = "结束时间不能为空")
    private LocalDateTime endTime;

    private Integer childRatio;

    private Integer adultRatio;

    private Integer elderlyRatio;

    private Integer status;

    private List<Long> equipmentIds;
}