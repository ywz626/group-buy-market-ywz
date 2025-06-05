package com.ywz.domain.trade.adapter.repository;

import com.ywz.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.ywz.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.ywz.domain.trade.model.entity.GroupBuyActivityEntity;
import com.ywz.domain.trade.model.entity.GroupBuyTeamEntity;
import com.ywz.domain.trade.model.entity.MarketPayOrderEntity;
import com.ywz.domain.trade.model.entity.NotifyTaskEntity;
import com.ywz.domain.trade.model.valobj.GroupBuyProgressVO;

import java.util.Date;
import java.util.List;

/**
 * @author 于汶泽
 * @Description: 仓储接口
 * @DateTime: 2025/6/3 16:44
 */
public interface ITradeRepository {


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

    /**
     * 查询拼团组队实体
     *
     * @param teamId 拼团组队ID
     * @return GroupBuyTeamEntity
     */
    GroupBuyTeamEntity queryGroupBuyTeam(String teamId);

    /**
     * 更新拼团订单状态
     *
     *
     */
    void updateGroupBuyOrderListStatus(String userId, String outTradeNo);

    boolean settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate);

    /**
     * 获取活动有效时间
     *
     * @return Date
     */
    int getActivityValidTime(Long activityId);

    /**
     * 判断是否在黑名单中
     * @param source 来源
     * @param channel 渠道
     * @return 是否在黑名单中
     */
    boolean isSCBlackList(String source, String channel);


    List<NotifyTaskEntity> queryNotifyTaskEntityListByStatus();

    List<NotifyTaskEntity> queryNotifyTaskEntityListByTeamId(String teamId);

    int updateNotifyTaskStatusSuccess(String teamId);

    int updateNotifyTaskStatusError(String teamId);

    int updateNotifyTaskStatusRetry(String teamId);
}
