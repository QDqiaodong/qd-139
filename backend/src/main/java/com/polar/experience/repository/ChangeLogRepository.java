package com.polar.experience.repository;

import com.polar.experience.entity.ChangeLog;
import com.polar.experience.enums.AgeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChangeLogRepository extends JpaRepository<ChangeLog, Long> {

    List<ChangeLog> findBySessionId(Long sessionId);

    List<ChangeLog> findByEquipmentId(Long equipmentId);

    List<ChangeLog> findByNewAgeGroup(AgeGroup ageGroup);

    List<ChangeLog> findBySessionIdAndNewAgeGroup(Long sessionId, AgeGroup ageGroup);
}