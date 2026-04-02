package com.medical.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.system.common.Result;
import com.medical.system.entity.Doctor;
import com.medical.system.entity.DoctorSchedule;
import com.medical.system.service.DoctorService;
import com.medical.system.service.DoctorScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/schedule")
public class DoctorScheduleController {

    private final DoctorScheduleService scheduleService;
    private final DoctorService doctorService;

    public DoctorScheduleController(DoctorScheduleService scheduleService,
                                    DoctorService doctorService) {
        this.scheduleService = scheduleService;
        this.doctorService = doctorService;
    }

    @GetMapping("/page")
    public Result<Page<DoctorSchedule>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long deptId,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(authentication)) {
            Long userId = getCurrentUserId(authentication);
            Doctor doctor = doctorService.getByUserId(userId);
            if (doctor == null) {
                throw new RuntimeException("医生信息不存在");
            }
            doctorId = doctor.getId();
        }
        return Result.success(scheduleService.pageList(pageNum, pageSize, doctorId, deptId, date));
    }

    @GetMapping("/available")
    public Result<List<DoctorSchedule>> available(
            @RequestParam(required = false) Long deptId,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate date) {
        return Result.success(scheduleService.getAvailableSchedules(deptId, date));
    }

    @PostMapping
    public Result<Void> create(@RequestBody DoctorSchedule schedule) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(authentication)) {
            Long userId = getCurrentUserId(authentication);
            Doctor doctor = doctorService.getByUserId(userId);
            if (doctor == null) {
                throw new RuntimeException("医生信息不存在");
            }
            schedule.setDoctorId(doctor.getId());
            schedule.setDeptId(doctor.getDeptId());
        }
        scheduleService.createSchedule(schedule);
        return Result.success();
    }

    @PostMapping("/batch")
    public Result<Void> batchCreate(@RequestBody List<DoctorSchedule> schedules) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(authentication)) {
            Long userId = getCurrentUserId(authentication);
            Doctor doctor = doctorService.getByUserId(userId);
            if (doctor == null) {
                throw new RuntimeException("医生信息不存在");
            }
            schedules.forEach(schedule -> {
                schedule.setDoctorId(doctor.getId());
                schedule.setDeptId(doctor.getDeptId());
            });
        }
        scheduleService.batchCreateSchedule(schedules);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody DoctorSchedule schedule) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(authentication)) {
            Long userId = getCurrentUserId(authentication);
            Doctor doctor = doctorService.getByUserId(userId);
            if (doctor == null) {
                throw new RuntimeException("医生信息不存在");
            }
            DoctorSchedule origin = scheduleService.getById(schedule.getId());
            if (origin == null || !doctor.getId().equals(origin.getDoctorId())) {
                throw new RuntimeException("只能修改自己的排班");
            }
            schedule.setDoctorId(doctor.getId());
            schedule.setDeptId(doctor.getDeptId());
        }
        scheduleService.updateSchedule(schedule);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (!isAdmin(authentication)) {
            Long userId = getCurrentUserId(authentication);
            Doctor doctor = doctorService.getByUserId(userId);
            if (doctor == null) {
                throw new RuntimeException("医生信息不存在");
            }
            DoctorSchedule origin = scheduleService.getById(id);
            if (origin == null || !doctor.getId().equals(origin.getDoctorId())) {
                throw new RuntimeException("只能删除自己的排班");
            }
        }
        scheduleService.deleteSchedule(id);
        return Result.success();
    }

    private boolean isAdmin(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch("ROLE_ADMIN"::equals);
    }

    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null || !(authentication.getPrincipal() instanceof Long)) {
            throw new RuntimeException("未获取到当前登录用户");
        }
        return (Long) authentication.getPrincipal();
    }
}
