package com.polar.experience.controller;

import com.polar.experience.dto.EquipmentDTO;
import com.polar.experience.enums.AgeGroup;
import com.polar.experience.service.EquipmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/equipment", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class EquipmentController {

    private final EquipmentService equipmentService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody EquipmentDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            EquipmentDTO result = equipmentService.create(dto);
            response.put("success", true);
            response.put("data", result);
            response.put("message", "创建成功");
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody EquipmentDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            EquipmentDTO result = equipmentService.update(id, dto);
            response.put("success", true);
            response.put("data", result);
            response.put("message", "更新成功");
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            equipmentService.delete(id);
            response.put("success", true);
            response.put("message", "删除成功");
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            EquipmentDTO result = equipmentService.getById(id);
            response.put("success", true);
            response.put("data", result);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll(@RequestParam(required = false) AgeGroup ageGroup) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<EquipmentDTO> result;
            if (ageGroup != null) {
                result = equipmentService.getByAgeGroup(ageGroup);
            } else {
                result = equipmentService.getAll();
            }
            response.put("success", true);
            response.put("data", result);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/cache/{equipmentNo}")
    public ResponseEntity<Map<String, Object>> getColdResistanceFromCache(@PathVariable String equipmentNo) {
        Map<String, Object> response = new HashMap<>();
        try {
            String result = equipmentService.getColdResistanceFromCache(equipmentNo);
            response.put("success", true);
            response.put("data", result);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}