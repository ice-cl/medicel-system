package com.medical.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.system.common.Result;
import com.medical.system.entity.HealthData;
import com.medical.system.service.HealthDataService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/health-data")
public class HealthDataController {

    private final HealthDataService healthDataService;

    public HealthDataController(HealthDataService healthDataService) {
        this.healthDataService = healthDataService;
    }

    @GetMapping("/page")
    public Result<Page<HealthData>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) String type) {
        return Result.success(healthDataService.pageList(pageNum, pageSize, patientId, type));
    }

    @GetMapping("/trend")
    public Result<List<HealthData>> trend(
            @RequestParam Long patientId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime) {
        return Result.success(healthDataService.listByPatientAndType(patientId, type, startTime, endTime));
    }

    @PostMapping
    public Result<Void> create(@RequestBody HealthData healthData) {
        healthDataService.createHealthData(healthData);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        healthDataService.deleteHealthData(id);
        return Result.success();
    }
}
