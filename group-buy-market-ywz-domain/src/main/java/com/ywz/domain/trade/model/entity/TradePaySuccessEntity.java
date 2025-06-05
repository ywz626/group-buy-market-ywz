package com.ywz.domain.trade.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 于汶泽
 * @Description: 交易支付订单实体对象
 * @DateTime: 2025/6/4 21:10
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradePaySuccessEntity {

    /** 渠道 */
    private String source;
    /** 来源 */
    private String channel;
    /** 用户ID */
    private String userId;
    /** 外部交易单号 */
    private String outTradeNo;
}
