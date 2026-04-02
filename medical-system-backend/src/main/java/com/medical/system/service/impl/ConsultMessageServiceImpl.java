package com.medical.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.medical.system.entity.ConsultMessage;
import com.medical.system.mapper.ConsultMessageMapper;
import com.medical.system.service.ConsultMessageService;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ConsultMessageServiceImpl extends ServiceImpl<ConsultMessageMapper, ConsultMessage>
        implements ConsultMessageService {

    @Override
    public List<ConsultMessage> listByConsultationId(Long consultationId) {
        return list(new LambdaQueryWrapper<ConsultMessage>()
                .eq(ConsultMessage::getConsultationId, consultationId)
                .orderByAsc(ConsultMessage::getCreateTime));
    }

    @Override
    public void sendMessage(ConsultMessage message) {
        save(message);
    }
}
