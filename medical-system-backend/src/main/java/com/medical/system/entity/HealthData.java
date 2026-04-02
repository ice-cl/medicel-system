package com.medical.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.medical.system.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("health_data")
public class HealthData extends BaseEntity {
    private Long patientId;
    private String type;
    private BigDecimal value;
    private String unit;
    private LocalDateTime recordTime;
}
