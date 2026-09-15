package com.polar.experience.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 馆务建单入参：挑选器材并填写送检说明。
 */
@Data
public class InspectionOrderCreateDTO {

    @NotNull(message = "请选择送检器材")
    private Long equipmentId;

    @NotBlank(message = "送检说明不能为空")
    @Size(max = 500, message = "送检说明不能超过500字")
    private String inspectionNote;

    private String operator;
}
