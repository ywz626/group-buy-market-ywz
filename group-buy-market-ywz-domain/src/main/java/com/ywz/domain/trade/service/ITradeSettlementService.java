package com.ywz.domain.trade.service;

import com.ywz.domain.trade.model.entity.NotifyTaskEntity;
import com.ywz.domain.trade.model.entity.TradePaySettlementEntity;
import com.ywz.domain.trade.model.entity.TradePaySuccessEntity;

import java.util.Map;

/**
 * @author 于汶泽
 * @Description: 订单支付结算服务接口
 * @DateTime: 2025/6/4 21:13
 */
public interface ITradeSettlementService {

    /**
     * 执行结算
     *
     * @param tradePaySuccessEntity 交易支付成功实体
     * @return 结算实体
     * @throws Exception 异常
     */
    TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) throws Exception;

    Map<String, Integer> execSettlementNotifyJob(String teamId) throws Exception;


    /**
     * 执行结算通知任务
     *
     * @return 结算数量
     * @throws Exception 异常
     */
    Map<String, Integer> execSettlementNotifyJob() throws Exception;

    Map<String, Integer> execSettlementNotifyJob(NotifyTaskEntity notifyTask) throws Exception;
}
