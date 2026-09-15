package com.polar.experience.controller;

import com.polar.experience.dto.ExperienceFeedbackCreateDTO;
import com.polar.experience.dto.ExperienceFeedbackDTO;
import com.polar.experience.service.ExperienceFeedbackService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/feedback", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class ExperienceFeedbackController {

    private final ExperienceFeedbackService feedbackService;

    /**
     * 名单查询：可按场次、按登记日期筛选，两个条件可单独或组合使用。
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> query(
            @RequestParam(required = false) Long sessionId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<ExperienceFeedbackDTO> result = feedbackService.query(sessionId, date);
            response.put("success", true);
            response.put("data", result);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    /**
     * 散场登记：同一场次、同一位游客当天重复提交会被拦下。
     */
    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@Valid @RequestBody ExperienceFeedbackCreateDTO dto) {
        Map<String, Object> response = new HashMap<>();
        try {
            ExperienceFeedbackDTO result = feedbackService.create(dto);
            response.put("success", true);
            response.put("data", result);
            response.put("message", "已登记");
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}
