package com.polar.experience.controller;

import com.polar.experience.dto.ChangeLogDTO;
import com.polar.experience.entity.ChangeLog;
import com.polar.experience.enums.AgeGroup;
import com.polar.experience.service.ChangeLogService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/change-log", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class ChangeLogController {

    private final ChangeLogService changeLogService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> changeAgeGroup(@Valid @RequestBody ChangeLogDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            ChangeLog result = changeLogService.changeAgeGroup(dto);
            response.put("success", true);
            response.put("data", result);
            response.put("message", "变更成功");
            return ResponseEntity.ok(response);
        } catch (ObjectOptimisticLockingFailureException e) {
            // 同一器材被两人同时变更：先提交者成功，后提交者版本过期，按冲突处理、不写流水
            response.put("success", false);
            response.put("conflict", true);
            response.put("message", "操作冲突：该器材的年龄段刚刚已被其他人变更，请刷新后重试，本次变更未生效");
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
            return ResponseEntity.ok(response);
        }
    }

    @GetMapping("/session/{sessionId}")
    public ResponseEntity<Map<String, Object>> getLogsBySession(@PathVariable Long sessionId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ChangeLog> result = changeLogService.getLogsBySession(sessionId);
            response.put("success", true);
            response.put("data", result);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/equipment/{equipmentId}")
    public ResponseEntity<Map<String, Object>> getLogsByEquipment(@PathVariable Long equipmentId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ChangeLog> result = changeLogService.getLogsByEquipment(equipmentId);
            response.put("success", true);
            response.put("data", result);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/age-group/{ageGroup}")
    public ResponseEntity<Map<String, Object>> getLogsByAgeGroup(@PathVariable AgeGroup ageGroup) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ChangeLog> result = changeLogService.getLogsByAgeGroup(ageGroup);
            response.put("success", true);
            response.put("data", result);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}