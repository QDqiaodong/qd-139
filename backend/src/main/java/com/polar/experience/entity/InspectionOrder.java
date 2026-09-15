package com.polar.experience.entity;

import com.polar.experience.enums.InspectionStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 耐寒送检单：馆务挑选耐寒规格有偏差的器材建立，随单写明送检说明。
 * 状态只有“待接单 PENDING / 已修复 REPAIRED”两种。
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "inspection_order")
public class InspectionOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** 送检单号：NH + 日期 + 当日序号，便于馆务口头核对 */
    @Column(name = "order_no", nullable = false, unique = true, length = 50)
    private String orderNo;

    /** 器材信息在建单时快照留存，器材档案后续变动不影响历史台账 */
    @Column(name = "equipment_id", nullable = false)
    private Long equipmentId;

    @Column(name = "equipment_no", nullable = false, length = 50)
    private String equipmentNo;

    @Column(name = "equipment_name", nullable = false, length = 100)
    private String equipmentName;

    @Column(name = "cold_resistance_spec", length = 100)
    private String coldResistanceSpec;

    /** 送检说明：写明耐寒规格偏差情况与送检要求 */
    @Column(name = "inspection_note", nullable = false, length = 500)
    private String inspectionNote;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private InspectionStatus status = InspectionStatus.PENDING;

    /** 建单馆务，用于口头核对时知道找谁 */
    @Column(name = "operator", length = 50)
    private String operator;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "repaired_at")
    private LocalDateTime repairedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
