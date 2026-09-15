package com.polar.experience.service;

import com.polar.experience.dto.EquipmentDTO;
import com.polar.experience.entity.Equipment;
import com.polar.experience.enums.AgeGroup;
import com.polar.experience.repository.EquipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_LIST_KEY = "equipment:cold_resistance_list";
    private static final String REDIS_KEY_PREFIX = "equipment:cold_resistance:";

    @Transactional
    public EquipmentDTO create(EquipmentDTO dto) {
        equipmentRepository.findByEquipmentNo(dto.getEquipmentNo())
                .ifPresent(e -> {
                    throw new RuntimeException("器材编号已存在");
                });

        Equipment equipment = new Equipment();
        equipment.setEquipmentNo(dto.getEquipmentNo());
        equipment.setName(dto.getName());
        equipment.setColdResistanceSpec(dto.getColdResistanceSpec());
        equipment.setAgeGroup(dto.getAgeGroup());
        equipment.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        Equipment saved = equipmentRepository.save(equipment);
        updateRedisCache(saved);
        log.info("创建器材: {}", saved.getEquipmentNo());
        return convertToDTO(saved);
    }

    @Transactional
    public EquipmentDTO update(Long id, EquipmentDTO dto) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("器材不存在"));

        if (!equipment.getEquipmentNo().equals(dto.getEquipmentNo())) {
            equipmentRepository.findByEquipmentNo(dto.getEquipmentNo())
                    .ifPresent(e -> {
                        throw new RuntimeException("器材编号已存在");
                    });
            equipment.setEquipmentNo(dto.getEquipmentNo());
        }

        equipment.setName(dto.getName());
        equipment.setColdResistanceSpec(dto.getColdResistanceSpec());
        equipment.setAgeGroup(dto.getAgeGroup());
        if (dto.getStatus() != null) {
            equipment.setStatus(dto.getStatus());
        }

        // 并发编辑同一器材时，flush 立即触发 @Version 乐观锁冲突，由控制器转成“操作冲突”提示
        Equipment saved = equipmentRepository.saveAndFlush(equipment);
        updateRedisCache(saved);
        log.info("更新器材: {}", saved.getEquipmentNo());
        return convertToDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("器材不存在"));
        equipmentRepository.delete(equipment);
        redisTemplate.delete(REDIS_KEY_PREFIX + equipment.getEquipmentNo());
        log.info("删除器材: {}", equipment.getEquipmentNo());
    }

    public EquipmentDTO getById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("器材不存在"));
        return convertToDTO(equipment);
    }

    /**
     * 器材管理列表：返回库里全部器材（含停用），供页面对账
     */
    public List<EquipmentDTO> getAll() {
        return equipmentRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EquipmentDTO> getByAgeGroup(AgeGroup ageGroup) {
        return equipmentRepository.findByAgeGroup(ageGroup).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<EquipmentDTO> getByAgeGroupAndStatus(AgeGroup ageGroup, Integer status) {
        return equipmentRepository.findByAgeGroupAndStatus(ageGroup, status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    /**
     * 场次绑定、送检等业务选择器使用：只挑在用器材
     */
    public List<EquipmentDTO> getByStatus(Integer status) {
        return equipmentRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    private void updateRedisCache(Equipment equipment) {
        String item = equipment.getEquipmentNo() + ":" + equipment.getColdResistanceSpec();
        redisTemplate.opsForList().leftPush(REDIS_LIST_KEY, item);
        String key = REDIS_KEY_PREFIX + equipment.getEquipmentNo();
        redisTemplate.opsForValue().set(key, equipment.getColdResistanceSpec());
    }

    public String getColdResistanceFromCache(String equipmentNo) {
        String key = REDIS_KEY_PREFIX + equipmentNo;
        Object value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            Equipment equipment = equipmentRepository.findByEquipmentNo(equipmentNo)
                    .orElse(null);
            if (equipment != null) {
                updateRedisCache(equipment);
                return equipment.getColdResistanceSpec();
            }
            return null;
        }
        return value.toString();
    }

    public List<String> getAllColdResistanceFromCache() {
        return redisTemplate.opsForList().range(REDIS_LIST_KEY, 0, -1)
                .stream()
                .map(Object::toString)
                .collect(Collectors.toList());
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