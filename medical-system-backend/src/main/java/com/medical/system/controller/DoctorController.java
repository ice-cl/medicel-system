package com.medical.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.system.common.Result;
import com.medical.system.entity.Doctor;
import com.medical.system.service.DoctorService;
import com.medical.system.vo.DoctorVO;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/doctor")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/page")
    public Result<Page<Doctor>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long deptId) {
        return Result.success(doctorService.pageList(pageNum, pageSize, deptId));
    }

    @GetMapping("/list")
    public Result<List<DoctorVO>> list(@RequestParam(required = false) Long deptId) {
        return Result.success(doctorService.listDoctorVO(deptId));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Doctor doctor) {
        doctorService.createDoctor(doctor);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Doctor doctor) {
        doctorService.updateDoctor(doctor);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return Result.success();
    }

    @GetMapping("/user/{userId}")
    public Result<Doctor> getByUserId(@PathVariable Long userId) {
        return Result.success(doctorService.getByUserId(userId));
    }

    @GetMapping("/pending")
    public Result<Page<Doctor>> pendingList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        return Result.success(doctorService.pagePendingList(pageNum, pageSize));
    }

    @PutMapping("/verify/{id}")
    public Result<Void> verify(@PathVariable Long id,
                               @RequestParam Integer status) {
        doctorService.verifyDoctor(id, status);
        return Result.success();
    }
}
