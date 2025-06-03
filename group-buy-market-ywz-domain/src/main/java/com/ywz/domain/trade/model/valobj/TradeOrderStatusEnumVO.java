package com.ywz.domain.trade.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author ywz
 * @description 交易订单状态枚举
 * @create 2025-01-11 10:21
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum TradeOrderStatusEnumVO {

    /**
     * 交易订单状态枚举
     * CREATE(0, "初始创建"),
     * COMPLETE(1, "消费完成"),
     * CLOSE(2, "超时关单"),
     */
    CREATE(0, "初始创建"),
    COMPLETE(1, "消费完成"),
    CLOSE(2, "超时关单"),
    ;

    private Integer code;
    private String info;

    public static TradeOrderStatusEnumVO valueOf(Integer code) {
        switch (code) {
            case 1:
                return COMPLETE;
            case 2:
                return CLOSE;
            case 0:
            default:
                return CREATE;
        }

    }

}
