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
 * @Description: 判断时间是否在可设置时间范围内的规则过滤器
 * @DateTime: 2025/6/5 15:13
 */
@Service
@Slf4j
public class SettableRuleFilter implements ILogicLinkHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;
    /**
     * 应用团购团队验证规则过滤器
     *
     * @param requestParameter 交易结算规则命令实体，包含交易时间等信息
     * @param dynamicContext 动态上下文，包含市场支付订单实体等运行时信息
     * @return 下一个过滤器处理结果的实体对象
     * @throws Exception 当团购团队不存在或交易时间超过有效结束时间时抛出异常
     */
    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        // 获取市场支付订单实体并查询对应的团购团队信息
        MarketPayOrderEntity marketPayOrderEntity = dynamicContext.getMarketPayOrderEntity();
        String teamId = marketPayOrderEntity.getTeamId();
        GroupBuyTeamEntity groupBuyTeamEntity = repository.queryGroupBuyTeam(teamId);
        if(groupBuyTeamEntity == null){
            throw new AppException(ResponseCode.E0002);
        }

        // 验证交易时间是否在团购有效期内
        Date validEndTime = groupBuyTeamEntity.getValidEndTime();
        Date outTradeTime = requestParameter.getOutTradeTime();
        log.info("validEndTime:{},OutTradeTime:{}",validEndTime, outTradeTime);
        if( validEndTime != null && validEndTime.before(outTradeTime)) {
            throw new AppException(ResponseCode.E0106);
        }

        // 将团购团队实体设置到动态上下文中并执行下一个过滤器
        dynamicContext.setGroupBuyTeamEntity(groupBuyTeamEntity);
        return next(requestParameter, dynamicContext);
    }

}
