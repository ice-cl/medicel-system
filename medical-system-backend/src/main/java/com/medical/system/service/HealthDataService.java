package com.medical.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.system.entity.HealthData;
import java.time.LocalDateTime;
import java.util.List;

public interface HealthDataService extends IService<HealthData> {
    Page<HealthData> pageList(Integer pageNum, Integer pageSize,
                               Long patientId, String type);
    List<HealthData> listByPatientAndType(Long patientId, String type,
                                           LocalDateTime startTime,
                                           LocalDateTime endTime);
    void createHealthData(HealthData healthData);
    void deleteHealthData(Long id);
}
