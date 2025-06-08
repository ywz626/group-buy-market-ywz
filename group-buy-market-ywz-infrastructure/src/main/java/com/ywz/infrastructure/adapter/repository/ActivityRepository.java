package com.ywz.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ywz.domain.activity.adapter.repository.IActivityRepository;
import com.ywz.domain.activity.model.entity.BuyOrderListEntity;
import com.ywz.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import com.ywz.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.ywz.domain.activity.model.valobj.SkuVO;
import com.ywz.domain.activity.model.valobj.TeamStatisticVO;
import com.ywz.infrastructure.dao.*;
import com.ywz.infrastructure.dao.po.*;
import com.ywz.infrastructure.dao.po.base.Page;
import com.ywz.infrastructure.dcc.DCCService;
import com.ywz.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.redisson.api.RBitSet;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 于汶泽
 * @Description: 活动仓储实现
 * @DateTime: 2025/6/1 17:43
 */
@Repository
@Slf4j
public class ActivityRepository implements IActivityRepository {

    @Resource
    private IGroupBuyActivityDao groupBuyActivityDao;
    @Resource
    private IGroupBuyDiscountDao groupBuyDiscountDao;
    @Resource
    private IGroupBuyOrderDao groupBuyOrderDao;
    @Resource
    private IGroupBuyOrderListDao groupBuyOrderListDao;
    @Resource
    private ISkuDao skuDao;
    @Resource
    private IScSkuActivityDao scSkuActivityDao;
    @Resource
    private IRedisService redisService;
    @Resource
    private DCCService dccService;

    @Override
    public GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(String source, String channel, String goodsId) {
        ScSkuActivity scSkuActivity = scSkuActivityDao.selectOne(Wrappers.<ScSkuActivity>lambdaQuery()
                .eq(ScSkuActivity::getSource, source)
                .eq(ScSkuActivity::getChannel, channel)
                .eq(ScSkuActivity::getGoodsId, goodsId)
                .orderByDesc(ScSkuActivity::getId));

        if (scSkuActivity == null) {
            return null;
        }
        GroupBuyActivityPO groupBuyActivityRes = groupBuyActivityDao.selectOne(Wrappers.<GroupBuyActivityPO>lambdaQuery()
                .eq(GroupBuyActivityPO::getActivityId, scSkuActivity.getActivityId())
                .orderByDesc(GroupBuyActivityPO::getId));

        // 判断时间是否在活动范围内
//        if (groupBuyActivityRes == null || groupBuyActivityRes.getStartTime().after(new Date()) || groupBuyActivityRes.getEndTime().before(new Date())) {
//            return null;
//        }
        String discountId = groupBuyActivityRes.getDiscountId();

        GroupBuyDiscountPO groupBuyDiscountRes = groupBuyDiscountDao.selectOne(Wrappers.<GroupBuyDiscountPO>lambdaQuery()
                .eq(GroupBuyDiscountPO::getDiscountId, discountId));
        GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount = GroupBuyActivityDiscountVO.GroupBuyDiscount.builder()
                .discountName(groupBuyDiscountRes.getDiscountName())
                .discountDesc(groupBuyDiscountRes.getDiscountDesc())
                .discountType(groupBuyDiscountRes.getDiscountType())
                .marketPlan(groupBuyDiscountRes.getMarketPlan())
                .marketExpr(groupBuyDiscountRes.getMarketExpr())
                .tagId(groupBuyDiscountRes.getTagId())
                .build();

        return GroupBuyActivityDiscountVO.builder()
                .activityId(groupBuyActivityRes.getActivityId())
                .activityName(groupBuyActivityRes.getActivityName())
                .source(scSkuActivity.getSource())
                .channel(scSkuActivity.getChannel())
                .goodsId(scSkuActivity.getGoodsId())
                .groupBuyDiscount(groupBuyDiscount)
                .groupType(groupBuyActivityRes.getGroupType())
                .takeLimitCount(groupBuyActivityRes.getTakeLimitCount())
                .target(groupBuyActivityRes.getTarget())
                .validTime(groupBuyActivityRes.getValidTime())
                .status(groupBuyActivityRes.getStatus())
                .startTime(groupBuyActivityRes.getStartTime())
                .endTime(groupBuyActivityRes.getEndTime())
                .tagId(groupBuyActivityRes.getTagId())
                .tagScope(groupBuyActivityRes.getTagScope())
                .build();
    }

    @Override
    public SkuVO querySkuByGoodsId(String goodsId) {
        Sku sku = skuDao.selectOne(Wrappers.<Sku>lambdaQuery()
                .eq(Sku::getGoodsId, goodsId));
        if (sku == null) {
            return null;
        }
        return SkuVO.builder()
                .goodsId(sku.getGoodsId())
                .goodsName(sku.getGoodsName())
                .originalPrice(sku.getOriginalPrice())
                .build();
    }

    @Override
    public boolean isTagCrowdRange(String tagId, String userId) {
        RBitSet bitSet = redisService.getBitSet(tagId);
        if (bitSet == null) {
            // 如果BitSet不存在，说明该标签没有用户，直接返回true
            return true;
        }
        // 检查用户ID是否在BitSet中
        return bitSet.get(redisService.getIndexFromUserId(userId));
    }

    @Override
    public boolean downgradeSwitch() {
        return dccService.isDowngradeSwitch();
    }

    @Override
    public boolean cutRange(String userId) {
        return dccService.isCutRange(userId);
    }

    @Override
    public TeamStatisticVO queryGroupTeamStatistic(Long activityId) {

        Integer allTeamCount = groupBuyOrderDao.getAllTeamCount(activityId);
        Integer allTeamCompleteCount = groupBuyOrderDao.getAllTeamCompleteCount(activityId);
        Integer allTeamUserCount = groupBuyOrderDao.getAllTeamUserCount(activityId);

        return TeamStatisticVO.builder()
                .allTeamCount(allTeamCount)
                .allTeamCompleteCount(allTeamCompleteCount)
                .allTeamUserCount(allTeamUserCount)
                .build();
    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> getMyOrderDetailList(Long activityId, String userId, int count) {
        // 查询自己参加的拼团订单详情列表
        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListDao.getOrderDetailList(activityId, userId, count);
        if (groupBuyOrderLists == null || groupBuyOrderLists.isEmpty()) {
            return null;
        }
        // 获取自己参加的拼团订单ID列表 一般是一个
        //  根据自己拼团订单id列表 查询拼团订单信息 ;这里不直接使用activityId从groupBuyOrderDao查询是因为groupBuyOrder中没有userId字段
        // 整合数据
        List<UserGroupBuyOrderDetailEntity> userGroupBuyOrderDetailEntities = getUserGroupBuyOrderDetailEntities(groupBuyOrderLists);
        if(userGroupBuyOrderDetailEntities == null || userGroupBuyOrderDetailEntities.isEmpty()) {
            return null;
        }
        return userGroupBuyOrderDetailEntities;
    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> getRandomOrderDetailList(Long activityId, String userId, int randomCount) {
        // 查询teamIds
        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListDao.getRandomOrderDetailList(activityId, userId, randomCount * 2);

        if (groupBuyOrderLists == null || groupBuyOrderLists.isEmpty()) {
            return null;
        }
        if (groupBuyOrderLists.size() > randomCount) {
            Collections.shuffle(groupBuyOrderLists);
            groupBuyOrderLists.subList(0, randomCount);
        }

        // 获取teamId列表
        // 根据teamId查询拼团订单信息
        List<UserGroupBuyOrderDetailEntity> userGroupBuyOrderDetailEntities = getUserGroupBuyOrderDetailEntities(groupBuyOrderLists);
        if (userGroupBuyOrderDetailEntities == null || userGroupBuyOrderDetailEntities.isEmpty()) {
            return null;
        }
        return userGroupBuyOrderDetailEntities;
    }

    @Override
    public List<BuyOrderListEntity> queryBuyOrderListByUserId(String userId) {
        List<GroupBuyOrderList> groupBuyOrderLists = groupBuyOrderListDao.selectList(Wrappers.<GroupBuyOrderList>lambdaQuery()
                .eq(GroupBuyOrderList::getUserId, userId))
                .stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<BuyOrderListEntity> responseData = new ArrayList<>();
        for (GroupBuyOrderList groupBuyOrderList : groupBuyOrderLists) {
            String goodsId = groupBuyOrderList.getGoodsId();
            String goodsName = skuDao.selectOne(Wrappers.<Sku>lambdaQuery()
                    .eq(Sku::getGoodsId, goodsId)).getGoodsName();
            log.info("查询的日期：{}", groupBuyOrderList.getCreateTime());
            BuyOrderListEntity buyOrderListEntity = BuyOrderListEntity.builder()
                    .goodName(goodsName)
                    .tradeCreateTime(groupBuyOrderList.getCreateTime())
                    .outTradeNo(groupBuyOrderList.getOutTradeNo())
                    .build();
            Integer status = groupBuyOrderList.getStatus();
            if(status == 0){
                buyOrderListEntity.setStatus(0);
                buyOrderListEntity.setUserNo(null);
            }if(status == 1){
                GroupBuyOrder groupBuyOrder = groupBuyOrderDao.selectOne(Wrappers.<GroupBuyOrder>lambdaQuery()
                        .eq(GroupBuyOrder::getTeamId, groupBuyOrderList.getTeamId())
                        .eq(GroupBuyOrder::getActivityId, groupBuyOrderList.getActivityId()));
                if(groupBuyOrder.getStatus() == 0){
                    buyOrderListEntity.setStatus(1);
                    buyOrderListEntity.setUserNo(groupBuyOrder.getTargetCount()- groupBuyOrder.getLockCount());
                }else {
                    buyOrderListEntity.setStatus(groupBuyOrder.getStatus());
                    buyOrderListEntity.setUserNo(null);
                }
            }
            responseData.add(buyOrderListEntity);
        }
        return responseData;
    }

    /**
     * 提取公共方法：获取用户拼团订单详情实体列表
     * @param groupBuyOrderLists
     * @return
     */
    private  List<UserGroupBuyOrderDetailEntity> getUserGroupBuyOrderDetailEntities(List<GroupBuyOrderList> groupBuyOrderLists) {
        Set<String> teamIds = groupBuyOrderLists.stream()
                .map(GroupBuyOrderList::getTeamId)
                .filter(teamId -> teamId != null && !teamId.isEmpty())
                .collect(Collectors.toSet());
        if( teamIds.isEmpty()) {
            return null;
        }
        log.info("查询的teamIds：{}", teamIds);
        List<GroupBuyOrder> groupBuyOrderList = groupBuyOrderDao.selectList(Wrappers.<GroupBuyOrder>lambdaQuery()
                .apply("target_count > lock_count")
                .eq(GroupBuyOrder::getStatus, 0)
                .in(GroupBuyOrder::getTeamId, teamIds));
        if( groupBuyOrderList == null || groupBuyOrderList.isEmpty()) {
            return null;
        }
        log.info("查询的拼团订单信息：{}", groupBuyOrderList);
        Map<String, GroupBuyOrder> groupBuyOrders = groupBuyOrderList.stream()
                .collect(Collectors.toMap(GroupBuyOrder::getTeamId, groupBuyOrder -> groupBuyOrder));
        log.info("查询的拼团订单信息Map：{}", groupBuyOrders);
        List<UserGroupBuyOrderDetailEntity> unionList = new ArrayList<>();
        for (GroupBuyOrderList orderDetail : groupBuyOrderLists) {
            String teamId = orderDetail.getTeamId();
            if (teamId == null || teamId.isEmpty()) {
                continue; // 跳过无效的teamId
            }
            log.info("查询的拼团订单信息teamId：{}", teamId);
            GroupBuyOrder groupBuyOrder = groupBuyOrders.get(teamId);
            UserGroupBuyOrderDetailEntity build = UserGroupBuyOrderDetailEntity.builder()
                    .completeCount(groupBuyOrder.getCompleteCount())
                    .lockCount(groupBuyOrder.getLockCount())
                    .targetCount(groupBuyOrder.getTargetCount())
                    .validStartTime(groupBuyOrder.getValidStartTime())
                    .validEndTime(groupBuyOrder.getValidEndTime())
                    .activityId(groupBuyOrder.getActivityId())
                    .teamId(groupBuyOrder.getTeamId())
                    .outTradeNo(orderDetail.getOutTradeNo())
                    .userId(orderDetail.getUserId())
                    .build();
            unionList.add(build);
        }
        return unionList;
    }

}
