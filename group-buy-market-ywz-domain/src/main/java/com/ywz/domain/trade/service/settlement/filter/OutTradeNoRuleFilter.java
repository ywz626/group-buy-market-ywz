package com.ywz.domain.trade.service.settlement.filter;

import com.ywz.domain.trade.adapter.repository.ITradeRepository;
import com.ywz.domain.trade.model.entity.MarketPayOrderEntity;
import com.ywz.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import com.ywz.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import com.ywz.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import com.ywz.types.design.framework.link.model2.handler.ILogicLinkHandler;
import com.ywz.types.enums.ResponseCode;
import com.ywz.types.exception.AppException;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author 于汶泽
 * @Description: 判断外部交易单号是否存在的规则过滤器
 * @DateTime: 2025/6/5 15:12
 */
@Service
public class OutTradeNoRuleFilter implements ILogicLinkHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;

    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        MarketPayOrderEntity marketPayOrderEntity = repository.queryMarketLockOrderByOutTradeNo(requestParameter.getUserId(), requestParameter.getOutTradeNo());
        if (marketPayOrderEntity == null) {
            // 外部交易单号不存在
            throw new AppException(ResponseCode.E0104);
        }
        dynamicContext.setMarketPayOrderEntity(marketPayOrderEntity);
        // 存在该外部交易单号
        return next(requestParameter,dynamicContext);
    }
}
