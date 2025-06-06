package com.ywz.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 于汶泽
 * @Description: 交易支付结算实体对象
 * @DateTime: 2025/6/4 21:07
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradePaySettlementEntity {

    private String source;

    private String channel;

    private String userId;

    private String teamId;

    private Long activityId;

    private String outTradeNo;

}
