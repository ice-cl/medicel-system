package com.medical.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.medical.system.entity.Consultation;
import com.medical.system.entity.Department;
import com.medical.system.entity.Doctor;
import com.medical.system.entity.HealthData;
import com.medical.system.entity.MedicalRecord;
import com.medical.system.entity.Patient;
import com.medical.system.entity.Registration;
import com.medical.system.mapper.ConsultationMapper;
import com.medical.system.mapper.DepartmentMapper;
import com.medical.system.mapper.DoctorMapper;
import com.medical.system.mapper.HealthDataMapper;
import com.medical.system.mapper.MedicalRecordMapper;
import com.medical.system.mapper.PatientMapper;
import com.medical.system.mapper.RegistrationMapper;
import com.medical.system.service.StatisticsService;
import com.medical.system.vo.StatisticsVO;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
public class StatisticsServiceImpl implements StatisticsService {

    private final PatientMapper patientMapper;
    private final DoctorMapper doctorMapper;
    private final RegistrationMapper registrationMapper;
    private final DepartmentMapper departmentMapper;
    private final MedicalRecordMapper medicalRecordMapper;
    private final ConsultationMapper consultationMapper;
    private final HealthDataMapper healthDataMapper;

    public StatisticsServiceImpl(PatientMapper patientMapper,
                                 DoctorMapper doctorMapper,
                                 RegistrationMapper registrationMapper,
                                 DepartmentMapper departmentMapper,
                                 MedicalRecordMapper medicalRecordMapper,
                                 ConsultationMapper consultationMapper,
                                 HealthDataMapper healthDataMapper) {
        this.patientMapper = patientMapper;
        this.doctorMapper = doctorMapper;
        this.registrationMapper = registrationMapper;
        this.departmentMapper = departmentMapper;
        this.medicalRecordMapper = medicalRecordMapper;
        this.consultationMapper = consultationMapper;
        this.healthDataMapper = healthDataMapper;
    }

    @Override
    public StatisticsVO getStatistics(Long userId, String role) {
        StatisticsVO vo = new StatisticsVO();
        vo.setRole(role);

        if ("PATIENT".equals(role)) {
            vo.setShowManagementMetrics(false);
            fillPatientStatistics(vo, userId);
            return vo;
        }

        vo.setShowManagementMetrics(true);
        fillManagementStatistics(vo);
        return vo;
    }

    private void fillManagementStatistics(StatisticsVO vo) {
        LocalDate today = LocalDate.now();

        vo.setTotalPatients(patientMapper.selectCount(
                new LambdaQueryWrapper<Patient>()
                        .eq(Patient::getDeleted, 0)));

        vo.setTotalDoctors(doctorMapper.selectCount(
                new LambdaQueryWrapper<Doctor>()
                        .eq(Doctor::getDeleted, 0)
                        .eq(Doctor::getStatus, 1)));

        vo.setPendingDoctors(doctorMapper.selectCount(
                new LambdaQueryWrapper<Doctor>()
                        .eq(Doctor::getDeleted, 0)
                        .eq(Doctor::getStatus, 0)));

        vo.setTotalRegistrations(registrationMapper.selectCount(
                new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getDeleted, 0)));

        vo.setTodayRegistrations(registrationMapper.selectCount(
                new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getDeleted, 0)
                        .eq(Registration::getRegDate, today)));

        vo.setTotalDepartments(departmentMapper.selectCount(
                new LambdaQueryWrapper<Department>()
                        .eq(Department::getDeleted, 0)));
    }

    private void fillPatientStatistics(StatisticsVO vo, Long userId) {
        Patient patient = patientMapper.selectOne(new LambdaQueryWrapper<Patient>()
                .eq(Patient::getUserId, userId)
                .eq(Patient::getDeleted, 0));
        if (patient == null) {
            vo.setMyRegistrations(0L);
            vo.setPendingRegistrations(0L);
            vo.setCompletedRegistrations(0L);
            vo.setMyMedicalRecords(0L);
            vo.setActiveConsultations(0L);
            vo.setHealthDataCount(0L);
            return;
        }

        Long patientId = patient.getId();
        vo.setMyRegistrations(registrationMapper.selectCount(
                new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getPatientId, patientId)
                        .eq(Registration::getDeleted, 0)));

        vo.setPendingRegistrations(registrationMapper.selectCount(
                new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getPatientId, patientId)
                        .eq(Registration::getDeleted, 0)
                        .eq(Registration::getStatus, 0)));

        vo.setCompletedRegistrations(registrationMapper.selectCount(
                new LambdaQueryWrapper<Registration>()
                        .eq(Registration::getPatientId, patientId)
                        .eq(Registration::getDeleted, 0)
                        .eq(Registration::getStatus, 2)));

        vo.setMyMedicalRecords(medicalRecordMapper.selectCount(
                new LambdaQueryWrapper<MedicalRecord>()
                        .eq(MedicalRecord::getPatientId, patientId)
                        .eq(MedicalRecord::getDeleted, 0)));

        vo.setActiveConsultations(consultationMapper.selectCount(
                new LambdaQueryWrapper<Consultation>()
                        .eq(Consultation::getPatientId, patientId)
                        .eq(Consultation::getDeleted, 0)
                        .eq(Consultation::getStatus, 0)));

        vo.setHealthDataCount(healthDataMapper.selectCount(
                new LambdaQueryWrapper<HealthData>()
                        .eq(HealthData::getPatientId, patientId)
                        .eq(HealthData::getDeleted, 0)));
    }
}
