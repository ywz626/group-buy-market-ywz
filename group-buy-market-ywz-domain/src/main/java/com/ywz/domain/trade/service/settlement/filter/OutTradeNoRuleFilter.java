package com.ywz.domain.trade.service.settlement.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import com.ywz.domain.trade.adapter.repository.ITradeRepository;
import com.ywz.domain.trade.model.entity.MarketPayOrderEntity;
import com.ywz.domain.trade.model.entity.TradeSettlementRuleCommandEntity;
import com.ywz.domain.trade.model.entity.TradeSettlementRuleFilterBackEntity;
import com.ywz.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
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
public class OutTradeNoRuleFilter implements ILogicHandler<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;

    /**
     * 应用交易结算规则过滤器，根据外部交易单号查询市场支付订单信息
     *
     * @param requestParameter 交易结算规则命令实体，包含用户ID和外部交易单号等信息
     * @param dynamicContext 动态上下文环境，用于在过滤器链中传递数据
     * @return TradeSettlementRuleFilterBackEntity 交易结算规则过滤器返回实体
     * @throws Exception 当查询过程中发生异常时抛出
     */
    @Override
    public TradeSettlementRuleFilterBackEntity apply(TradeSettlementRuleCommandEntity requestParameter, TradeSettlementRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        // 根据用户ID和外部交易单号查询市场锁定订单信息
        MarketPayOrderEntity marketPayOrderEntity = repository.queryMarketLockOrderByOutTradeNo(requestParameter.getUserId(), requestParameter.getOutTradeNo());
        if (marketPayOrderEntity == null) {
            // 外部交易单号不存在
            throw new AppException(ResponseCode.E0104);
        }
        // 将查询到的市场支付订单实体设置到动态上下文中
        dynamicContext.setMarketPayOrderEntity(marketPayOrderEntity);
        // 存在该外部交易单号，继续执行下一个过滤器
        return next(requestParameter,dynamicContext);
    }

}
