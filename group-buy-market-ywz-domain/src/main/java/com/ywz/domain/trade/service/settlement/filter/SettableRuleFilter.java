package com.ywz.domain.trade.service.settlement.filter;

import com.ywz.domain.trade.adapter.repository.ITradeRepository;
import com.ywz.domain.trade.model.entity.*;
import com.ywz.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import com.ywz.types.design.framework.link.model2.handler.ILogicLinkHandler;
import com.ywz.types.enums.ResponseCode;
import com.ywz.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author 于汶泽
 * @Description: 结束节点
 * @DateTime: 2025/6/5 15:13
 */
@Service
@Slf4j
public class SettableRuleFilter implements ILogicLinkHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;
    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        MarketPayOrderEntity marketPayOrderEntity = dynamicContext.getMarketPayOrderEntity();
        String teamId = marketPayOrderEntity.getTeamId();
        GroupBuyTeamEntity groupBuyTeamEntity = repository.queryGroupBuyTeam(teamId);
        if(groupBuyTeamEntity == null){
            throw new AppException(ResponseCode.E0002);
        }
        Date validEndTime = groupBuyTeamEntity.getValidEndTime();
        Date outTradeTime = requestParameter.getOutTradeTime();
        log.info("validEndTime:{},OutTradeTime:{}",validEndTime, outTradeTime);
        if( validEndTime != null && validEndTime.before(outTradeTime)) {
            throw new AppException(ResponseCode.E0106);
        }
        dynamicContext.setGroupBuyTeamEntity(groupBuyTeamEntity);
        return next(requestParameter, dynamicContext);
    }
}
