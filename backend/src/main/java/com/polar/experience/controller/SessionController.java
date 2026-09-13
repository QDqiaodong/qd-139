package com.polar.experience.controller;

import com.polar.experience.dto.SessionDTO;
import com.polar.experience.service.SessionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/session", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody SessionDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            SessionDTO result = sessionService.create(dto);
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
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @Valid @RequestBody SessionDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            SessionDTO result = sessionService.update(id, dto);
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
            sessionService.delete(id);
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
            SessionDTO result = sessionService.getById(id);
            response.put("success", true);
            response.put("data", result);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<SessionDTO> result = sessionService.getAll();
            response.put("success", true);
            response.put("data", result);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/bind")
    public ResponseEntity<Map<String, Object>> bindEquipments(@PathVariable Long sessionId, @RequestBody List<Long> equipmentIds) {
        Map<String, Object> response = new HashMap<>();
        try {
            sessionService.bindEquipments(sessionId, equipmentIds);
            response.put("success", true);
            response.put("message", "绑定成功");
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}