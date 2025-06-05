package com.ywz.domain.trade.service.lock;

import com.ywz.domain.trade.adapter.repository.ITradeRepository;
import com.ywz.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.ywz.domain.trade.model.entity.*;
import com.ywz.domain.trade.model.valobj.GroupBuyProgressVO;
import com.ywz.domain.trade.service.ITradeOrderService;
import com.ywz.domain.trade.service.lock.factory.TradeRuleFilterFactory;
import com.ywz.types.design.framework.link.model2.chain.BusinessLinkedList;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * @author 于汶泽
 * @Description: 仓储服务实现类
 * @DateTime: 2025/6/3 19:07
 */
@Service
public class TradeOrderService implements ITradeOrderService {

    private final ITradeRepository tradeRepository;

    @Resource(name = "tradeRuleFilter")
    private BusinessLinkedList<TradeRuleCommandEntity, TradeRuleFilterFactory.DynamicContext, TradeRuleFilterBackEntity> tradeRuleFilter;

    public TradeOrderService(ITradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @Override
    public MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo) {
        return tradeRepository.queryMarketPayOrderEntityByOutTradeNo(userId, outTradeNo);
    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        return tradeRepository.queryGroupBuyProgress(teamId);
    }

    @Override
    public MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity, PayDiscountEntity payDiscountEntity) throws Exception {
        TradeRuleCommandEntity tradeRuleCommand = TradeRuleCommandEntity.builder()
                .activityId(payActivityEntity.getActivityId())
                .userId(userEntity.getUserId())
                .build();
        // 应用规则过滤器链
        TradeRuleFilterBackEntity tradeBackEntity = tradeRuleFilter.apply(tradeRuleCommand, new TradeRuleFilterFactory.DynamicContext());
        Integer userTakeOrderCount = tradeBackEntity.getUserTakeOrderCount();

        // 构建聚合对象
        GroupBuyOrderAggregate groupBuyOrderAggregate = GroupBuyOrderAggregate.builder()
                .userEntity(userEntity)
                .payActivityEntity(payActivityEntity)
                .payDiscountEntity(payDiscountEntity)
                .userTakeOrderCount(userTakeOrderCount)
                .build();
        return tradeRepository.lockMarketPayOrder(groupBuyOrderAggregate);
    }
}
