package com.polar.experience.service;

import com.polar.experience.dto.SessionDTO;
import com.polar.experience.entity.Session;
import com.polar.experience.repository.EquipmentRepository;
import com.polar.experience.repository.SessionEquipmentRepository;
import com.polar.experience.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class SessionServiceTest {

    private SessionRepository sessionRepository;
    private SessionEquipmentRepository sessionEquipmentRepository;
    private EquipmentRepository equipmentRepository;
    private NotificationService notificationService;
    private SessionService service;

    @BeforeEach
    void setUp() {
        sessionRepository = mock(SessionRepository.class);
        sessionEquipmentRepository = mock(SessionEquipmentRepository.class);
        equipmentRepository = mock(EquipmentRepository.class);
        notificationService = mock(NotificationService.class);
        service = new SessionService(sessionRepository, sessionEquipmentRepository,
                equipmentRepository, notificationService);

        when(sessionRepository.findBySessionNo(any())).thenReturn(Optional.empty());
        when(sessionRepository.findByStatus(1)).thenReturn(List.of(existingSession()));
        when(sessionEquipmentRepository.findBySessionId(any())).thenReturn(List.of());
        when(sessionRepository.save(any(Session.class))).thenAnswer(inv -> {
            Session s = inv.getArgument(0);
            if (s.getId() == null) {
                s.setId(100L);
            }
            return s;
        });
    }

    private Session existingSession() {
        Session s = new Session();
        s.setId(1L);
        s.setSessionNo("S001");
        s.setName("上午极地体验场");
        s.setStartTime(LocalDateTime.of(2026, 9, 15, 9, 0, 0));
        s.setEndTime(LocalDateTime.of(2026, 9, 15, 11, 0, 0));
        s.setChildRatio(0);
        s.setAdultRatio(100);
        s.setElderlyRatio(0);
        s.setStatus(1);
        return s;
    }

    private SessionDTO dto(LocalDateTime start, LocalDateTime end) {
        SessionDTO dto = new SessionDTO();
        dto.setSessionNo("S002");
        dto.setName("新增场次");
        dto.setStartTime(start);
        dto.setEndTime(end);
        dto.setChildRatio(0);
        dto.setAdultRatio(100);
        dto.setElderlyRatio(0);
        dto.setStatus(1);
        return dto;
    }

    @Test
    void create_rejectsWhenTimeOverlapsExistingSession() {
        // 10:30~12:00 与 09:00~11:00 叠在 10:30~11:00
        SessionDTO dto = dto(
                LocalDateTime.of(2026, 9, 15, 10, 30, 0),
                LocalDateTime.of(2026, 9, 15, 12, 0, 0));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(dto));
        String message = ex.getMessage();
        assertTrue(message.contains("上午极地体验场"), "应写明撞了哪一场：" + message);
        assertTrue(message.contains("S001"), "应写明对方场次编号：" + message);
        assertTrue(message.contains("2026-09-15 09:00:00") && message.contains("2026-09-15 11:00:00"),
                "应写明对方从几点到几点：" + message);
        assertTrue(message.contains("2026-09-15 10:30:00 至 2026-09-15 11:00:00"),
                "应写明实际冲突时段：" + message);
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void create_allowsBackToBackSessions() {
        // 11:00 恰好接在 09:00~11:00 后面，首尾相接不算重叠
        SessionDTO dto = dto(
                LocalDateTime.of(2026, 9, 15, 11, 0, 0),
                LocalDateTime.of(2026, 9, 15, 12, 0, 0));

        SessionDTO result = service.create(dto);
        assertEquals(100L, result.getId());
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void update_rejectsWhenMovedIntoAnotherSessionSlot() {
        // 把 S002 原本在下午的场次改到上午，撞上 S001；编辑时自身不在候选列表中（S002 另查一次模拟）
        Session other = new Session();
        other.setId(2L);
        other.setSessionNo("S002");
        other.setName("下午极地体验场");
        other.setStartTime(LocalDateTime.of(2026, 9, 15, 14, 0, 0));
        other.setEndTime(LocalDateTime.of(2026, 9, 15, 16, 0, 0));
        other.setAdultRatio(100);
        other.setStatus(1);
        when(sessionRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(other));

        SessionDTO dto = dto(
                LocalDateTime.of(2026, 9, 15, 10, 0, 0),
                LocalDateTime.of(2026, 9, 15, 10, 30, 0));
        dto.setSessionNo("S002");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.update(2L, dto));
        assertTrue(ex.getMessage().contains("S001"));
        assertTrue(ex.getMessage().contains("上午极地体验场"));
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void update_allowsKeepingOwnTimeSlot() {
        Session self = existingSession();
        when(sessionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(self));

        SessionDTO dto = dto(
                LocalDateTime.of(2026, 9, 15, 9, 0, 0),
                LocalDateTime.of(2026, 9, 15, 11, 0, 0));
        dto.setSessionNo("S001");

        assertDoesNotThrow(() -> service.update(1L, dto));
        verify(sessionRepository).save(any(Session.class));
    }

    @Test
    void update_disablingSessionSkipsOverlapCheck() {
        // 停用场次不再占用时段，即使列表里存在历史重叠数据，停用操作也应放行
        Session self = existingSession();
        when(sessionRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(self));
        when(sessionRepository.findByStatus(1)).thenReturn(List.of(existingSession(), existingSession()));

        SessionDTO dto = dto(
                LocalDateTime.of(2026, 9, 15, 9, 0, 0),
                LocalDateTime.of(2026, 9, 15, 11, 0, 0));
        dto.setSessionNo("S001");
        dto.setStatus(0);

        assertDoesNotThrow(() -> service.update(1L, dto));
        verify(sessionRepository, never()).findByStatus(any());
        verify(notificationService).notifySessionDisabled(any(Session.class));
    }
}
