package com.medical.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.system.entity.Doctor;
import com.medical.system.entity.DoctorSchedule;
import com.medical.system.entity.Registration;
import com.medical.system.entity.MedicalRecord;
import com.medical.system.mapper.RegistrationMapper;
import com.medical.system.mapper.DoctorMapper;
import com.medical.system.mapper.MedicalRecordMapper;
import com.medical.system.service.DoctorScheduleService;
import com.medical.system.service.DoctorService;
import com.medical.system.service.RegistrationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class RegistrationServiceImpl extends ServiceImpl<RegistrationMapper, Registration>
        implements RegistrationService {

    private final DoctorScheduleService scheduleService;
    private final DoctorService doctorService;
    private final DoctorMapper doctorMapper;
    private final MedicalRecordMapper medicalRecordMapper;

    public RegistrationServiceImpl(DoctorScheduleService scheduleService,
                                   DoctorService doctorService,
                                   DoctorMapper doctorMapper,
                                   MedicalRecordMapper medicalRecordMapper) {
        this.scheduleService = scheduleService;
        this.doctorService = doctorService;
        this.doctorMapper = doctorMapper;
        this.medicalRecordMapper = medicalRecordMapper;
    }

    @Override
    public Page<Registration> pageList(Integer pageNum, Integer pageSize,
                                        Long patientId, Long doctorId, Integer status) {
        LambdaQueryWrapper<Registration> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Registration::getDeleted, 0);
        if (patientId != null) {
            wrapper.eq(Registration::getPatientId, patientId);
        }
        if (doctorId != null) {
            wrapper.eq(Registration::getDoctorId, doctorId);
        }
        if (status != null) {
            wrapper.eq(Registration::getStatus, status);
        }
        wrapper.orderByDesc(Registration::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    @Transactional
    public void createRegistration(Registration registration) {
        // 检查排班是否可用
        DoctorSchedule schedule = scheduleService.getById(registration.getScheduleId());
        if (schedule == null || schedule.getStatus() != 1) {
            throw new RuntimeException("该排班不可用");
        }
        if (schedule.getCurrentPatients() >= schedule.getMaxPatients()) {
            throw new RuntimeException("该时段已满");
        }

        // 获取医生信息
        Doctor doctor = doctorService.getById(registration.getDoctorId());
        if (doctor == null || doctor.getDeleted() != null && doctor.getDeleted() == 1) {
            throw new RuntimeException("医生不存在");
        }
        if (!doctor.getId().equals(schedule.getDoctorId())) {
            throw new RuntimeException("挂号医生与排班不匹配");
        }

        // 生成挂号单号
        registration.setRegNo(generateRegNo());
        registration.setDeptId(schedule.getDeptId());
        registration.setRegDate(schedule.getScheduleDate());
        registration.setTimePeriod(schedule.getTimePeriod());
        registration.setQueueNo(schedule.getCurrentPatients() + 1);
        registration.setRegFee(doctor.getConsultationFee());
        registration.setStatus(0);
        registration.setDeleted(0);
        save(registration);

        // 更新排班已挂号人数
        schedule.setCurrentPatients(schedule.getCurrentPatients() + 1);
        scheduleService.updateById(schedule);
    }

    @Override
    @Transactional
    public void cancelRegistration(Long id) {
        Registration reg = getById(id);
        if (reg == null || reg.getStatus() != 0) {
            throw new RuntimeException("无法取消该挂号");
        }

        reg.setStatus(3);
        updateById(reg);

        // 更新排班人数
        DoctorSchedule schedule = scheduleService.getById(reg.getScheduleId());
        if (schedule != null) {
            schedule.setCurrentPatients(Math.max(0, schedule.getCurrentPatients() - 1));
            scheduleService.updateById(schedule);
        }
    }

    @Override
    @Transactional
    public void updateStatus(Long id, Integer status, Long userId) {
        Registration origin = getById(id);
        if (origin == null || origin.getDeleted() != null && origin.getDeleted() == 1) {
            throw new RuntimeException("挂号记录不存在");
        }

        if (status == null || (status != 1 && status != 2 && status != 3)) {
            throw new RuntimeException("状态参数不合法");
        }

        if (userId != null) {
            Doctor doctor = doctorMapper.selectOne(new LambdaQueryWrapper<Doctor>()
                    .eq(Doctor::getUserId, userId)
                    .eq(Doctor::getDeleted, 0));
            if (doctor == null || !doctor.getId().equals(origin.getDoctorId())) {
                throw new RuntimeException("仅接诊医生可更新该挂号状态");
            }
        }

        if (status == 2 && origin.getStatus() != null && origin.getStatus() != 1) {
            throw new RuntimeException("仅就诊中状态可置为已完成");
        }

        if (status == 1 && origin.getStatus() != null && origin.getStatus() != 0) {
            throw new RuntimeException("仅待就诊状态可置为就诊中");
        }

        if (status == 2) {
            Long recordCount = medicalRecordMapper.selectCount(new LambdaQueryWrapper<MedicalRecord>()
                    .eq(MedicalRecord::getRegistrationId, id)
                    .eq(MedicalRecord::getDeleted, 0));
            if (recordCount == null || recordCount == 0) {
                throw new RuntimeException("请先创建病历后再完成就诊");
            }
        }

        Registration reg = new Registration();
        reg.setId(id);
        reg.setStatus(status);
        updateById(reg);
    }

    private String generateRegNo() {
        String prefix = "REG" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return prefix + (System.currentTimeMillis() % 1000);
    }
}
