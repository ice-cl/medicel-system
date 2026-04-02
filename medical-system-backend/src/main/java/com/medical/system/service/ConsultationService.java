package com.medical.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.system.entity.Consultation;

public interface ConsultationService extends IService<Consultation> {
    Page<Consultation> pageList(Integer pageNum, Integer pageSize,
                                 Long patientId, Long doctorId, Integer status);
    void createConsultation(Consultation consultation);
    void endConsultation(Long id);
}
