package com.medical.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.medical.system.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("doctor_schedule")
public class DoctorSchedule extends BaseEntity {
    private Long doctorId;
    private Long deptId;
    private LocalDate scheduleDate;
    private Integer timePeriod;
    private Integer maxPatients;
    private Integer currentPatients;
    private Integer status;
}
