package com.medical.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.system.entity.MedicalRecord;

public interface MedicalRecordService extends IService<MedicalRecord> {
    Page<MedicalRecord> pageList(Integer pageNum, Integer pageSize,
                                  Long patientId, Long doctorId);
    void createRecord(MedicalRecord record);
    void updateRecord(MedicalRecord record);
}
