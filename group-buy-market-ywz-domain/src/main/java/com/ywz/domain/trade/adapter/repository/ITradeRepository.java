package com.ywz.domain.trade.adapter.repository;

import com.ywz.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.ywz.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.ywz.domain.trade.model.entity.GroupBuyActivityEntity;
import com.ywz.domain.trade.model.entity.GroupBuyTeamEntity;
import com.ywz.domain.trade.model.entity.MarketPayOrderEntity;
import com.ywz.domain.trade.model.valobj.GroupBuyProgressVO;

import java.util.Date;

/**
 * @author 于汶泽
 * @Description: 仓储接口
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

    /**
     * 查询拼团活动实体
     *
     * @param activityId 活动ID
     * @return GroupBuyActivityEntity
     */
    GroupBuyActivityEntity queryGroupBuyActivityEntity(Long activityId);

    Integer queryOrerCount(String userId, Long activityId);

    /**
     * 查询拼团锁定订单
     *
     * @param outTradeNo 外部交易号
     * @param userId 用户ID
     * @return boolean
     */
    MarketPayOrderEntity queryMarketLockOrderByOutTradeNo(String outTradeNo, String userId);

    GroupBuyTeamEntity queryGroupBuyTeam(String teamId);

    /**
     * 更新拼团订单状态
     *
     *
     */
    void updateGroupBuyOrderListStatus(String userId, String outTradeNo);

    void settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate);

    /**
     * 获取活动有效时间
     *
     * @return Date
     */
    int getActivityValidTime(Long activityId);
}
