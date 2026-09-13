package com.polar.experience.controller;

import com.polar.experience.dto.AgeGroupSummaryDTO;
import com.polar.experience.enums.AgeGroup;
import com.polar.experience.service.AgeGroupSummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/summary", produces = "application/json;charset=UTF-8")
@RequiredArgsConstructor
public class AgeGroupSummaryController {

    private final AgeGroupSummaryService ageGroupSummaryService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getSummaryByAllAgeGroups() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<AgeGroupSummaryDTO> result = ageGroupSummaryService.getSummaryByAllAgeGroups();
            response.put("success", true);
            response.put("data", result);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{ageGroup}")
    public ResponseEntity<Map<String, Object>> getSummaryByAgeGroup(@PathVariable AgeGroup ageGroup) {
        Map<String, Object> response = new HashMap<>();
        try {
            AgeGroupSummaryDTO result = ageGroupSummaryService.getSummaryByAgeGroup(ageGroup);
            response.put("success", true);
            response.put("data", result);
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("message", e.getMessage());
        }
        return ResponseEntity.ok(response);
    }
}