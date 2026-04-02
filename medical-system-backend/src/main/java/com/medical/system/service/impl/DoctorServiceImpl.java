package com.medical.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.system.entity.Doctor;
import com.medical.system.entity.SysUser;
import com.medical.system.entity.SysUserRole;
import com.medical.system.entity.SysRole;
import com.medical.system.mapper.DoctorMapper;
import com.medical.system.mapper.SysUserMapper;
import com.medical.system.mapper.SysUserRoleMapper;
import com.medical.system.mapper.SysRoleMapper;
import com.medical.system.service.DoctorService;
import com.medical.system.vo.DoctorVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    private final SysUserMapper sysUserMapper;
    private final SysUserRoleMapper sysUserRoleMapper;
    private final SysRoleMapper sysRoleMapper;
    private final PasswordEncoder passwordEncoder;

    public DoctorServiceImpl(SysUserMapper sysUserMapper, SysUserRoleMapper sysUserRoleMapper,
                            SysRoleMapper sysRoleMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.sysUserRoleMapper = sysUserRoleMapper;
        this.sysRoleMapper = sysRoleMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Page<Doctor> pageList(Integer pageNum, Integer pageSize, Long deptId) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getDeleted, 0);
        if (deptId != null) {
            wrapper.eq(Doctor::getDeptId, deptId);
        }
        wrapper.orderByDesc(Doctor::getCreateTime);
        Page<Doctor> page = page(new Page<>(pageNum, pageSize), wrapper);

        // 填充医生姓名
        List<Long> userIds = page.getRecords().stream().map(Doctor::getUserId).collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
            Map<Long, String> userNameMap = users.stream()
                    .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName));
            page.getRecords().forEach(d -> d.setRealName(userNameMap.get(d.getUserId())));
        }
        return page;
    }

    @Override
    public List<Doctor> listByDept(Long deptId) {
        return list(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getDeleted, 0)
                .eq(Doctor::getStatus, 1)
                .eq(deptId != null, Doctor::getDeptId, deptId));
    }

    @Override
    @Transactional
    public void createDoctor(Doctor doctor) {
        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(doctor.getDoctorCode());
        user.setPassword(passwordEncoder.encode("123456"));
        user.setRealName(doctor.getRealName() != null ? doctor.getRealName() : "医生" + doctor.getDoctorCode());
        user.setStatus(1);
        user.setDeleted(0);
        sysUserMapper.insert(user);

        // 分配医生角色
        LambdaQueryWrapper<SysRole> roleWrapper = new LambdaQueryWrapper<>();
        roleWrapper.eq(SysRole::getRoleCode, "DOCTOR");
        Long roleId = sysRoleMapper.selectOne(roleWrapper).getId();
        SysUserRole userRole = new SysUserRole();
        userRole.setUserId(user.getId());
        userRole.setRoleId(roleId);
        sysUserRoleMapper.insert(userRole);

        // 创建医生记录
        doctor.setUserId(user.getId());
        doctor.setDeleted(0);
        doctor.setStatus(1);
        save(doctor);
    }

    @Override
    public void updateDoctor(Doctor doctor) {
        updateById(doctor);
    }

    @Override
    public void deleteDoctor(Long id) {
        Doctor doctor = new Doctor();
        doctor.setId(id);
        doctor.setDeleted(1);
        updateById(doctor);
    }

    @Override
    public Page<Doctor> pagePendingList(Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getDeleted, 0)
               .eq(Doctor::getStatus, 0)
               .orderByDesc(Doctor::getCreateTime);
        Page<Doctor> page = page(new Page<>(pageNum, pageSize), wrapper);

        // 填充医生姓名
        List<Long> userIds = page.getRecords().stream().map(Doctor::getUserId).collect(Collectors.toList());
        if (!userIds.isEmpty()) {
            List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
            Map<Long, String> userNameMap = users.stream()
                    .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName));
            page.getRecords().forEach(d -> d.setRealName(userNameMap.get(d.getUserId())));
        }
        return page;
    }

    @Override
    public void verifyDoctor(Long id, Integer status) {
        Doctor doctor = getById(id);
        if (doctor != null) {
            doctor.setStatus(status);
            updateById(doctor);
        }
    }

    @Override
    public Doctor getByUserId(Long userId) {
        return getOne(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getUserId, userId)
                .eq(Doctor::getDeleted, 0));
    }

    @Override
    public List<DoctorVO> listDoctorVO(Long deptId) {
        List<Doctor> doctors = list(new LambdaQueryWrapper<Doctor>()
                .eq(Doctor::getDeleted, 0)
                .eq(Doctor::getStatus, 1)
                .eq(deptId != null, Doctor::getDeptId, deptId));
        if (doctors.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> userIds = doctors.stream().map(Doctor::getUserId).collect(Collectors.toList());
        List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
        Map<Long, String> userNameMap = users.stream()
                .collect(Collectors.toMap(SysUser::getId, SysUser::getRealName));

        return doctors.stream().map(d -> {
            DoctorVO vo = new DoctorVO();
            vo.setId(d.getId());
            vo.setUserId(d.getUserId());
            vo.setDeptId(d.getDeptId());
            vo.setDoctorCode(d.getDoctorCode());
            vo.setRealName(userNameMap.get(d.getUserId()));
            vo.setTitle(d.getTitle());
            vo.setSpecialty(d.getSpecialty());
            vo.setConsultationFee(d.getConsultationFee());
            vo.setIsExpert(d.getIsExpert());
            vo.setStatus(d.getStatus());
            return vo;
        }).collect(Collectors.toList());
    }
}
