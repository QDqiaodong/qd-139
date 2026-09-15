package com.polar.experience.repository;

import com.polar.experience.enums.InspectionStatus;
import com.polar.experience.entity.InspectionOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InspectionOrderRepository extends JpaRepository<InspectionOrder, Long> {

    List<InspectionOrder> findAllByOrderByCreatedAtDesc();

    List<InspectionOrder> findByStatusOrderByCreatedAtDesc(InspectionStatus status);

    List<InspectionOrder> findByEquipmentIdOrderByCreatedAtDesc(Long equipmentId);

    List<InspectionOrder> findByEquipmentIdAndStatusOrderByCreatedAtDesc(Long equipmentId, InspectionStatus status);

    boolean existsByEquipmentIdAndStatus(Long equipmentId, InspectionStatus status);

    /**
     * 取该器材当前的待接单（业务上同一器材同时只允许一张待接单）。
     */
    Optional<InspectionOrder> findFirstByEquipmentIdAndStatus(Long equipmentId, InspectionStatus status);

    long countByOrderNoStartingWith(String prefix);
}
