package com.medical.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.medical.system.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("department")
public class Department extends BaseEntity {
    private String deptCode;
    private String deptName;
    private Long parentId;
    private String description;
    private String location;
    private String phone;
    private Integer sortOrder;
    private Integer status;
}
