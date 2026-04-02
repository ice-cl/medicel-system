package com.medical.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.medical.system.common.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@TableName("consult_message")
public class ConsultMessage extends BaseEntity {
    private Long consultationId;
    private Long senderId;
    private String senderRole;
    private String content;
    private String imageUrl;
}
