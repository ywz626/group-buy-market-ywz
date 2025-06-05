package com.ywz.domain.trade.service.settlement.filter;

import com.ywz.domain.trade.adapter.repository.ITradeRepository;
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
 * @Description: 过滤黑名单节点
 * @DateTime: 2025/6/5 14:46
 */
@Service
public class SCRuleFilter implements ILogicLinkHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;

    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        // 进行过滤黑名单操作
        boolean isBlack = repository.isSCBlackList(requestParameter.getSource(),requestParameter.getChannel());

        if (isBlack) {
            // 如果是黑名单用户，直接返回空的过滤结果
            throw new AppException(ResponseCode.E0105);
        }
        return next(requestParameter, dynamicContext);
    }
}
