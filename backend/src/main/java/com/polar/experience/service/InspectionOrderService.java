package com.polar.experience.service;

import com.polar.experience.dto.InspectionOrderCreateDTO;
import com.polar.experience.dto.InspectionOrderDTO;
import com.polar.experience.entity.Equipment;
import com.polar.experience.entity.InspectionOrder;
import com.polar.experience.enums.InspectionStatus;
import com.polar.experience.repository.EquipmentRepository;
import com.polar.experience.repository.InspectionOrderRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class InspectionOrderService {

    private static final DateTimeFormatter ORDER_NO_DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final InspectionOrderRepository inspectionOrderRepository;
    private final EquipmentRepository equipmentRepository;

    /** 自注入：撞号重试必须走代理的新事务，失败事务会整体回滚后才能再次尝试 */
    private final InspectionOrderService self;

    @Autowired
    public InspectionOrderService(InspectionOrderRepository inspectionOrderRepository,
                                  EquipmentRepository equipmentRepository,
                                  @Lazy InspectionOrderService self) {
        this.inspectionOrderRepository = inspectionOrderRepository;
        this.equipmentRepository = equipmentRepository;
        this.self = self;
    }

    /**
     * 馆务建单：器材必须为在册器材；已有待接单的器材不允许再生成新的待接单。
     * 外层方法不开事务，仅做前置校验；单号并发撞号时通过新事务重试。
     */
    public InspectionOrderDTO create(InspectionOrderCreateDTO dto) {
        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("所选器材不存在，请重新挑选"));
        if (equipment.getStatus() == null || equipment.getStatus() != 1) {
            throw new RuntimeException("所选器材已停用，不能送检");
        }

        if (inspectionOrderRepository.existsByEquipmentIdAndStatus(
                equipment.getId(), InspectionStatus.PENDING)) {
            throw new RuntimeException("该器材已有待接单的送检单，不能重复建单，请待修复后再送检");
        }

        DataIntegrityViolationException lastError = null;
        for (int attempt = 1; attempt <= 3; attempt++) {
            try {
                return self.attemptCreate(equipment, dto);
            } catch (DataIntegrityViolationException e) {
                lastError = e;
                log.warn("送检单号冲突，重新取号后重试（第{}次）", attempt);
            }
        }
        throw new RuntimeException("送检单号连续冲突，请稍后重新提交", lastError);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public InspectionOrderDTO attemptCreate(Equipment equipment, InspectionOrderCreateDTO dto) {
        InspectionOrder order = new InspectionOrder();
        order.setOrderNo(generateOrderNo());
        order.setEquipmentId(equipment.getId());
        order.setEquipmentNo(equipment.getEquipmentNo());
        order.setEquipmentName(equipment.getName());
        order.setColdResistanceSpec(equipment.getColdResistanceSpec());
        order.setInspectionNote(dto.getInspectionNote().trim());
        order.setOperator(dto.getOperator());
        order.setStatus(InspectionStatus.PENDING);

        InspectionOrder saved = inspectionOrderRepository.saveAndFlush(order);
        log.info("建立耐寒送检单: orderNo={}, equipmentNo={}", saved.getOrderNo(), saved.getEquipmentNo());
        return convertToDTO(saved);
    }

    /**
     * 点“标记已修复”：待接单立即变为已修复，记录修复时间；已是已修复则幂等返回。
     */
    @Transactional
    public InspectionOrderDTO markRepaired(Long id) {
        InspectionOrder order = inspectionOrderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("送检单不存在"));
        if (order.getStatus() != InspectionStatus.REPAIRED) {
            order.setStatus(InspectionStatus.REPAIRED);
            order.setRepairedAt(LocalDateTime.now());
            inspectionOrderRepository.save(order);
            log.info("送检单标记已修复: orderNo={}, equipmentNo={}", order.getOrderNo(), order.getEquipmentNo());
        }
        return convertToDTO(order);
    }

    public List<InspectionOrderDTO> query(Long equipmentId, InspectionStatus status) {
        List<InspectionOrder> orders;
        if (equipmentId != null && status != null) {
            orders = inspectionOrderRepository.findByEquipmentIdAndStatusOrderByCreatedAtDesc(equipmentId, status);
        } else if (equipmentId != null) {
            orders = inspectionOrderRepository.findByEquipmentIdOrderByCreatedAtDesc(equipmentId);
        } else if (status != null) {
            orders = inspectionOrderRepository.findByStatusOrderByCreatedAtDesc(status);
        } else {
            orders = inspectionOrderRepository.findAllByOrderByCreatedAtDesc();
        }
        return orders.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    private String generateOrderNo() {
        String datePart = LocalDateTime.now().format(ORDER_NO_DATE_FORMATTER);
        String prefix = "NH" + datePart + "-";
        long seq = inspectionOrderRepository.countByOrderNoStartingWith(prefix) + 1;
        return prefix + String.format("%03d", seq);
    }

    private InspectionOrderDTO convertToDTO(InspectionOrder order) {
        InspectionOrderDTO dto = new InspectionOrderDTO();
        dto.setId(order.getId());
        dto.setOrderNo(order.getOrderNo());
        dto.setEquipmentId(order.getEquipmentId());
        dto.setEquipmentNo(order.getEquipmentNo());
        dto.setEquipmentName(order.getEquipmentName());
        dto.setColdResistanceSpec(order.getColdResistanceSpec());
        dto.setInspectionNote(order.getInspectionNote());
        dto.setStatus(order.getStatus());
        dto.setOperator(order.getOperator());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setRepairedAt(order.getRepairedAt());
        return dto;
    }
}
