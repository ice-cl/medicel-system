package com.medical.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.system.entity.DoctorSchedule;
import com.medical.system.mapper.DoctorScheduleMapper;
import com.medical.system.service.DoctorScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
public class DoctorScheduleServiceImpl extends ServiceImpl<DoctorScheduleMapper, DoctorSchedule>
        implements DoctorScheduleService {

    @Override
    public Page<DoctorSchedule> pageList(Integer pageNum, Integer pageSize,
                                          Long doctorId, Long deptId, LocalDate date) {
        LambdaQueryWrapper<DoctorSchedule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DoctorSchedule::getDeleted, 0);
        if (doctorId != null) {
            wrapper.eq(DoctorSchedule::getDoctorId, doctorId);
        }
        if (deptId != null) {
            wrapper.eq(DoctorSchedule::getDeptId, deptId);
        }
        if (date != null) {
            wrapper.eq(DoctorSchedule::getScheduleDate, date);
        }
        wrapper.orderByDesc(DoctorSchedule::getScheduleDate);
        return page(new Page<>(pageNum, pageSize), wrapper);
    }

    @Override
    public List<DoctorSchedule> getAvailableSchedules(Long deptId, LocalDate date) {
        return list(new LambdaQueryWrapper<DoctorSchedule>()
                .eq(DoctorSchedule::getDeleted, 0)
                .eq(DoctorSchedule::getStatus, 1)
                .eq(deptId != null, DoctorSchedule::getDeptId, deptId)
                .eq(date != null, DoctorSchedule::getScheduleDate, date)
                .apply("current_patients < max_patients"));
    }

    @Override
    public void createSchedule(DoctorSchedule schedule) {
        schedule.setDeleted(0);
        schedule.setStatus(1);
        schedule.setCurrentPatients(0);
        save(schedule);
    }

    @Override
    @Transactional
    public void batchCreateSchedule(List<DoctorSchedule> schedules) {
        schedules.forEach(s -> {
            s.setDeleted(0);
            s.setStatus(1);
            s.setCurrentPatients(0);
        });
        saveBatch(schedules);
    }

    @Override
    public void updateSchedule(DoctorSchedule schedule) {
        updateById(schedule);
    }

    @Override
    public void deleteSchedule(Long id) {
        DoctorSchedule schedule = new DoctorSchedule();
        schedule.setId(id);
        schedule.setDeleted(1);
        updateById(schedule);
    }
}
