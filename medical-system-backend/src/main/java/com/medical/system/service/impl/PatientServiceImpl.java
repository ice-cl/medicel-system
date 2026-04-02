package com.medical.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.system.entity.Patient;
import com.medical.system.entity.SysUser;
import com.medical.system.entity.SysUserRole;
import com.medical.system.entity.SysRole;
import com.medical.system.mapper.PatientMapper;
import com.medical.system.mapper.SysUserMapper;
import com.medical.system.mapper.SysUserRoleMapper;
import com.medical.system.mapper.SysRoleMapper;
import com.medical.system.service.PatientService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class PatientServiceImpl extends ServiceImpl<PatientMapper, Patient> implements PatientService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public PatientServiceImpl(SysUserMapper sysUserMapper, SysUserRoleMapper sysUserRoleMapper,
                             SysRoleMapper sysRoleMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<Patient> pageList(Integer pageNum, Integer pageSize, String name) {
        LambdaQueryWrapper<Patient> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Patient::getDeleted, 0);
        if (StringUtils.hasText(name)) {
            wrapper.like(Patient::getName, name);
        }
        wrapper.orderByDesc(Patient::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public Patient getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getUserId, userId)
                .eq(Patient::getDeleted, 0));
    }

    @Override
    @Transactional
    public void createPatient(Patient patient) {
        // 生成患者编号
        String patientCode = generatePatientCode();
        patient.setPatientCode(patientCode);

        // 创建用户账号（使用患者姓名作为用户名）
        SysUser user = new SysUser();
        user.setUsername(patient.getName()); // 使用姓名作为用户名
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRealName(patient.getName());
        user.setPhone(patient.getPhone());
        user.setStatus(1);
        user.setDeleted(0);
        sysUserMapper.insert(user);

        // 分配患者角色
        LambdaQueryWrapper<SysRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(SysRole::getRoleCode, "PATIENT");
        Long roleId = sysRoleMapper.selectOne(roleWrapper).getId();
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(roleId);
        sysUserRoleMapper.insert(userRole);

        // 创建患者记录
        patient.setUserId(user.getId());
        patient.setDeleted(0);
        save(patient);
    }

    @Override
    public void updatePatient(Patient patient) {
        updateById(patient);
    }

    private String generatePatientCode() {
        String prefix = "P" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        return prefix + System.currentTimeMillis() % 10000;
    }
}
