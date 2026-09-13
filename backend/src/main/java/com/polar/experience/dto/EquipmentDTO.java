package com.polar.experience.dto;

import com.polar.experience.enums.AgeGroup;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class EquipmentDTO {

    private Long id;

    @NotBlank(message = "器材编号不能为空")
    private String equipmentNo;

    @NotBlank(message = "器材名称不能为空")
    private String name;

    @NotBlank(message = "耐寒规格不能为空")
    private String coldResistanceSpec;

    @NotNull(message = "适配人群年龄段不能为空")
    private AgeGroup ageGroup;

    private Integer status;
}