package com.medical.system.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.medical.system.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("prescription")
public class Prescription extends BaseEntity {
    private Long recordId;
    private String drugName;
    private String dosage;
    private String frequency;
    private Integer days;
    @TableField(value = "`usage`")
    private String usage;
    private String remark;
}
