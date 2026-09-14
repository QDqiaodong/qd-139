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
        // 先按普通读校验场次存在与状态；已停用的场次直接当作失效，不允许再变更年龄段
        Session session = sessionRepository.findById(dto.getSessionId())
                .orElseThrow(() -> new RuntimeException("场次不存在"));
        if (Integer.valueOf(0).equals(session.getStatus())) {
            throw new RuntimeException("场次【" + session.getSessionNo()
                    + "】已停用，按失效处理，不能再变更器材年龄段");
        }

        Equipment equipment = equipmentRepository.findById(dto.getEquipmentId())
                .orElseThrow(() -> new RuntimeException("器材不存在"));

        AgeGroup oldAgeGroup = equipment.getAgeGroup();
        AgeGroup newAgeGroup = dto.getNewAgeGroup();

        if (oldAgeGroup == newAgeGroup) {
            throw new RuntimeException("新年龄段与原年龄段相同");
        }

        // 落库前对场次加行锁并再次校验停用状态：
        // 与“馆务停用场次”互斥——若停用先提交，这里读到停用即整体回滚；
        // 若本变更先拿锁，停用操作须等本次提交后才能生效。两种情况下都不会给已停用场次写流水。
        Session lockedSession = sessionRepository.findByIdForUpdate(dto.getSessionId())
                .orElseThrow(() -> new RuntimeException("场次不存在"));
        if (Integer.valueOf(0).equals(lockedSession.getStatus())) {
            throw new RuntimeException("场次【" + lockedSession.getSessionNo()
                    + "】已停用，按失效处理，不能再变更器材年龄段");
        }

        equipment.setAgeGroup(newAgeGroup);
        // 立即刷库：器材带 @Version 乐观锁，两人同时改同一器材时，
        // 后提交者版本号过期，这里抛出 ObjectOptimisticLockingFailureException，
        // 事务回滚、不写流水，按操作冲突处理。
        equipmentRepository.saveAndFlush(equipment);

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
