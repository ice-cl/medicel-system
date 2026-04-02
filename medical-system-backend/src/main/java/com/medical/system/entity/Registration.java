package com.medical.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.medical.system.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("registration")
public class Registration extends BaseEntity {
    private String regNo;
    private Long patientId;
    private Long doctorId;
    private Long deptId;
    private Long scheduleId;
    private LocalDate regDate;
    private Integer timePeriod;
    private Integer queueNo;
    private BigDecimal regFee;
    private Integer status;
}
