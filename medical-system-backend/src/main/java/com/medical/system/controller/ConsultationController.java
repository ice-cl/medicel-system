package com.medical.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.system.common.Result;
import com.medical.system.entity.Consultation;
import com.medical.system.service.ConsultationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/consultation")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @GetMapping("/page")
    public Result<Page<Consultation>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Integer status) {
        return Result.success(consultationService.pageList(
                pageNum, pageSize, patientId, doctorId, status));
    }

    @GetMapping("/{id}")
    public Result<Consultation> getById(@PathVariable Long id) {
        return Result.success(consultationService.getById(id));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Consultation consultation) {
        consultationService.createConsultation(consultation);
        return Result.success();
    }

    @PutMapping("/end/{id}")
    public Result<Void> end(@PathVariable Long id) {
        consultationService.endConsultation(id);
        return Result.success();
    }
}
