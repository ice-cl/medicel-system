package com.medical.system.vo;

import lombok.Data;

@Data
public class AiHealthAnalysisVO {
    private Long patientId;
    private String patientName;
    private String audience;
    private Integer recordCount;
    private String latestRecordTime;
    private String analysis;
    private String disclaimer;
}
