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
import com.ywz.domain.trade.model.valobj.NotifyConfigVO;
import com.ywz.domain.trade.model.valobj.NotifyTypeEnumVO;
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
import com.ywz.infrastructure.event.EventPublisher;
import com.ywz.infrastructure.redis.RedissonService;
import com.ywz.types.enums.ActivityStatusEnumVO;
import com.ywz.types.enums.GroupBuyOrderEnumVO;
import com.ywz.types.enums.ResponseCode;
import com.ywz.types.exception.AppException;
import jodd.util.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;
import com.ywz.types.common.Constants;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.TimeUnit;

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
    @Value("${spring.rabbitmq.config.producer.topic_team_success.routing_key}")
    private String routingKey;
    @Autowired
    private RedissonService redissonService;


    /**
     * 根据用户ID和外部交易号查询市场锁定订单
     *
     * @param userId 用户ID
     * @param outTradeNo 外部交易号
     * @return MarketPayOrderEntity 订单实体对象，如果未找到对应订单则返回null
     */
    @Override
    public MarketPayOrderEntity queryMarketLockOrderByOutTradeNo(String userId, String outTradeNo) {
        // 查询团购订单列表中状态为CREATE的订单
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


    /**
     * 锁定拼团支付订单信息，并创建或更新相关拼团记录。
     * <p>
     * 该方法根据传入的聚合根对象判断是新建一个拼团还是加入已有拼团。如果是新团，则生成新的 teamId 并初始化拼团主表；
     * 如果是老团，则增加锁单数（lock_count），若超出目标人数则抛出异常。
     * 同时会插入一条拼团明细记录到 group_buy_order_list 表中。
     *
     * @param groupBuyOrderAggregate 包含用户、活动、优惠等信息的聚合根对象，用于构建拼团订单数据
     * @return 返回封装好的市场支付订单实体，包含订单ID、抵扣价格、交易状态及实际支付金额
     * @throws AppException 当拼团已满或其他业务逻辑错误时抛出
     */
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
            // 新建拼团：生成唯一 teamId 和有效时间，初始化拼团主表记录
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
                    .notifyType(payDiscountEntity.getNotifyConfig().getNotifyType().getCode())
                    .targetCount(payActivityEntity.getTargetCount())
                    .completeCount(0)
                    .validStartTime(new Date())
                    .validEndTime(calendar.getTime())
                    .lockCount(1)
                    .build();
            groupBuyOrderDao.insert(groupBuyOrder);
        } else {
            // 加入现有拼团：更新锁定数量，如果拼团已满则抛出异常
            int updateAddLockCount = groupBuyOrderDao.update(null, new LambdaUpdateWrapper<GroupBuyOrder>()
                    .setSql(" lock_count = lock_count + 1")
                    .eq(GroupBuyOrder::getTeamId, teamId)
                    .apply("{0} < {1}", "lock_count", "target_count"));

            if (updateAddLockCount != 1) {
                // 拼团已满，抛出异常
                throw new AppException(ResponseCode.E0005);
            }
        }

        // 生成订单号并组装拼团明细记录
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
            // 插入拼团明细记录
            groupBuyOrderListDao.insert(groupBuyOrderListReq);
        } catch (DuplicateKeyException e) {
            // 处理重复键异常，防止并发导致的问题
            throw new AppException(ResponseCode.INDEX_EXCEPTION);
        }

        // 封装返回结果
        return MarketPayOrderEntity.builder()
                .orderId(orderId)
                .deductionPrice(payDiscountEntity.getDeductionPrice())
                .tradeOrderStatusEnumVO(TradeOrderStatusEnumVO.COMPLETE)
                .payPrice(payDiscountEntity.getPayPrice())
                .build();
    }


    /**
     * 查询团购进度信息
     *
     * @param teamId 团队ID，用于查询对应的团购订单信息
     * @return GroupBuyProgressVO 团购进度信息对象，包含目标数量、已完成数量和锁定数量
     */
    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        // 根据团队ID查询团购订单信息
        GroupBuyOrder groupBuyOrder = groupBuyOrderDao.selectOne(new LambdaQueryWrapper<GroupBuyOrder>()
                .eq(GroupBuyOrder::getTeamId, teamId));

        // 构建并返回团购进度信息VO对象
        return GroupBuyProgressVO.builder()
                .targetCount(groupBuyOrder.getTargetCount())
                .completeCount(groupBuyOrder.getCompleteCount())
                .lockCount(groupBuyOrder.getLockCount())
                .build();
    }


    /**
     * 根据活动ID查询团购活动实体信息
     *
     * @param activityId 活动ID
     * @return GroupBuyActivityEntity 团购活动实体对象
     */
    @Override
    public GroupBuyActivityEntity queryGroupBuyActivityEntity(Long activityId) {
        // 查询团购活动PO对象
        GroupBuyActivityPO groupBuyActivityPO = groupBuyActivityDao.selectOne(Wrappers.<GroupBuyActivityPO>lambdaQuery()
                .eq(GroupBuyActivityPO::getActivityId, activityId));

        // 将PO对象转换为Entity对象并返回
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


    /**
     * 查询用户在指定活动中的团购订单数量
     *
     * @param userId 用户ID
     * @param activityId 活动ID
     * @return 用户在指定活动中的订单数量，如果查询结果为空则返回0
     */
    @Override
    public Integer queryOrerCount(String userId, Long activityId) {
        // 查询用户在指定活动中的订单数量
        Long count = groupBuyOrderListDao.selectCount(new LambdaQueryWrapper<GroupBuyOrderList>()
                .eq(GroupBuyOrderList::getUserId, userId)
                .eq(GroupBuyOrderList::getActivityId, activityId));
        // 将查询结果转换为Integer类型，空值处理为0
        return count == null ? 0 : count.intValue();
    }



    /**
     * 根据团队ID查询团购团队信息
     *
     * @param teamId 团队ID，用于查询对应的团购订单信息
     * @return GroupBuyTeamEntity 团购团队实体对象，包含团队的详细信息；如果未找到对应的团购订单则返回null
     */
    @Override
    public GroupBuyTeamEntity queryGroupBuyTeam(String teamId) {
        // 根据团队ID查询团购订单信息
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
                .notifyConfig(NotifyConfigVO.builder()
                        .notifyType(NotifyTypeEnumVO.valueOf(groupBuyOrder.getNotifyType()))
                        .notifyMQ(routingKey)
                        .notifyUrl(groupBuyOrder.getNotifyUrl())
                        .build())
                .build();
    }


    /**
     * 更新拼单明细订单状态
     *
     * @param userId 用户ID
     * @param outTradeNo 外部交易号
     */
    @Override
    public void updateGroupBuyOrderListStatus(String userId, String outTradeNo) {
        // 更新拼单订单状态为1(已支付)，并更新修改时间
        groupBuyOrderListDao.update(Wrappers.<GroupBuyOrderList>lambdaUpdate()
                .setSql("status = 1, update_time = now()")
                .eq(GroupBuyOrderList::getUserId, userId)
                .eq(GroupBuyOrderList::getOutTradeNo, outTradeNo));
    }


    @Resource
    private EventPublisher eventPublisher;

    /**
     * 拼单结算
     * <p>
     * 该方法用于处理拼团单的支付成功后的结算逻辑。包括更新拼团明细订单状态、更新拼团主订单完成数，
     * 若为最后一单则触发通知任务，并更新拼团主订单状态为已完成。
     * </p>
     *
     * @param groupBuyTeamSettlementAggregate 包含拼团团队实体、交易支付成功信息及用户信息的聚合对象
     * @return 是否是最后一单并完成了整个拼团（true 表示拼团已结束，false 表示仍在进行中）
     */
    @Transactional(rollbackFor = Exception.class, timeout = 500)
    @Override
    public NotifyTaskEntity settlementMarketPayOrder(GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate) {
        GroupBuyTeamEntity groupBuyTeamEntity = groupBuyTeamSettlementAggregate.getGroupBuyTeamEntity();
        TradePaySuccessEntity tradePaySuccessEntity = groupBuyTeamSettlementAggregate.getTradePaySuccessEntity();
        UserEntity userEntity = groupBuyTeamSettlementAggregate.getUserEntity();

        // 更新当前用户的拼团明细订单状态为已支付，并记录外部交易时间
        int update = groupBuyOrderListDao.update(Wrappers.<GroupBuyOrderList>lambdaUpdate()
                .setSql("status = 1, update_time = now() ,out_trade_time = {0}", tradePaySuccessEntity.getOutTradeTime())
                .eq(GroupBuyOrderList::getUserId, userEntity.getUserId())
                .eq(GroupBuyOrderList::getOutTradeNo, tradePaySuccessEntity.getOutTradeNo()));
        if (update != 1) {
            // 更新失败，抛出异常
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        // 增加拼团主订单的已完成人数计数，仅当未达到目标人数时才允许更新
        int updateOrder = groupBuyOrderDao.update(Wrappers.<GroupBuyOrder>lambdaUpdate()
                .setSql("complete_count = complete_count + 1")
                .apply("{0} < {1}", "complete_count", "target_count")
                .eq(GroupBuyOrder::getTeamId, groupBuyTeamEntity.getTeamId()));
        if (updateOrder != 1) {
            // 更新失败，抛出异常
            throw new AppException(ResponseCode.UPDATE_ZERO);
        }

        // 查询该拼团下的所有外部订单编号列表
        List<String> outTradeNoList = groupBuyOrderListDao.selectAllOutTradeNoByTeamId(groupBuyTeamEntity.getTeamId());

        // 判断是否是最后一单，如果是则执行最终结算流程
        if (groupBuyTeamEntity.getTargetCount() == groupBuyTeamEntity.getCompleteCount() + 1) {
            // 构造通知任务数据并插入数据库，供后续异步通知使用
            NotifyTask notifyTask = new NotifyTask();
            notifyTask.setActivityId(groupBuyTeamEntity.getActivityId());
            notifyTask.setTeamId(groupBuyTeamEntity.getTeamId());
            NotifyConfigVO notifyConfig = groupBuyTeamEntity.getNotifyConfig();
            notifyTask.setNotifyUrl(notifyConfig.getNotifyUrl());
            notifyTask.setNotifyType(notifyConfig.getNotifyType().getCode());
            notifyTask.setNotifyMq(notifyConfig.getNotifyMQ());
            log.info("notifyUrl:{}", notifyConfig.getNotifyUrl());
            notifyTask.setNotifyCount(0);
            notifyTask.setNotifyStatus(0);
            notifyTask.setParameterJson(JSON.toJSONString(new HashMap<String, Object>() {{
                put("teamId", groupBuyTeamEntity.getTeamId());
                put("outTradeNoList", outTradeNoList);
            }}));
            notifyTaskDao.insert(notifyTask);

            // 将拼团主订单状态设置为已完成
            int updateForGroupBuy = groupBuyOrderDao.update(Wrappers.<GroupBuyOrder>lambdaUpdate()
                    .setSql("status = 1")
                    .eq(GroupBuyOrder::getTeamId, groupBuyTeamEntity.getTeamId()));
            if (updateForGroupBuy != 1) {
                // 订单更新失败
                throw new AppException(ResponseCode.UPDATE_ZERO);
            }
            return NotifyTaskEntity
                    .builder()
                    .notifyMQ(notifyTask.getNotifyMq())
                    .notifyMQ(notifyTask.getNotifyMq())
                    .notifyType(notifyTask.getNotifyType())
                    .notifyUrl(notifyTask.getNotifyUrl())
                    .teamId(notifyTask.getTeamId())
                    .parameterJson(notifyTask.getParameterJson())
                    .build();
        }
        return null;
    }


    /**
     * 获取拼单有效时间
     *
     * @param activityId 活动ID
     * @return 拼单活动的有效时间（单位：秒）
     */
    @Override
    public int getActivityValidTime(Long activityId) {
        return groupBuyActivityDao.getActivityValidTime(activityId);
    }

    /**
     * 判断指定的源和渠道是否在黑名单中
     *
     * @param source  数据源标识
     * @param channel 渠道标识
     * @return true-在黑名单中，false-不在黑名单中
     */
    @Override
    public boolean isSCBlackList(String source, String channel) {
        return dccService.isScBlackList(source, channel);
    }

    /**
     * 查询回调任务列表
     *
     * @param teamId 团队ID
     * @return 回调任务实体列表
     */
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

    /**
     * 更新回调任务状态为成功
     *
     * @param teamId 团队ID，用于定位需要更新的回调任务
     * @return 更新记录数，表示成功更新的回调任务数量
     */
    @Override
    public int updateNotifyTaskStatusSuccess(String teamId) {
        return notifyTaskDao.update(Wrappers.<NotifyTask>lambdaUpdate()
                .setSql("notify_count = notify_count + 1,notify_status = 1")
                .eq(NotifyTask::getTeamId, teamId));
    }

    /**
     * 更新回调任务状态为失败
     *
     * @param teamId 团队ID，用于定位需要更新的回调任务
     * @return 更新记录数，表示成功更新的任务数量
     */
    @Override
    public int updateNotifyTaskStatusError(String teamId) {
        return notifyTaskDao.update(Wrappers.<NotifyTask>lambdaUpdate()
                .setSql("notify_count = notify_count + 1,notify_status = 3")
                .eq(NotifyTask::getTeamId, teamId));
    }

    /**
     * 更新通知任务状态重试次数
     *
     * @param teamId 团队ID，用于筛选需要更新的通知任务
     * @return 返回受影响的记录数
     */
    @Override
    public int updateNotifyTaskStatusRetry(String teamId) {
        return notifyTaskDao.update(Wrappers.<NotifyTask>lambdaUpdate()
                .setSql("notify_count = notify_count + 1,notify_status = 2")
                .eq(NotifyTask::getTeamId, teamId));
    }

        /**
     * 占用团队库存
     * @param teamStockKey 团队库存键名
     * @param recoveryTeamStockKey 恢复团队库存键名
     * @param targetCount 目标数量
     * @param validTime 有效时间
     * @return 占用成功返回true，否则返回false
     */
    @Override
    public boolean occupyTeamStock(String teamStockKey, String recoveryTeamStockKey, Integer targetCount, Integer validTime) {
        // 获取恢复团队库存数量
        Long recoveryTeamStockCount = redissonService.getAtomicLong(recoveryTeamStockKey);
        recoveryTeamStockCount = null == recoveryTeamStockCount ? 0 : recoveryTeamStockCount;

        // 增加占用计数
        long occupyCount = redissonService.incr(teamStockKey) + 1;

        // 检查是否超过库存限制
        if(occupyCount > recoveryTeamStockCount + targetCount){
            log.error("放入库存失败,拼团人数已满");
            redissonService.setAtomicLong(teamStockKey,targetCount);
            return false;
        }

        // 创建锁键并尝试获取锁
        String lockKey = teamStockKey + Constants.UNDERLINE + occupyCount;
        Boolean lock = redissonService.setNx(lockKey, validTime + 60, TimeUnit.MINUTES);

        // 记录锁获取失败日志
        if(!lock){
            log.error("放入库存失败,锁已存在:{}",lockKey);
        }

        return lock;
    }


    /**
     * 恢复团队库存
     *
     * @param recoveryTeamStockKey 团队库存恢复的Redis键名，用于标识需要恢复库存的具体团队商品
     */
    @Override
    public void recoveryTeamStock(String recoveryTeamStockKey) {
        // 如果恢复键名为空或空白字符串，则直接返回不执行任何操作
        if(StringUtils.isBlank(recoveryTeamStockKey)){
            return;
        }
        // 对指定的Redis键进行自增操作，实现库存数量的恢复
        redissonService.incr(recoveryTeamStockKey);
    }


    /**
     * 获取回调任务列表
     * 根据通知状态查询通知任务，状态为0(待通知)或2(通知失败)的任务
     *
     * @return 通知任务实体列表
     */
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
