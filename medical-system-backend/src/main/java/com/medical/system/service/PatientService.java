package com.medical.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.system.entity.Patient;

public interface PatientService extends IService<Patient> {
    Page<Patient> pageList(Integer pageNum, Integer pageSize, String name);
    Patient getByUserId(Long userId);
    void createPatient(Patient patient);
    void updatePatient(Patient patient);
}
