package com.medical.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.system.entity.MedicalRecord;
import com.medical.system.mapper.MedicalRecordMapper;
import com.medical.system.service.MedicalRecordService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class MedicalRecordServiceImpl extends ServiceImpl<MedicalRecordMapper, MedicalRecord>
        implements MedicalRecordService {

    @Override
    public Page<MedicalRecord> pageList(Integer pageNum, Integer pageSize,
                                         Long patientId, Long doctorId) {
        LambdaQueryWrapper<MedicalRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(MedicalRecord::getDeleted, 0);
        if (patientId != null) {
            wrapper.eq(MedicalRecord::getPatientId, patientId);
        }
        if (doctorId != null) {
            wrapper.eq(MedicalRecord::getDoctorId, doctorId);
        }
        wrapper.orderByDesc(MedicalRecord::getVisitDate);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void createRecord(MedicalRecord record) {
        if (record.getRegistrationId() != null) {
            Long exists = count(new LambdaQueryWrapper<MedicalRecord>()
                    .eq(MedicalRecord::getRegistrationId, record.getRegistrationId())
                    .eq(MedicalRecord::getDeleted, 0));
            if (exists != null && exists > 0) {
                throw new RuntimeException("该挂号已创建病历");
            }
        }
        record.setRecordNo(generateRecordNo());
        record.setVisitDate(LocalDateTime.now());
        record.setStatus(1);
        record.setDeleted(0);
        save(record);
    }

    @Override
    public void updateRecord(MedicalRecord record) {
        updateById(record);
    }

    private String generateRecordNo() {
        String prefix = "MR" + LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        return prefix + (System.currentTimeMillis() % 1000);
    }
}
