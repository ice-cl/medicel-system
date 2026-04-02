package com.medical.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.system.entity.Prescription;
import java.util.List;

public interface PrescriptionService extends IService<Prescription> {
    Page<Prescription> pageList(Integer pageNum, Integer pageSize, String drugName);
    List<Prescription> listByRecordId(Long recordId);
    void createPrescription(Prescription prescription);
    void updatePrescription(Prescription prescription);
    void deletePrescription(Long id);
    void batchCreate(List<Prescription> prescriptions);
}
