package com.medical.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.medical.system.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consultation")
public class Consultation extends BaseEntity {
    private Long patientId;
    private Long doctorId;
    private Integer status;
}
