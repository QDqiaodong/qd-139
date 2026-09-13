package com.polar.experience.repository;

import com.polar.experience.entity.Equipment;
import com.polar.experience.enums.AgeGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    Optional<Equipment> findByEquipmentNo(String equipmentNo);

    List<Equipment> findByAgeGroup(AgeGroup ageGroup);

    List<Equipment> findByAgeGroupAndStatus(AgeGroup ageGroup, Integer status);

    List<Equipment> findByStatus(Integer status);
}