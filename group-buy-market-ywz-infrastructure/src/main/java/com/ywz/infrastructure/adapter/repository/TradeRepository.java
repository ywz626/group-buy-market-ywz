package com.ywz.infrastructure.adapter.repository;

import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ywz.domain.trade.adapter.repository.ITradeRepository;
import com.ywz.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.ywz.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.ywz.domain.trade.model.entity.*;
import com.ywz.domain.trade.model.valobj.GroupBuyProgressVO;
import com.ywz.domain.trade.model.valobj.TradeOrderStatusEnumVO;
import com.ywz.infrastructure.dao.IGroupBuyActivityDao;
import com.ywz.infrastructure.dao.IGroupBuyOrderDao;
import com.ywz.infrastructure.dao.IGroupBuyOrderListDao;
import com.ywz.infrastructure.dao.INotifyTaskDao;
import com.ywz.infrastructure.dao.po.GroupBuyActivityPO;
import com.ywz.infrastructure.dao.po.GroupBuyOrder;
import com.ywz.infrastructure.dao.po.GroupBuyOrderList;
import com.ywz.infrastructure.dao.po.NotifyTask;
import com.ywz.infrastructure.dcc.DCCService;
import com.ywz.types.enums.ActivityStatusEnumVO;
import com.ywz.types.enums.GroupBuyOrderEnumVO;
import com.ywz.types.enums.ResponseCode;
import com.ywz.types.exception.AppException;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import com.ywz.types.common.Constants;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author 于汶泽
 * @Description: 仓储服务实现
 * @DateTime: 2025/6/3 16:45
 */
@Slf4j
@Repository
public class TradeRepository implements ITradeRepository {

    @Resource
    private IGroupBuyOrderDao groupBuyOrderDao;
    @Resource
    private IGroupBuyOrderListDao groupBuyOrderListDao;
    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;
    @Resource
    private INotifyTaskDao notifyTaskDao;
    @Resource
    private DCCService dccService;


    @Override
    public MarketPayOrderEntity queryMarketLockOrderByOutTradeNo(String userId, String outTradeNo) {
        GroupBuyOrderList groupBuyOrderList = groupBuyOrderListDao.selectOne(new LambdaQueryWrapper<GroupBuyOrderList>()
                .eq(GroupBuyOrderList::getUserId, userId)
                .eq(GroupBuyOrderList::getOutTradeNo, outTradeNo)
                .eq(GroupBuyOrderList::getStatus, TradeOrderStatusEnumVO.CREATE.getCode()));
        if (groupBuyOrderList == null) {
            // 未找到对应的订单
            return null;
        }
        // 整合对象
        return MarketPayOrderEntity.builder()
                .orderId(groupBuyOrderList.getOrderId())
                .teamId(groupBuyOrderList.getTeamId())
                .payPrice(groupBuyOrderList.getPayPrice())
                .originalPrice(groupBuyOrderList.getOriginalPrice())
                .deductionPrice(groupBuyOrderList.getDeductionPrice())
                .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.valueOf(groupBuyOrderList.getStatus()))
                .payPrice(groupBuyOrderList.getPayPrice())
                .build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class) // 设置事务回滚
    public MarketPayOrderEntity lockMarketPayOrder(GroupBuyOrderAggregate groupBuyOrderAggregate) {
        UserEntity userEntity = groupBuyOrderAggregate.getUserEntity();
        PayActivityEntity payActivityEntity = groupBuyOrderAggregate.getPayActivityEntity();
        PayDiscountEntity payDiscountEntity = groupBuyOrderAggregate.getPayDiscountEntity();
        Integer userTakeOrderCount = groupBuyOrderAggregate.getUserTakeOrderCount();
        // 构建拼团订单
        String teamId = payActivityEntity.getTeamId();
        if (StringUtil.isBlank(teamId)) {
            // 新团
            teamId = RandomStringUtils.randomNumeric(8);
            Integer validTime = payActivityEntity.getValidTime();
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.MINUTE, validTime);
            GroupBuyOrder groupBuyOrder = GroupBuyOrder.builder()
                    .teamId(teamId)
                    .activityId(payActivityEntity.getActivityId())
                    .source(payDiscountEntity.getSource())
                    .channel(payDiscountEntity.getChannel())
                    .originalPrice(payDiscountEntity.getOriginalPrice())
                    .deductionPrice(payDiscountEntity.getDeductionPrice())
                    .payPrice(payDiscountEntity.getDeductionPrice())
                    .notifyUrl(payDiscountEntity.getNotifyConfig().getNotifyUrl())
                    .notifyType(payDiscountEntity.getNotifyConfig().getNotifyType())
                    .targetCount(payActivityEntity.getTargetCount())
                    .completeCount(0)
                    .validStartTime(new Date())
                    .validEndTime(calendar.getTime())
                    .lockCount(1)
                    .build();
            groupBuyOrderDao.insert(groupBuyOrder);
        } else {
            // 老团 - 更新拼团订单锁定数量
            int updateAddLockCount = groupBuyOrderDao.update(null, new LambdaUpdateWrapper<GroupBuyOrder>()
                    .setSql(" lock_count = lock_count + 1")
                    .eq(GroupBuyOrder::getTeamId, teamId)
                    .apply("{0} < {1}", "lock_count", "target_count"));

            if (updateAddLockCount != 1) {
                // 拼团已满，抛出异常
                throw new AppException(ResponseCode.E0005);
            }
        }
        String orderId = RandomStringUtils.randomNumeric(12);
        GroupBuyOrderList groupBuyOrderListReq = GroupBuyOrderList.builder()
                .userId(userEntity.getUserId())
                .teamId(teamId)
                .orderId(orderId)
                .activityId(payActivityEntity.getActivityId())
                .startTime(payActivityEntity.getStartTime())
                .endTime(payActivityEntity.getEndTime())
                .goodsId(payDiscountEntity.getGoodsId())
                .source(payDiscountEntity.getSource())
                .channel(payDiscountEntity.getChannel())
                .originalPrice(payDiscountEntity.getOriginalPrice())
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .payPrice(payDiscountEntity.getPayPrice())
                .status(TradeOrderStatusEnumVO.CREATE.getCode())
                .outTradeNo(payDiscountEntity.getOutTradeNo())
                .bizId(payActivityEntity.getActivityId() + Constants.UNDERLINE + userEntity.getUserId() + Constants.UNDERLINE + (userTakeOrderCount + 1))
                .build();
        try {
            // 写入拼团记录
            groupBuyOrderListDao.insert(groupBuyOrderListReq);
        } catch (DuplicateKeyException e) {
            throw new AppException(ResponseCode.INDEX_EXCEPTION);
        }

        return MarketPayOrderEntity.builder()
                .orderId(orderId)
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.COMPLETE)
                .payPrice(payDiscountEntity.getPayPrice())
                .build();
    }

    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.selectOne(new LambdaQueryWrapper<GroupBuyOrder>()
                .eq(GroupBuyOrder::getTeamId, teamId));
        return GroupBuyProgressVO.builder()
                .targetCount(groupBuyOrder.getTargetCount())
                .completeCount(groupBuyOrder.getCompleteCount())
                .lockCount(groupBuyOrder.getLockCount())
                .build();
    }

    @Override
    public GroupBuyActivityEntity queryGroupBuyActivityEntity(Long activityId) {
        GroupBuyActivityPO groupBuyActivityPO = groupBuyActivityDao.selectOne(Wrappers.<GroupBuyActivityPO>lambdaQuery()
                .eq(GroupBuyActivityPO::getActivityId, activityId));
        return GroupBuyActivityEntity.builder()
                .activityName(groupBuyActivityPO.getActivityName())
                .tagId(groupBuyActivityPO.getTagId())
                .groupType(groupBuyActivityPO.getGroupType())
                .discountId(groupBuyActivityPO.getDiscountId())
                .target(groupBuyActivityPO.getTarget())
                .takeLimitCount(groupBuyActivityPO.getTakeLimitCount())
                .validTime(groupBuyActivityPO.getValidTime())
                .tagScope(groupBuyActivityPO.getTagScope())
                .activityId(activityId)
                .status(ActivityStatusEnumVO.valueOf(groupBuyActivityPO.getStatus()))
                .startTime(groupBuyActivityPO.getStartTime())
                .endTime(groupBuyActivityPO.getEndTime())
                .build();
    }

    @Override
    public Integer queryOrerCount(String userId, Long activityId) {
        Long count = groupBuyOrderListDao.selectCount(new LambdaQueryWrapper<GroupBuyOrderList>()
                .eq(GroupBuyOrderList::getUserId, userId)
                .eq(GroupBuyOrderList::getActivityId, activityId));
        return count == null ? 0 : count.intValue();
    }


    @Override
    public GroupBuyTeamEntity queryGroupBuyTeam(String teamId) {
        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.selectOne(Wrappers.<GroupBuyOrder>lambdaQuery()
                .eq(GroupBuyOrder::getTeamId, teamId));

        if (groupBuyOrder == null) {
            return null;
        }
        // 整合对象
        return GroupBuyTeamEntity.builder()
                .teamId(groupBuyOrder.getTeamId())
                .status(GroupBuyOrderEnumVO.valueOf(groupBuyOrder.getStatus()))
                .activityId(groupBuyOrder.getActivityId())
                .completeCount(groupBuyOrder.getCompleteCount())
                .lockCount(groupBuyOrder.getLockCount())
                .targetCount(groupBuyOrder.getTargetCount())
                .createTime(groupBuyOrder.getCreateTime())
                .notifyUrl(groupBuyOrder.getNotifyUrl())
                .build();
    }

    @Override
    public void updateGroupBuyOrderListStatus(String userId, String outTradeNo) {
        groupBuyOrderListDao.update(Wrappers.<GroupBuyOrderList>lambdaUpdate()
                .setSql("status = 1, update_time = now()")
                .eq(GroupBuyOrderList::getUserId, userId)
                .eq(GroupBuyOrderList::getOutTradeNo, outTradeNo));
    }

    @Transactional(rollbackFor = Exception.class, timeout = 500)
    @Override
    public boolean settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate) {
        GroupBuyTeamEntity groupBuyTeamEntity = groupBuyTeamSettlementAggregate.getGroupBuyTeamEntity();
        TradePaySuccessEntity tradePaySuccessEntity = groupBuyTeamSettlementAggregate.getTradePaySuccessEntity();
        UserEntity userEntity = groupBuyTeamSettlementAggregate.getUserEntity();
        // 更新拼团明细订单状态
        int update = groupBuyOrderListDao.update(Wrappers.<GroupBuyOrderList>lambdaUpdate()
                .setSql("status = 1, update_time = now() ,out_trade_time = {0}", tradePaySuccessEntity.getOutTradeTime())
                .eq(GroupBuyOrderList::getUserId, userEntity.getUserId())
                .eq(GroupBuyOrderList::getOutTradeNo, tradePaySuccessEntity.getOutTradeNo()));
        if (update != 1) {
            // 更新失败，抛出异常
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        // 更新拼团订单状态为已完成
        int updateOrder = groupBuyOrderDao.update(Wrappers.<GroupBuyOrder>lambdaUpdate()
                .setSql("complete_count = complete_count + 1")
                .apply("{0} < {1}", "complete_count", "target_count")
                .eq(GroupBuyOrder::getTeamId, groupBuyTeamEntity.getTeamId()));
        if (updateOrder != 1) {
            // 更新失败，抛出异常
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }
        // 获取外部订单号列表
        List<String> outTradeNoList = groupBuyOrderListDao.selectAllOutTradeNoByTeamId(groupBuyTeamEntity.getTeamId());

        if (groupBuyTeamEntity.getTargetCount() == groupBuyTeamEntity.getCompleteCount() + 1) {
            // 最后一单 ，开始结算
            NotifyTask notifyTask = new NotifyTask();
            notifyTask.setActivityId(groupBuyTeamEntity.getActivityId());
            notifyTask.setTeamId(groupBuyTeamEntity.getTeamId());
            notifyTask.setNotifyUrl(groupBuyTeamEntity.getNotifyUrl());
            log.info("notifyUrl:{}",groupBuyTeamEntity.getNotifyUrl());
            notifyTask.setNotifyCount(0);
            notifyTask.setNotifyStatus(0);
            notifyTask.setParameterJson(JSON.toJSONString(new HashMap<String, Object>() {{
                put("teamId", groupBuyTeamEntity.getTeamId());
                put("outTradeNoList", outTradeNoList);
            }}));
            notifyTaskDao.insert(notifyTask);
            // 更新拼团订单状态为已完成
            // TODO
            int updateForGroupBuy = groupBuyOrderDao.update(Wrappers.<GroupBuyOrder>lambdaUpdate()
                    .setSql("status = 1")
                    .eq(GroupBuyOrder::getTeamId, groupBuyTeamEntity.getTeamId()));
            if (updateForGroupBuy != 1) {
                // 订单更新失败
                throw new AppException(ResponseCode.UPDATE_ZERO);
            }
            return true;
        }
        return false;
    }

    @Override
    public int getActivityValidTime(Long activityId) {
        return groupBuyActivityDao.getActivityValidTime(activityId);
    }

    @Override
    public boolean isSCBlackList(String source, String channel) {
        return dccService.isScBlackList(source, channel);
    }

    @Override
    public List<NotifyTaskEntity> queryNotifyTaskEntityListByTeamId(String teamId) {
        NotifyTask notifyTask = notifyTaskDao.selectOne(Wrappers.<NotifyTask>lambdaQuery()
                .eq(NotifyTask::getTeamId, teamId));
        if (null == notifyTask) {
            return new ArrayList<>();
        }
        return Collections.singletonList(
                NotifyTaskEntity.builder()
                        .notifyCount(notifyTask.getNotifyCount())
                        .parameterJson(notifyTask.getParameterJson())
                        .teamId(notifyTask.getTeamId())
                        .notifyUrl(notifyTask.getNotifyUrl())
                        .build()
        );
    }

    @Override
    public int updateNotifyTaskStatusSuccess(String teamId) {
        return notifyTaskDao.update(Wrappers.<NotifyTask>lambdaUpdate()
                .setSql("notify_count = notify_count + 1,notify_status = 1")
                .eq(NotifyTask::getTeamId, teamId));
    }

    @Override
    public int updateNotifyTaskStatusError(String teamId) {
        return notifyTaskDao.update(Wrappers.<NotifyTask>lambdaUpdate()
                .setSql("notify_count = notify_count + 1,notify_status = 3")
                .eq(NotifyTask::getTeamId, teamId));
    }

    @Override
    public int updateNotifyTaskStatusRetry(String teamId) {
        return notifyTaskDao.update(Wrappers.<NotifyTask>lambdaUpdate()
                .setSql("notify_count = notify_count + 1,notify_status = 2")
                .eq(NotifyTask::getTeamId, teamId));
    }

    @Override
    public List<NotifyTaskEntity> queryNotifyTaskEntityListByStatus() {
        List<NotifyTask> notifyTasks = notifyTaskDao.selectList(Wrappers.<NotifyTask>lambdaQuery()
                .in(NotifyTask::getNotifyStatus, 0, 2));

        List<NotifyTaskEntity> notifyTaskEntities = new ArrayList<NotifyTaskEntity>();
        for (NotifyTask notifyTask : notifyTasks) {
            NotifyTaskEntity notifyTaskEntity = NotifyTaskEntity.builder()
                    .notifyCount(notifyTask.getNotifyCount())
                    .parameterJson(notifyTask.getParameterJson())
                    .teamId(notifyTask.getTeamId())
                    .notifyUrl(notifyTask.getNotifyUrl())
                    .build();
            notifyTaskEntities.add(notifyTaskEntity);
        }
        return notifyTaskEntities;
    }


}
