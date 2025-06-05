package com.ywz.domain.trade.service;

import com.ywz.domain.trade.model.entity.TradePaySettlementEntity;
import com.ywz.domain.trade.model.entity.TradePaySuccessEntity;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/6/4 21:13
 */
public interface ITradeSettlementService {

    TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) throws Exception;
}
