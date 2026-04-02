package com.medical.system.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.system.entity.DoctorSchedule;
import java.time.LocalDate;
import java.util.List;

public interface DoctorScheduleService extends IService<DoctorSchedule> {
    Page<DoctorSchedule> pageList(Integer pageNum, Integer pageSize,
                                   Long doctorId, Long deptId, LocalDate date);
    List<DoctorSchedule> getAvailableSchedules(Long deptId, LocalDate date);
    void createSchedule(DoctorSchedule schedule);
    void batchCreateSchedule(List<DoctorSchedule> schedules);
    void updateSchedule(DoctorSchedule schedule);
    void deleteSchedule(Long id);
}
