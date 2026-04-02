package com.medical.system.controller;

import com.medical.system.common.Result;
import com.medical.system.service.AiHealthAnalysisService;
import com.medical.system.vo.AiHealthAnalysisVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/ai-health")
public class AiHealthAnalysisController {

    private final AiHealthAnalysisService aiHealthAnalysisService;

    public AiHealthAnalysisController(AiHealthAnalysisService aiHealthAnalysisService) {
        this.aiHealthAnalysisService = aiHealthAnalysisService;
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public Result<AiHealthAnalysisVO> analyzeCurrentPatient(Authentication authentication) {
        Long userId = getCurrentUserId(authentication);
        return Result.success(aiHealthAnalysisService.analyzeCurrentPatient(userId));
    }

    @GetMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public Result<AiHealthAnalysisVO> analyzePatient(@PathVariable Long patientId) {
        return Result.success(aiHealthAnalysisService.analyzePatientForClinician(patientId));
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long)) {
            throw new RuntimeException("当前登录状态无效，请重新登录");
        }
        return (Long) authentication.getPrincipal();
    }
}
