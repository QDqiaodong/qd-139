package com.polar.experience.dto;

import com.polar.experience.enums.AgeGroup;
import lombok.Data;

import java.util.List;

@Data
public class AgeGroupSummaryDTO {

    private AgeGroup ageGroup;

    private String ageGroupName;

    private List<EquipmentDTO> equipments;

    private Integer totalCount;
}