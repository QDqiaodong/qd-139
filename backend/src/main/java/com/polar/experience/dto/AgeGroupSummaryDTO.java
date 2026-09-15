package com.polar.experience.dto;

import com.polar.experience.enums.AgeGroup;
import lombok.Data;

import java.util.List;

@Data
public class AgeGroupSummaryDTO {

    private AgeGroup ageGroup;

    private String ageGroupName;

    /**
     * 在用器材名单（status=1）
     */
    private List<EquipmentDTO> activeEquipments;

    /**
     * 停用器材名单（status=0），对账时与器材列表保持一致，不从汇总中剔除
     */
    private List<EquipmentDTO> inactiveEquipments;

    /**
     * 在用件数
     */
    private Integer activeCount;

    /**
     * 停用件数
     */
    private Integer inactiveCount;

    /**
     * 库里实际挂着的总件数 = 在用 + 停用
     */
    private Integer totalCount;
}
