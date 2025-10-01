package com.ywz.domain.trade.model.valobj;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @Author: ywz
 * @CreateTime: 2025-10-01
 * @Description: 回调通知的VO
 * @Version: 1.0
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class NotifyConfigVO {
    private NotifyTypeEnumVO notifyType;
    private String notifyMQ;
    private String notifyUrl;
}
