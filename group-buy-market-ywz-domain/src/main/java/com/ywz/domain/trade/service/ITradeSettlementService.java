package com.ywz.domain.trade.service;

import com.ywz.domain.trade.model.entity.TradePaySettlementEntity;
import com.ywz.domain.trade.model.entity.TradePaySuccessEntity;

import java.util.Map;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/6/4 21:13
 */
public interface ITradeSettlementService {

    TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) throws Exception;

    Map<String, Integer> execSettlementNotifyJob(String teamId) throws Exception;


    /**
     * 执行结算通知任务
     *
     * @return 结算数量
     * @throws Exception 异常
     */
    Map<String, Integer> execSettlementNotifyJob() throws Exception;
}
