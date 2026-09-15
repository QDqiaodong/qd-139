package com.polar.experience.controller;

import com.polar.experience.dto.InspectionOrderCreateDTO;
import com.polar.experience.dto.InspectionOrderDTO;
import com.polar.experience.enums.InspectionStatus;
import com.polar.experience.service.InspectionOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/inspection", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class InspectionOrderController {

    private final InspectionOrderService inspectionOrderService;

    /**
     * 台账查询：可按器材、按状态筛选，两个条件可单独或组合使用。
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> query(
            @RequestParam(required = false) Long equipmentId,
            @RequestParam(required = false) InspectionStatus status) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<InspectionOrderDTO> result = inspectionOrderService.query(equipmentId, status);
            response.put("success", true);
            response.put("data", result);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody InspectionOrderCreateDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            InspectionOrderDTO result = inspectionOrderService.create(dto);
            response.put("success", true);
            response.put("data", result);
            response.put("message", "送检单已建立，状态为待接单");
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 标记已修复：该单状态立即变为已修复。
     */
    @PutMapping("/{id}/repair")
    public ResponseEntity<Map<String, Object>> markRepaired(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            InspectionOrderDTO result = inspectionOrderService.markRepaired(id);
            response.put("success", true);
            response.put("data", result);
            response.put("message", "已标记为已修复");
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}
