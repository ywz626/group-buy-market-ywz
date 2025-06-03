package com.ywz.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.ywz.domain.activity.adapter.repository.IActivityRepository;
import com.ywz.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.ywz.domain.activity.model.valobj.SkuVO;
import com.ywz.infrastructure.dao.IGroupBuyActivityDao;
import com.ywz.infrastructure.dao.IGroupBuyDiscountDao;
import com.ywz.infrastructure.dao.IScSkuActivityDao;
import com.ywz.infrastructure.dao.ISkuDao;
import com.ywz.infrastructure.dao.po.GroupBuyActivityPO;
import com.ywz.infrastructure.dao.po.GroupBuyDiscountPO;
import com.ywz.infrastructure.dao.po.ScSkuActivity;
import com.ywz.infrastructure.dao.po.Sku;
import com.ywz.infrastructure.dcc.DCCService;
import com.ywz.infrastructure.redis.IRedisService;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.api.RBitSet;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Date;

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
    private ISkuDao skuDao;
    @Resource
    private IScSkuActivityDao scSkuActivityDao;
    @Resource
    private IRedisService redisService;
    @Resource
    private DCCService dccService;

    @Override
    public GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(String source, String channel) {
        ScSkuActivity scSkuActivity = scSkuActivityDao.selectOne(Wrappers.<ScSkuActivity>lambdaQuery()
                .eq(ScSkuActivity::getSource, source)
                .eq(ScSkuActivity::getChannel, channel)
                .orderByDesc(ScSkuActivity::getId));

        GroupBuyActivityPO groupBuyActivityRes = groupBuyActivityDao.selectOne(Wrappers.<GroupBuyActivityPO>lambdaQuery()
                .eq(GroupBuyActivityPO::getActivityId, scSkuActivity.getActivityId())
                .orderByDesc(GroupBuyActivityPO::getId));

        if(groupBuyActivityRes == null || groupBuyActivityRes.getStartTime().after(new Date()) || groupBuyActivityRes.getEndTime().before(new Date())){
            return null;
        }
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
        if(sku == null){
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
        if(bitSet == null){
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
}
