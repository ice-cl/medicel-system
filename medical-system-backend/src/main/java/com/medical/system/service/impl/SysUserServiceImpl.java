package com.medical.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.system.entity.Doctor;
import com.medical.system.entity.Patient;
import com.medical.system.entity.SysUser;
import com.medical.system.entity.SysUserRole;
import com.medical.system.mapper.DoctorMapper;
import com.medical.system.mapper.PatientMapper;
import com.medical.system.mapper.SysUserMapper;
import com.medical.system.mapper.SysUserRoleMapper;
import com.medical.system.service.SysUserService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    private final SysUserRoleMapper userRoleMapper;
    private final DoctorMapper doctorMapper;
    private final PatientMapper patientMapper;
    private final PasswordEncoder passwordEncoder;

    public SysUserServiceImpl(SysUserRoleMapper userRoleMapper,
                              DoctorMapper doctorMapper,
                              PatientMapper patientMapper,
                              PasswordEncoder passwordEncoder) {
        this.userRoleMapper = userRoleMapper;
        this.doctorMapper = doctorMapper;
        this.patientMapper = patientMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<SysUser> pageList(Integer pageNum, Integer pageSize, String username) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SysUser::getDeleted, 0);
        if (StringUtils.hasText(username)) {
            wrapper.like(SysUser::getUsername, username);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);
        Page<SysUser> page = page(new Page<>(pageNum, pageSize), wrapper);
        page.getRecords().forEach(user -> {
            Long roleId = userRoleMapper.getRoleIdByUserId(user.getId());
            String roleCode = userRoleMapper.getRoleCodeByUserId(user.getId());
            user.setRoleId(roleId);
            user.setRoleCode(roleCode);
        });
        return page;
    }

    @Override
    @Transactional
    public void createUser(SysUser user, Long roleId) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setDeleted(0);
        user.setStatus(1);
        save(user);

        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(roleId);
        userRoleMapper.insert(userRole);
    }

    @Override
    @Transactional
    public void updateUser(SysUser user, Long roleId) {
        if (StringUtils.hasText(user.getPassword())) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        } else {
            user.setPassword(null);
        }
        updateById(user);

        if (roleId != null) {
            userRoleMapper.deleteByUserId(user.getId());
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(roleId);
            userRoleMapper.insert(userRole);
        }
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setDeleted(1);
        updateById(user);
        userRoleMapper.deleteByUserId(id);

        Doctor doctor = doctorMapper.selectOne(
                new LambdaQueryWrapper<Doctor>()
                        .eq(Doctor::getUserId, id)
                        .eq(Doctor::getDeleted, 0)
        );
        if (doctor != null) {
            doctor.setDeleted(1);
            doctorMapper.updateById(doctor);
        }

        Patient patient = patientMapper.selectOne(
                new LambdaQueryWrapper<Patient>()
                        .eq(Patient::getUserId, id)
                        .eq(Patient::getDeleted, 0)
        );
        if (patient != null) {
            patient.setDeleted(1);
            patientMapper.updateById(patient);
        }
    }
}
