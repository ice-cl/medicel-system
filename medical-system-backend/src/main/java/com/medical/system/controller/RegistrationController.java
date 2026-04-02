package com.medical.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.medical.system.common.Result;
import com.medical.system.entity.Registration;
import com.medical.system.service.RegistrationService;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/registration")
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/page")
    public Result<Page<Registration>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Integer status) {
        return Result.success(registrationService.pageList(
                pageNum, pageSize, patientId, doctorId, status));
    }

    @PostMapping
    public Result<Void> create(@RequestBody Registration registration) {
        registrationService.createRegistration(registration);
        return Result.success();
    }

    @PutMapping("/cancel/{id}")
    public Result<Void> cancel(@PathVariable Long id) {
        registrationService.cancelRegistration(id);
        return Result.success();
    }

    @PutMapping("/status/{id}")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam Integer status) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = authentication != null && authentication.getPrincipal() instanceof Long
                ? (Long) authentication.getPrincipal() : null;
        registrationService.updateStatus(id, status, userId);
        return Result.success();
    }
}
