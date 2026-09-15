package com.polar.experience.service;

import com.polar.experience.dto.ExperienceFeedbackCreateDTO;
import com.polar.experience.dto.ExperienceFeedbackDTO;
import com.polar.experience.entity.ExperienceFeedback;
import com.polar.experience.entity.Session;
import com.polar.experience.enums.NumbnessLevel;
import com.polar.experience.enums.TempFeeling;
import com.polar.experience.repository.ExperienceFeedbackRepository;
import com.polar.experience.repository.SessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ExperienceFeedbackServiceTest {

    private ExperienceFeedbackRepository feedbackRepository;
    private SessionRepository sessionRepository;
    private ExperienceFeedbackService service;

    @BeforeEach
    void setUp() {
        feedbackRepository = mock(ExperienceFeedbackRepository.class);
        sessionRepository = mock(SessionRepository.class);
        service = new ExperienceFeedbackService(feedbackRepository, sessionRepository);
    }

    private Session session() {
        Session s = new Session();
        s.setId(3L);
        s.setSessionNo("SESS-001");
        s.setName("上午场-家庭体验");
        s.setStatus(1);
        return s;
    }

    private ExperienceFeedbackCreateDTO createDTO() {
        ExperienceFeedbackCreateDTO dto = new ExperienceFeedbackCreateDTO();
        dto.setSessionId(3L);
        dto.setVisitorName("  张女士  ");
        dto.setTempFeeling(TempFeeling.COLD);
        dto.setNumbness(NumbnessLevel.MILD);
        dto.setRegistrar(" 李馆务 ");
        dto.setFeedbackDate(LocalDate.of(2026, 9, 15));
        return dto;
    }

    @Test
    void create_rejectsWhenSessionMissing() {
        when(sessionRepository.findById(3L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(createDTO()));
        assertTrue(ex.getMessage().contains("场次不存在"));
        verify(feedbackRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_rejectsDisabledSession() {
        Session disabled = session();
        disabled.setStatus(0);
        when(sessionRepository.findById(3L)).thenReturn(Optional.of(disabled));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(createDTO()));
        assertTrue(ex.getMessage().contains("已停用"));
        verify(feedbackRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_rejectsDuplicateSameVisitorSameSessionSameDay() {
        when(sessionRepository.findById(3L)).thenReturn(Optional.of(session()));
        when(feedbackRepository.existsBySessionIdAndVisitorNameAndFeedbackDate(
                3L, "张女士", LocalDate.of(2026, 9, 15))).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(createDTO()));
        assertTrue(ex.getMessage().contains("已登记过"));
        verify(feedbackRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_savesSnapshotAndNormalizesNames() {
        when(sessionRepository.findById(3L)).thenReturn(Optional.of(session()));
        when(feedbackRepository.existsBySessionIdAndVisitorNameAndFeedbackDate(any(), anyString(), any()))
                .thenReturn(false);
        when(feedbackRepository.saveAndFlush(any(ExperienceFeedback.class))).thenAnswer(inv -> {
            ExperienceFeedback f = inv.getArgument(0);
            f.setId(11L);
            f.setCreatedAt(LocalDateTime.now());
            return f;
        });

        ExperienceFeedbackDTO result = service.create(createDTO());

        ArgumentCaptor<ExperienceFeedback> captor = ArgumentCaptor.forClass(ExperienceFeedback.class);
        verify(feedbackRepository).saveAndFlush(captor.capture());
        ExperienceFeedback saved = captor.getValue();
        assertEquals(3L, saved.getSessionId());
        assertEquals("SESS-001", saved.getSessionNo());
        assertEquals("上午场-家庭体验", saved.getSessionName());
        assertEquals("张女士", saved.getVisitorName());
        assertEquals(TempFeeling.COLD, saved.getTempFeeling());
        assertEquals(NumbnessLevel.MILD, saved.getNumbness());
        assertEquals("李馆务", saved.getRegistrar());
        assertEquals(LocalDate.of(2026, 9, 15), saved.getFeedbackDate());
        assertEquals(11L, result.getId());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void create_defaultsFeedbackDateToTodayWhenNotGiven() {
        when(sessionRepository.findById(3L)).thenReturn(Optional.of(session()));
        when(feedbackRepository.existsBySessionIdAndVisitorNameAndFeedbackDate(any(), anyString(), any()))
                .thenReturn(false);
        when(feedbackRepository.saveAndFlush(any(ExperienceFeedback.class))).thenAnswer(inv -> inv.getArgument(0));

        ExperienceFeedbackCreateDTO dto = createDTO();
        dto.setFeedbackDate(null);
        service.create(dto);

        ArgumentCaptor<ExperienceFeedback> captor = ArgumentCaptor.forClass(ExperienceFeedback.class);
        verify(feedbackRepository).saveAndFlush(captor.capture());
        assertEquals(LocalDate.now(), captor.getValue().getFeedbackDate());
    }

    @Test
    void create_allowsSameVisitorOnDifferentDay() {
        when(sessionRepository.findById(3L)).thenReturn(Optional.of(session()));
        // 换一天后该游客在本场没有记录 → 允许再记
        when(feedbackRepository.existsBySessionIdAndVisitorNameAndFeedbackDate(
                3L, "张女士", LocalDate.of(2026, 9, 16))).thenReturn(false);
        when(feedbackRepository.saveAndFlush(any(ExperienceFeedback.class))).thenAnswer(inv -> inv.getArgument(0));

        ExperienceFeedbackCreateDTO dto = createDTO();
        dto.setFeedbackDate(LocalDate.of(2026, 9, 16));
        assertDoesNotThrow(() -> service.create(dto));
        verify(feedbackRepository).saveAndFlush(any());
    }

    @Test
    void create_translatesConcurrentUniqueViolationToFriendlyMessage() {
        when(sessionRepository.findById(3L)).thenReturn(Optional.of(session()));
        when(feedbackRepository.existsBySessionIdAndVisitorNameAndFeedbackDate(any(), anyString(), any()))
                .thenReturn(false);
        when(feedbackRepository.saveAndFlush(any(ExperienceFeedback.class)))
                .thenThrow(new DataIntegrityViolationException("Duplicate entry"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(createDTO()));
        assertTrue(ex.getMessage().contains("已登记过"));
    }

    @Test
    void query_filtersBySessionAndDate() {
        ExperienceFeedback f = new ExperienceFeedback();
        f.setId(1L);
        LocalDate date = LocalDate.of(2026, 9, 15);
        when(feedbackRepository.findBySessionIdAndFeedbackDateOrderByCreatedAtDesc(3L, date))
                .thenReturn(List.of(f));

        List<ExperienceFeedbackDTO> result = service.query(3L, date);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
