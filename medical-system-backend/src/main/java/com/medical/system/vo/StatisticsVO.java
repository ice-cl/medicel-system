package com.medical.system.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StatisticsVO {
    private String role;
    private Boolean showManagementMetrics;

    private Long totalPatients;
    private Long totalDoctors;
    private Long totalRegistrations;
    private Long todayRegistrations;
    private Long pendingDoctors;
    private Long totalDepartments;

    private Long myRegistrations;
    private Long pendingRegistrations;
    private Long completedRegistrations;
    private Long myMedicalRecords;
    private Long activeConsultations;
    private Long healthDataCount;
}
