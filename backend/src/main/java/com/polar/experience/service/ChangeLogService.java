package com.polar.experience.service;

import com.polar.experience.dto.ChangeLogDTO;
import com.polar.experience.entity.*;
import com.polar.experience.enums.AgeGroup;
import com.polar.experience.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChangeLogService {

    private final EquipmentRepository equipmentRepository;
    private final SessionRepository sessionRepository;
    private final ChangeLogRepository changeLogRepository;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String REDIS_KEY_PREFIX = "equipment:cold_resistance:";

    @Transactional
    public ChangeLog changeAgeGroup(ChangeLogDTO dto) {
        Session session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new RuntimeException("场次不存在"));

        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("器材不存在"));

        AgeGroup oldAgeGroup = equipment.getAgeGroup();
        AgeGroup newAgeGroup = dto.getNewAgeGroup();

        if (oldAgeGroup == newAgeGroup) {
            throw new RuntimeException("新年龄段与原年龄段相同");
        }

        equipment.setAgeGroup(newAgeGroup);
        equipmentRepository.save(equipment);

        String key = REDIS_KEY_PREFIX + equipment.getEquipmentNo();
        redisTemplate.delete(key);

        ChangeLog changeLog = createChangeLog(dto, oldAgeGroup);
        changeLogRepository.save(changeLog);

        log.info("变更器材年龄段: equipmentNo={}, oldAgeGroup={}, newAgeGroup={}",
                equipment.getEquipmentNo(), oldAgeGroup, newAgeGroup);
        return changeLog;
    }

    private ChangeLog createChangeLog(ChangeLogDTO dto, AgeGroup oldAgeGroup) {
        ChangeLog changeLog;
        switch (dto.getNewAgeGroup()) {
            case CHILD:
                changeLog = new ChildChangeLog();
                break;
            case ADULT:
                changeLog = new AdultChangeLog();
                break;
            case ELDERLY:
                changeLog = new ElderlyChangeLog();
                break;
            default:
                throw new RuntimeException("未知年龄段");
        }
        changeLog.setSessionId(dto.getSessionId());
        changeLog.setEquipmentId(dto.getEquipmentId());
        changeLog.setOldAgeGroup(oldAgeGroup);
        changeLog.setNewAgeGroup(dto.getNewAgeGroup());
        changeLog.setChangeReason(dto.getChangeReason());
        changeLog.setOperator(dto.getOperator());
        return changeLog;
    }

    public List<ChangeLog> getLogsBySession(Long sessionId) {
        return changeLogRepository.findBySessionId(sessionId);
    }

    public List<ChangeLog> getLogsByEquipment(Long equipmentId) {
        return changeLogRepository.findByEquipmentId(equipmentId);
    }

    public List<ChangeLog> getLogsByAgeGroup(AgeGroup ageGroup) {
        return changeLogRepository.findByNewAgeGroup(ageGroup);
    }
}