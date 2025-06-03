package com.ywz.domain.trade.service;

import com.ywz.domain.trade.adapter.repository.ITradeRepository;
import com.ywz.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.ywz.domain.trade.model.entity.MarketPayOrderEntity;
import com.ywz.domain.trade.model.entity.PayActivityEntity;
import com.ywz.domain.trade.model.entity.PayDiscountEntity;
import com.ywz.domain.trade.model.entity.UserEntity;
import com.ywz.domain.trade.model.valobj.GroupBuyProgressVO;
import org.springframework.stereotype.Service;


/**
 * @author 于汶泽
 * @Description: 仓储服务实现类
 * @DateTime: 2025/6/3 19:07
 */
@Service
public class TradeOrderService implements ITradeOrderService {

    private final ITradeRepository tradeRepository;

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
    public MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity, PayDiscountEntity payDiscountEntity) {
        // 构建聚合对象
        GroupBuyOrderAggregate groupBuyOrderAggregate = GroupBuyOrderAggregate.builder()
                .userEntity(userEntity)
                .payActivityEntity(payActivityEntity)
                .payDiscountEntity(payDiscountEntity)
                .build();
        return tradeRepository.lockMarketPayOrder(groupBuyOrderAggregate);
    }
}
