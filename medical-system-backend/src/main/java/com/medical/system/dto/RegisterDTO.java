package com.medical.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterDTO {
    @NotBlank(message = "用户名不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;

    @NotBlank(message = "真实姓名不能为空")
    private String realName;

    @NotBlank(message = "手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String phone;

    private String email;
    private Integer gender;

    // 注册类型: PATIENT-患者, DOCTOR-医生
    @NotBlank(message = "注册类型不能为空")
    private String registerType;

    // 医生注册时需要的额外信息
    private Long deptId;
    private String title;
    private String specialty;
    private String introduction;
}
