package com.medical.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.system.entity.Consultation;
import com.medical.system.mapper.ConsultationMapper;
import com.medical.system.service.ConsultationService;
import org.springframework.stereotype.Service;

@Service
public class ConsultationServiceImpl extends ServiceImpl<ConsultationMapper, Consultation>
        implements ConsultationService {

    @Override
    public Page<Consultation> pageList(Integer pageNum, Integer pageSize,
                                        Long patientId, Long doctorId, Integer status) {
        LambdaQueryWrapper<Consultation> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, Consultation::getPatientId, patientId)
               .eq(doctorId != null, Consultation::getDoctorId, doctorId)
               .eq(status != null, Consultation::getStatus, status)
               .orderByDesc(Consultation::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public void createConsultation(Consultation consultation) {
        consultation.setStatus(0);
        save(consultation);
    }

    @Override
    public void endConsultation(Long id) {
        Consultation consultation = getById(id);
        if (consultation != null) {
            consultation.setStatus(1);
            updateById(consultation);
        }
    }
}
