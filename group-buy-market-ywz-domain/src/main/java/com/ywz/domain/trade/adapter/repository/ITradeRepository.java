package com.ywz.domain.trade.adapter.repository;

import com.ywz.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.ywz.domain.trade.model.entity.MarketPayOrderEntity;
import com.ywz.domain.trade.model.valobj.GroupBuyProgressVO;
import org.springframework.stereotype.Repository;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/6/3 16:44
 */
public interface ITradeRepository {

    /**
     * 根据用户ID和外部交易号查询未支付的营销订单
     *
     * @param userId 用户ID
     * @param outTradeNo 外部交易号
     * @return MarketPayOrderEntity
     */
    MarketPayOrderEntity queryMarketPayOrderEntityByOutTradeNo(String userId, String outTradeNo);

    /**
     * 锁定拼团订单
     *
     * @param groupBuyOrderAggregate 拼团订单聚合对象
     * @return MarketPayOrderEntity
     */
    MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate);

    /**
     * 查询拼团进度
     *
     * @param teamId 拼团团队ID
     * @return GroupBuyProgressVO
     */
    GroupBuyProgressVO queryGroupBuyProgress(String teamId);
}
