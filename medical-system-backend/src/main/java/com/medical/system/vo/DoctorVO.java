package com.medical.system.vo;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class DoctorVO {
    private Long id;
    private Long userId;
    private Long deptId;
    private String doctorCode;
    private String realName;
    private String title;
    private String specialty;
    private BigDecimal consultationFee;
    private Integer isExpert;
    private Integer status;
}
