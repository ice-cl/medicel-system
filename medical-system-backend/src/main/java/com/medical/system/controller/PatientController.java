package com.medical.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.system.common.Result;
import com.medical.system.entity.Patient;
import com.medical.system.service.PatientService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patient")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/page")
    public Result<Page<Patient>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        return Result.success(patientService.pageList(pageNum, pageSize, name));
    }

    @GetMapping("/list")
    public Result<List<Patient>> list() {
        return Result.success(patientService.list());
    }

    @GetMapping("/{id}")
    public Result<Patient> getById(@PathVariable Long id) {
        return Result.success(patientService.getById(id));
    }

    @GetMapping("/user/{userId}")
    public Result<Patient> getByUserId(@PathVariable Long userId) {
        return Result.success(patientService.getByUserId(userId));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Patient patient) {
        patientService.createPatient(patient);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Patient patient) {
        patientService.updatePatient(patient);
        return Result.success();
    }
}
