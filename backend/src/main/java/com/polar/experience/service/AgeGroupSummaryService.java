package com.polar.experience.service;

import com.polar.experience.dto.AgeGroupSummaryDTO;
import com.polar.experience.dto.EquipmentDTO;
import com.polar.experience.entity.Equipment;
import com.polar.experience.enums.AgeGroup;
import com.polar.experience.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AgeGroupSummaryService {

    private final EquipmentRepository equipmentRepository;

    public List<AgeGroupSummaryDTO> getSummaryByAllAgeGroups() {
        List<AgeGroupSummaryDTO> summaries = new ArrayList<>();

        for (AgeGroup ageGroup : AgeGroup.values()) {
            summaries.add(getSummaryByAgeGroup(ageGroup));
        }

        return summaries;
    }

    public AgeGroupSummaryDTO getSummaryByAgeGroup(AgeGroup ageGroup) {
        // 按年龄段取全部器材（含停用），再按状态拆分，停用件数不能从汇总里蒸发
        List<Equipment> allEquipments = equipmentRepository.findByAgeGroup(ageGroup);

        Map<Boolean, List<Equipment>> partitioned = allEquipments.stream()
                .collect(Collectors.partitioningBy(e -> Integer.valueOf(1).equals(e.getStatus())));
        List<Equipment> activeEquipments = partitioned.get(true);
        List<Equipment> inactiveEquipments = partitioned.get(false);

        AgeGroupSummaryDTO summary = new AgeGroupSummaryDTO();
        summary.setAgeGroup(ageGroup);
        summary.setAgeGroupName(getAgeGroupName(ageGroup));
        summary.setActiveEquipments(activeEquipments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        summary.setInactiveEquipments(inactiveEquipments.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList()));
        summary.setActiveCount(activeEquipments.size());
        summary.setInactiveCount(inactiveEquipments.size());
        summary.setTotalCount(allEquipments.size());

        return summary;
    }

    private String getAgeGroupName(AgeGroup ageGroup) {
        switch (ageGroup) {
            case CHILD:
                return "儿童";
            case ADULT:
                return "成人";
            case ELDERLY:
                return "老年";
            default:
                return "未知";
        }
    }

    private EquipmentDTO convertToDTO(Equipment equipment) {
        EquipmentDTO dto = new EquipmentDTO();
        dto.setId(equipment.getId());
        dto.setEquipmentNo(equipment.getEquipmentNo());
        dto.setName(equipment.getName());
        dto.setColdResistanceSpec(equipment.getColdResistanceSpec());
        dto.setAgeGroup(equipment.getAgeGroup());
        dto.setStatus(equipment.getStatus());
        return dto;
    }
}