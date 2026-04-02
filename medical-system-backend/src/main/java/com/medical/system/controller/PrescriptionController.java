package com.medical.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.system.common.Result;
import com.medical.system.entity.Prescription;
import com.medical.system.service.PrescriptionService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prescription")
public class PrescriptionController {

    private final PrescriptionService prescriptionService;

    public PrescriptionController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @GetMapping("/page")
    public Result<Page<Prescription>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String drugName) {
        return Result.success(prescriptionService.pageList(pageNum, pageSize, drugName));
    }

    @GetMapping("/record/{recordId}")
    public Result<List<Prescription>> listByRecord(@PathVariable Long recordId) {
        return Result.success(prescriptionService.listByRecordId(recordId));
    }

    @GetMapping("/{id}")
    public Result<Prescription> getById(@PathVariable Long id) {
        return Result.success(prescriptionService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Prescription prescription) {
        prescriptionService.createPrescription(prescription);
        return Result.success();
    }

    @PostMapping("/batch")
    public Result<Void> batchCreate(@RequestBody List<Prescription> prescriptions) {
        prescriptionService.batchCreate(prescriptions);
        return Result.success();
    }

    @PutMapping
    public Result<Void> update(@RequestBody Prescription prescription) {
        prescriptionService.updatePrescription(prescription);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        prescriptionService.deletePrescription(id);
        return Result.success();
    }
}
