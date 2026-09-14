package com.polar.experience.controller;

import com.polar.experience.dto.NotificationDTO;
import com.polar.experience.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/notification", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> list() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<NotificationDTO> result = notificationService.listForDutyStaff();
            response.put("success", true);
            response.put("data", result);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Object>> unreadCount() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("data", notificationService.unreadCountForDutyStaff());
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Map<String, Object>> markRead(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            NotificationDTO result = notificationService.markRead(id);
            response.put("success", true);
            response.put("data", result);
            response.put("message", "已标记为已读");
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}
