package com.polar.experience.service;

import com.polar.experience.dto.SessionDTO;
import com.polar.experience.entity.Equipment;
import com.polar.experience.entity.Session;
import com.polar.experience.entity.SessionEquipment;
import com.polar.experience.repository.EquipmentRepository;
import com.polar.experience.repository.SessionEquipmentRepository;
import com.polar.experience.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
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

        Session saved = sessionRepository.save(session);

        if (dto.getEquipmentIds() != null) {
            sessionEquipmentRepository.deleteBySessionId(id);
            bindEquipments(id, dto.getEquipmentIds());
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
        return dto;
    }
}