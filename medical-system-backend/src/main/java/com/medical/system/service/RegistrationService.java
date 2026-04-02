package com.medical.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.system.entity.Registration;
import java.time.LocalDate;

public interface RegistrationService extends IService<Registration> {
    Page<Registration> pageList(Integer pageNum, Integer pageSize,
                                 Long patientId, Long doctorId, Integer status);
    void createRegistration(Registration registration);
    void cancelRegistration(Long id);
    void updateStatus(Long id, Integer status, Long doctorId);
}
