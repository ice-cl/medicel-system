package com.medical.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.system.entity.Prescription;
import com.medical.system.mapper.PrescriptionMapper;
import com.medical.system.service.PrescriptionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import java.util.List;

@Service
public class PrescriptionServiceImpl extends ServiceImpl<PrescriptionMapper, Prescription>
        implements PrescriptionService {

    @Override
    public Page<Prescription> pageList(Integer pageNum, Integer pageSize, String drugName) {
        LambdaQueryWrapper<Prescription> wrapper = new LambdaQueryWrapper<>();
        wrapper.like(StringUtils.hasText(drugName), Prescription::getDrugName, drugName)
               .orderByDesc(Prescription::getCreateTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<Prescription> listByRecordId(Long recordId) {
        return list(new LambdaQueryWrapper<Prescription>()
                .eq(Prescription::getRecordId, recordId)
                .orderByAsc(Prescription::getId));
    }

    @Override
    public void createPrescription(Prescription prescription) {
        save(prescription);
    }

    @Override
    public void updatePrescription(Prescription prescription) {
        updateById(prescription);
    }

    @Override
    public void deletePrescription(Long id) {
        removeById(id);
    }

    @Override
    @Transactional
    public void batchCreate(List<Prescription> prescriptions) {
        saveBatch(prescriptions);
    }
}
