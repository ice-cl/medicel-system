package com.medical.system.controller;

import com.medical.system.common.Result;
import com.medical.system.entity.ConsultMessage;
import com.medical.system.service.ConsultMessageService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/consult-message")
public class ConsultMessageController {

    private final ConsultMessageService consultMessageService;

    public ConsultMessageController(ConsultMessageService consultMessageService) {
        this.consultMessageService = consultMessageService;
    }

    @GetMapping("/consultation/{consultationId}")
    public Result<List<ConsultMessage>> listByConsultation(
            @PathVariable Long consultationId) {
        return Result.success(consultMessageService.listByConsultationId(consultationId));
    }

    @PostMapping
    public Result<Void> send(@RequestBody ConsultMessage message) {
        consultMessageService.sendMessage(message);
        return Result.success();
    }
}
