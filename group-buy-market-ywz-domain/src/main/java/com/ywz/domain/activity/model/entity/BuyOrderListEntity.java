package com.ywz.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/6/7 18:50
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class BuyOrderListEntity {
    private String goodName;

    private Date tradeCreateTime;
    /** 交易状态 0-待支付 1-进行中 2-已完成 3-以失败 */
    private Integer status;

    private Integer userNo;

    private String outTradeNo;
}
