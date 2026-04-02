package com.medical.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.medical.system.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("medical_record")
public class MedicalRecord extends BaseEntity {
    private String recordNo;
    private Long patientId;
    private Long doctorId;
    private Long registrationId;
    private LocalDateTime visitDate;
    private String chiefComplaint;
    private String presentIllness;
    private String pastHistory;
    private String physicalExam;
    private String diagnosis;
    private String treatmentPlan;
    private String prescription;
    private String advice;
    private Integer status;
}
