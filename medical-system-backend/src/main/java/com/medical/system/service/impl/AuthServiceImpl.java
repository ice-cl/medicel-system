package com.medical.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medical.system.dto.LoginDTO;
import com.medical.system.dto.RegisterDTO;
import com.medical.system.entity.Doctor;
import com.medical.system.entity.Patient;
import com.medical.system.entity.SysRole;
import com.medical.system.entity.SysUser;
import com.medical.system.entity.SysUserRole;
import com.medical.system.mapper.DoctorMapper;
import com.medical.system.mapper.PatientMapper;
import com.medical.system.mapper.SysRoleMapper;
import com.medical.system.mapper.SysUserMapper;
import com.medical.system.mapper.SysUserRoleMapper;
import com.medical.system.service.AuthService;
import com.medical.system.utils.JwtUtils;
import com.medical.system.vo.LoginVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class AuthServiceImpl implements AuthService {

    private final SysUserMapper userMapper;
    private final SysUserRoleMapper userRoleMapper;
    private final SysRoleMapper roleMapper;
    private final DoctorMapper doctorMapper;
    private final PatientMapper patientMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthServiceImpl(SysUserMapper userMapper,
                          SysUserRoleMapper userRoleMapper,
                          SysRoleMapper roleMapper,
                          DoctorMapper doctorMapper,
                          PatientMapper patientMapper,
                          PasswordEncoder passwordEncoder,
                          JwtUtils jwtUtils) {
        this.userMapper = userMapper;
        this.userRoleMapper = userRoleMapper;
        this.roleMapper = roleMapper;
        this.doctorMapper = doctorMapper;
        this.patientMapper = patientMapper;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    @Override
    public LoginVO login(LoginDTO loginDTO) {
        SysUser user = userMapper.selectOne(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, loginDTO.getUsername())
                .eq(SysUser::getDeleted, 0)
        );

        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new RuntimeException("密码错误");
        }

        if (user.getStatus() != 1) {
            throw new RuntimeException("账号已被禁用");
        }

        if (user.getDeleted() != null && user.getDeleted() == 1) {
            throw new RuntimeException("账号已删除");
        }

        String roleCode = userRoleMapper.getRoleCodeByUserId(user.getId());
        if (!StringUtils.hasText(roleCode)) {
            throw new RuntimeException("用户角色不存在");
        }

        if ("DOCTOR".equals(roleCode)) {
            Doctor doctor = doctorMapper.selectOne(
                    new LambdaQueryWrapper<Doctor>()
                            .eq(Doctor::getUserId, user.getId())
                            .eq(Doctor::getDeleted, 0)
            );
            if (doctor == null) {
                throw new RuntimeException("医生资料不存在");
            }
            if (doctor.getStatus() == null || doctor.getStatus() != 1) {
                throw new RuntimeException("医生账号待审核或已禁用");
            }
        }

        String token = jwtUtils.generateToken(user.getId(), user.getUsername(), roleCode);

        LoginVO loginVO = new LoginVO();
        loginVO.setToken(token);
        loginVO.setUserId(user.getId());
        loginVO.setUsername(user.getUsername());
        loginVO.setRealName(user.getRealName());
        loginVO.setRole(roleCode);

        return loginVO;
    }

    @Override
    @Transactional
    public void register(RegisterDTO registerDTO) {
        // 检查用户名是否已存在
        Long count = userMapper.selectCount(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, registerDTO.getUsername())
        );
        if (count > 0) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查手机号是否已存在
        count = userMapper.selectCount(
            new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getPhone, registerDTO.getPhone())
        );
        if (count > 0) {
            throw new RuntimeException("手机号已被注册");
        }

        // 创建用户
        SysUser user = new SysUser();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setEmail(registerDTO.getEmail());
        user.setGender(registerDTO.getGender());
        user.setStatus(1);
        user.setDeleted(0);
        userMapper.insert(user);

        // 根据注册类型分配角色
        String roleCode = "PATIENT".equals(registerDTO.getRegisterType()) ? "PATIENT" : "DOCTOR";
        SysRole role = roleMapper.selectOne(
            new LambdaQueryWrapper<SysRole>()
                .eq(SysRole::getRoleCode, roleCode)
        );

        if (role != null) {
            SysUserRole userRole = new SysUserRole();
            userRole.setUserId(user.getId());
            userRole.setRoleId(role.getId());
            userRole.setCreateTime(LocalDateTime.now());
            userRoleMapper.insert(userRole);
        }

        // 根据注册类型创建对应的扩展信息
        if ("PATIENT".equals(registerDTO.getRegisterType())) {
            Patient patient = new Patient();
            patient.setUserId(user.getId());
            patient.setPatientCode(generatePatientCode());
            patient.setName(registerDTO.getRealName());
            patient.setPhone(registerDTO.getPhone());
            patient.setGender(registerDTO.getGender());
            patient.setDeleted(0);
            patientMapper.insert(patient);
        } else if ("DOCTOR".equals(registerDTO.getRegisterType())) {
            Doctor doctor = new Doctor();
            doctor.setUserId(user.getId());
            doctor.setDeptId(registerDTO.getDeptId());
            doctor.setDoctorCode(generateDoctorCode());
            doctor.setTitle(registerDTO.getTitle());
            doctor.setSpecialty(registerDTO.getSpecialty());
            doctor.setIntroduction(registerDTO.getIntroduction());
            doctor.setConsultationFee(BigDecimal.ZERO);
            doctor.setIsExpert(0);
            doctor.setStatus(0); // 待审核
            doctor.setDeleted(0);
            doctorMapper.insert(doctor);
        }
    }

    private String generatePatientCode() {
        return "P" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + (System.currentTimeMillis() % 1000);
    }

    private String generateDoctorCode() {
        return "D" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + (System.currentTimeMillis() % 1000);
    }
}
