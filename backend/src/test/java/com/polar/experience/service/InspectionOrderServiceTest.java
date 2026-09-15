package com.polar.experience.service;

import com.polar.experience.dto.InspectionOrderCreateDTO;
import com.polar.experience.dto.InspectionOrderDTO;
import com.polar.experience.entity.Equipment;
import com.polar.experience.entity.InspectionOrder;
import com.polar.experience.enums.AgeGroup;
import com.polar.experience.enums.InspectionStatus;
import com.polar.experience.repository.EquipmentRepository;
import com.polar.experience.repository.InspectionOrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.startsWith;
import static org.mockito.Mockito.*;

class InspectionOrderServiceTest {

    private InspectionOrderRepository orderRepository;
    private EquipmentRepository equipmentRepository;
    private InspectionOrderService self;
    private InspectionOrderService service;

    @BeforeEach
    void setUp() {
        orderRepository = mock(InspectionOrderRepository.class);
        equipmentRepository = mock(EquipmentRepository.class);
        self = mock(InspectionOrderService.class);
        service = new InspectionOrderService(orderRepository, equipmentRepository, self);
    }

    private Equipment equipment() {
        Equipment e = new Equipment();
        e.setId(7L);
        e.setEquipmentNo("EQ-002");
        e.setName("成人防寒座椅");
        e.setColdResistanceSpec("-60°C至-20°C");
        e.setAgeGroup(AgeGroup.ADULT);
        e.setStatus(1);
        return e;
    }

    private InspectionOrderCreateDTO createDTO() {
        InspectionOrderCreateDTO dto = new InspectionOrderCreateDTO();
        dto.setEquipmentId(7L);
        dto.setInspectionNote("  实测低温下材质脆化，与-60°C标称规格不符，送第三方复测  ");
        dto.setOperator("王馆务");
        return dto;
    }

    @Test
    void create_rejectsWhenEquipmentAlreadyHasPendingOrder() {
        when(equipmentRepository.findById(7L)).thenReturn(Optional.of(equipment()));
        when(orderRepository.existsByEquipmentIdAndStatus(7L, InspectionStatus.PENDING)).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(createDTO()));
        assertTrue(ex.getMessage().contains("已有待接单"));
        verify(self, never()).attemptCreate(any(), any());
        verify(orderRepository, never()).saveAndFlush(any());
    }

    @Test
    void create_rejectsStoppedEquipment() {
        Equipment stopped = equipment();
        stopped.setStatus(0);
        when(equipmentRepository.findById(7L)).thenReturn(Optional.of(stopped));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.create(createDTO()));
        assertTrue(ex.getMessage().contains("已停用"));
        verify(self, never()).attemptCreate(any(), any());
    }

    @Test
    void create_delegatesToNewTransactionWhenNoPending() {
        when(equipmentRepository.findById(7L)).thenReturn(Optional.of(equipment()));
        when(orderRepository.existsByEquipmentIdAndStatus(7L, InspectionStatus.PENDING)).thenReturn(false);
        InspectionOrderDTO expected = new InspectionOrderDTO();
        when(self.attemptCreate(any(Equipment.class), any())).thenReturn(expected);

        assertSame(expected, service.create(createDTO()));
        verify(self).attemptCreate(argThat(e -> e.getId() == 7L && "EQ-002".equals(e.getEquipmentNo())), any());
    }

    @Test
    void attemptCreate_buildsPendingOrderWithSnapshotsAndSequentialOrderNo() {
        when(orderRepository.countByOrderNoStartingWith(startsWith("NH"))).thenReturn(2L);
        when(orderRepository.saveAndFlush(any(InspectionOrder.class))).thenAnswer(inv -> {
            InspectionOrder o = inv.getArgument(0);
            o.setId(99L);
            o.setCreatedAt(LocalDateTime.now());
            return o;
        });

        InspectionOrderDTO result = service.attemptCreate(equipment(), createDTO());

        ArgumentCaptor<InspectionOrder> captor = ArgumentCaptor.forClass(InspectionOrder.class);
        verify(orderRepository).saveAndFlush(captor.capture());
        InspectionOrder saved = captor.getValue();
        assertEquals(InspectionStatus.PENDING, saved.getStatus());
        assertEquals(7L, saved.getEquipmentId());
        assertEquals("EQ-002", saved.getEquipmentNo());
        assertEquals("成人防寒座椅", saved.getEquipmentName());
        assertEquals("-60°C至-20°C", saved.getColdResistanceSpec());
        assertEquals("实测低温下材质脆化，与-60°C标称规格不符，送第三方复测", saved.getInspectionNote());
        assertTrue(saved.getOrderNo().matches("NH\\d{8}-003"), "单号应为 NH+日期+三位序号，实际: " + saved.getOrderNo());
        assertEquals(99L, result.getId());
        assertNotNull(result.getCreatedAt());
    }

    @Test
    void markRepaired_flipsPendingToRepairedAndRecordsTime() {
        InspectionOrder order = new InspectionOrder();
        order.setId(5L);
        order.setOrderNo("NH20260915-001");
        order.setStatus(InspectionStatus.PENDING);
        when(orderRepository.findById(5L)).thenReturn(Optional.of(order));

        InspectionOrderDTO result = service.markRepaired(5L);

        assertEquals(InspectionStatus.REPAIRED, result.getStatus());
        assertNotNull(result.getRepairedAt());
        verify(orderRepository).save(order);
    }

    @Test
    void markRepaired_isIdempotentForAlreadyRepairedOrder() {
        LocalDateTime repairedAt = LocalDateTime.of(2026, 9, 15, 10, 0);
        InspectionOrder order = new InspectionOrder();
        order.setId(6L);
        order.setStatus(InspectionStatus.REPAIRED);
        order.setRepairedAt(repairedAt);
        when(orderRepository.findById(6L)).thenReturn(Optional.of(order));

        InspectionOrderDTO result = service.markRepaired(6L);

        assertEquals(InspectionStatus.REPAIRED, result.getStatus());
        assertEquals(repairedAt, result.getRepairedAt());
        verify(orderRepository, never()).save(any());
    }

    @Test
    void query_filtersByEquipmentAndStatus() {
        InspectionOrder o1 = new InspectionOrder();
        o1.setId(1L);
        o1.setStatus(InspectionStatus.PENDING);
        when(orderRepository.findByEquipmentIdAndStatusOrderByCreatedAtDesc(7L, InspectionStatus.PENDING))
                .thenReturn(List.of(o1));

        List<InspectionOrderDTO> result = service.query(7L, InspectionStatus.PENDING);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
    }
}
