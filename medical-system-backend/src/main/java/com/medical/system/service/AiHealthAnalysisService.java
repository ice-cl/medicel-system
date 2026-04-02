package com.medical.system.service;

import com.medical.system.vo.AiHealthAnalysisVO;

public interface AiHealthAnalysisService {
    AiHealthAnalysisVO analyzeCurrentPatient(Long userId);

    AiHealthAnalysisVO analyzePatientForClinician(Long patientId);
}
