package com.medical.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.system.common.Result;
import com.medical.system.entity.MedicalRecord;
import com.medical.system.service.MedicalRecordService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/medical-record")
public class MedicalRecordController {

    private final MedicalRecordService medicalRecordService;

    public MedicalRecordController(MedicalRecordService medicalRecordService) {
        this.medicalRecordService = medicalRecordService;
    }

    @GetMapping("/page")
    public Result<Page<MedicalRecord>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId) {
        return Result.success(medicalRecordService.pageList(
                pageNum, pageSize, patientId, doctorId));
    }

    @GetMapping("/{id}")
    public Result<MedicalRecord> getById(@PathVariable Long id) {
        return Result.success(medicalRecordService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody MedicalRecord record) {
        medicalRecordService.createRecord(record);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody MedicalRecord record) {
        medicalRecordService.updateRecord(record);
        return Result.success();
    }
}
