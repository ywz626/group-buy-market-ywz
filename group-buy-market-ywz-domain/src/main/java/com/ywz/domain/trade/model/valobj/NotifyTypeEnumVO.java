package com.ywz.domain.trade.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum NotifyTypeEnumVO {
    MQ("MQ","MQ消息回调"),
    HTTP("HTTP","HTTP回调");

    private String code;
    private String info;
}
