package com.medical.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.system.entity.HealthData;
import com.medical.system.mapper.HealthDataMapper;
import com.medical.system.service.HealthDataService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class HealthDataServiceImpl extends ServiceImpl<HealthDataMapper, HealthData>
        implements HealthDataService {

    @Override
    public Page<HealthData> pageList(Integer pageNum, Integer pageSize,
                                      Long patientId, String type) {
        LambdaQueryWrapper<HealthData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(patientId != null, HealthData::getPatientId, patientId)
               .eq(type != null, HealthData::getType, type)
               .orderByDesc(HealthData::getRecordTime);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<HealthData> listByPatientAndType(Long patientId, String type,
                                                  LocalDateTime startTime,
                                                  LocalDateTime endTime) {
        LambdaQueryWrapper<HealthData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HealthData::getPatientId, patientId)
               .eq(type != null, HealthData::getType, type)
               .ge(startTime != null, HealthData::getRecordTime, startTime)
               .le(endTime != null, HealthData::getRecordTime, endTime)
               .orderByAsc(HealthData::getRecordTime);
        return list(wrapper);
    }

    @Override
    public void createHealthData(HealthData healthData) {
        save(healthData);
    }

    @Override
    public void deleteHealthData(Long id) {
        removeById(id);
    }
}
