package com.polar.experience.service;

import com.polar.experience.dto.SessionDTO;
import com.polar.experience.entity.Equipment;
import com.polar.experience.entity.Session;
import com.polar.experience.entity.SessionEquipment;
import com.polar.experience.enums.AgeGroup;
import com.polar.experience.repository.EquipmentRepository;
import com.polar.experience.repository.SessionEquipmentRepository;
import com.polar.experience.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionEquipmentRepository sessionEquipmentRepository;
    private final EquipmentRepository equipmentRepository;

    @Transactional
    public SessionDTO create(SessionDTO dto) {
        sessionRepository.findBySessionNo(dto.getSessionNo())
                .ifPresent(s -> {
                    throw new RuntimeException("场次编号已存在");
                });

        Session session = new Session();
        session.setSessionNo(dto.getSessionNo());
        session.setName(dto.getName());
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        session.setChildRatio(dto.getChildRatio() != null ? dto.getChildRatio() : 0);
        session.setAdultRatio(dto.getAdultRatio() != null ? dto.getAdultRatio() : 100);
        session.setElderlyRatio(dto.getElderlyRatio() != null ? dto.getElderlyRatio() : 0);
        session.setStatus(dto.getStatus() != null ? dto.getStatus() : 1);

        validateRatios(session.getChildRatio(), session.getAdultRatio(), session.getElderlyRatio());

        Session saved = sessionRepository.save(session);

        if (dto.getEquipmentIds() != null && !dto.getEquipmentIds().isEmpty()) {
            bindEquipments(saved.getId(), dto.getEquipmentIds());
        }

        log.info("创建场次: {}", saved.getSessionNo());
        return convertToDTO(saved);
    }

    @Transactional
    public SessionDTO update(Long id, SessionDTO dto) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("场次不存在"));

        if (!session.getSessionNo().equals(dto.getSessionNo())) {
            sessionRepository.findBySessionNo(dto.getSessionNo())
                    .ifPresent(s -> {
                        throw new RuntimeException("场次编号已存在");
                    });
            session.setSessionNo(dto.getSessionNo());
        }

        session.setName(dto.getName());
        session.setStartTime(dto.getStartTime());
        session.setEndTime(dto.getEndTime());
        if (dto.getChildRatio() != null) session.setChildRatio(dto.getChildRatio());
        if (dto.getAdultRatio() != null) session.setAdultRatio(dto.getAdultRatio());
        if (dto.getElderlyRatio() != null) session.setElderlyRatio(dto.getElderlyRatio());
        if (dto.getStatus() != null) session.setStatus(dto.getStatus());

        validateRatios(session.getChildRatio(), session.getAdultRatio(), session.getElderlyRatio());

        Session saved = sessionRepository.save(session);

        if (dto.getEquipmentIds() != null) {
            sessionEquipmentRepository.deleteBySessionId(id);
            bindEquipments(id, dto.getEquipmentIds());
        } else {
            // 未提交器材清单时，也要校验已有绑定与新主年龄段一致，保证刷新后配比与已绑器材不冲突
            validateEquipmentsMatchMainGroup(saved, getEquipmentIdsBySession(id));
        }

        log.info("更新场次: {}", saved.getSessionNo());
        return convertToDTO(saved);
    }

    @Transactional
    public void delete(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("场次不存在"));
        sessionEquipmentRepository.deleteBySessionId(id);
        sessionRepository.delete(session);
        log.info("删除场次: {}", session.getSessionNo());
    }

    public SessionDTO getById(Long id) {
        Session session = sessionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("场次不存在"));
        return convertToDTO(session);
    }

    public List<SessionDTO> getAll() {
        return sessionRepository.findByStatus(1).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public void bindEquipments(Long sessionId, List<Long> equipmentIds) {
        Session session = sessionRepository.findById(sessionId)
                .orElseThrow(() -> new RuntimeException("场次不存在"));

        // 先校验后绑定：任一器材与主年龄段不符即整体拦截，事务回滚，已绑名单保持不变
        validateEquipmentsMatchMainGroup(session, equipmentIds);

        for (Long equipmentId : equipmentIds) {
            if (!sessionEquipmentRepository.existsBySessionIdAndEquipmentId(sessionId, equipmentId)) {
                SessionEquipment se = new SessionEquipment();
                se.setSessionId(sessionId);
                se.setEquipmentId(equipmentId);
                sessionEquipmentRepository.save(se);
            }
        }
        log.info("绑定器材到场次: sessionId={}, equipmentCount={}", sessionId, equipmentIds.size());
    }

    /**
     * 校验人群配比：儿童、成人、老年三项均须在0~100之间，且之和必须等于100，否则不允许保存。
     */
    private void validateRatios(Integer childRatio, Integer adultRatio, Integer elderlyRatio) {
        int child = childRatio != null ? childRatio : 0;
        int adult = adultRatio != null ? adultRatio : 0;
        int elderly = elderlyRatio != null ? elderlyRatio : 0;

        if (child < 0 || child > 100 || adult < 0 || adult > 100 || elderly < 0 || elderly > 100) {
            throw new RuntimeException("儿童、成人、老年人群配比均须在0~100之间");
        }

        int sum = child + adult + elderly;
        if (sum != 100) {
            throw new RuntimeException("儿童、成人、老年人群配比之和必须等于100%（当前为" + sum + "%），不能保存");
        }
    }

    /**
     * 认定主年龄段：取人群配比中占比最高的一类；占比并列时按儿童、成人、老年顺序取前者。
     */
    public AgeGroup resolveMainAgeGroup(Session session) {
        int child = session.getChildRatio() != null ? session.getChildRatio() : 0;
        int adult = session.getAdultRatio() != null ? session.getAdultRatio() : 0;
        int elderly = session.getElderlyRatio() != null ? session.getElderlyRatio() : 0;

        if (child >= adult && child >= elderly) {
            return AgeGroup.CHILD;
        }
        if (adult >= elderly) {
            return AgeGroup.ADULT;
        }
        return AgeGroup.ELDERLY;
    }

    /**
     * 校验待绑器材清单：器材必须存在，且适配年龄段必须与场次主年龄段一致，否则拦截并提示。
     */
    private void validateEquipmentsMatchMainGroup(Session session, List<Long> equipmentIds) {
        if (equipmentIds == null || equipmentIds.isEmpty()) {
            return;
        }

        List<Equipment> equipments = equipmentRepository.findAllById(equipmentIds);
        Set<Long> foundIds = equipments.stream().map(Equipment::getId).collect(Collectors.toSet());
        List<Long> missingIds = equipmentIds.stream()
                .filter(equipmentId -> !foundIds.contains(equipmentId))
                .collect(Collectors.toList());
        if (!missingIds.isEmpty()) {
            throw new RuntimeException("器材不存在或已删除，无法绑定：ID " + missingIds);
        }

        AgeGroup mainAgeGroup = resolveMainAgeGroup(session);
        List<String> mismatched = equipments.stream()
                .filter(equipment -> equipment.getAgeGroup() != mainAgeGroup)
                .map(equipment -> equipment.getName() + "（适配" + getAgeGroupName(equipment.getAgeGroup()) + "）")
                .collect(Collectors.toList());
        if (!mismatched.isEmpty()) {
            throw new RuntimeException("绑定失败：本场次主年龄段为【" + getAgeGroupName(mainAgeGroup)
                    + "】，只能绑定该段适龄器材，以下器材不适配：" + String.join("、", mismatched));
        }
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

    public List<Long> getEquipmentIdsBySession(Long sessionId) {
        return sessionEquipmentRepository.findBySessionId(sessionId).stream()
                .map(SessionEquipment::getEquipmentId)
                .collect(Collectors.toList());
    }

    public List<Equipment> getEquipmentsBySession(Long sessionId) {
        List<Long> equipmentIds = getEquipmentIdsBySession(sessionId);
        return equipmentRepository.findAllById(equipmentIds);
    }

    private SessionDTO convertToDTO(Session session) {
        SessionDTO dto = new SessionDTO();
        dto.setId(session.getId());
        dto.setSessionNo(session.getSessionNo());
        dto.setName(session.getName());
        dto.setStartTime(session.getStartTime());
        dto.setEndTime(session.getEndTime());
        dto.setChildRatio(session.getChildRatio());
        dto.setAdultRatio(session.getAdultRatio());
        dto.setElderlyRatio(session.getElderlyRatio());
        dto.setStatus(session.getStatus());
        dto.setEquipmentIds(getEquipmentIdsBySession(session.getId()));
        dto.setMainAgeGroup(resolveMainAgeGroup(session));
        return dto;
    }
}