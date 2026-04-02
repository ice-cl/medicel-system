package com.medical.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.medical.system.entity.ConsultMessage;
import java.util.List;

public interface ConsultMessageService extends IService<ConsultMessage> {
    List<ConsultMessage> listByConsultationId(Long consultationId);
    void sendMessage(ConsultMessage message);
}
