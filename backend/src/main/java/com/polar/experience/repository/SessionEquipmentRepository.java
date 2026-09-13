package com.polar.experience.repository;

import com.polar.experience.entity.SessionEquipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SessionEquipmentRepository extends JpaRepository<SessionEquipment, Long> {

    List<SessionEquipment> findBySessionId(Long sessionId);

    void deleteBySessionId(Long sessionId);

    boolean existsBySessionIdAndEquipmentId(Long sessionId, Long equipmentId);
}