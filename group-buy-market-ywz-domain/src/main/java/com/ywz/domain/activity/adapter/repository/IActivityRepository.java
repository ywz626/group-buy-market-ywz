package com.ywz.domain.activity.adapter.repository;


import com.ywz.domain.activity.model.entity.BuyOrderListEntity;
import com.ywz.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import com.ywz.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.ywz.domain.activity.model.valobj.ScSkuActivityVO;
import com.ywz.domain.activity.model.valobj.SkuVO;
import com.ywz.domain.activity.model.valobj.TeamStatisticVO;

import java.util.List;

/**
 * @author 于汶泽
 * @description 活动仓储
 * @create 2025-6-01 17:43
 */
public interface IActivityRepository {

    ScSkuActivityVO queryScSkuActivityVO(String source,String chanel,String goodsId);



    GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(Long activityId);

    /**
     * 根据商品ID查询商品信息
     *
     * @param goodsId 商品ID
     * @return SkuVO 商品信息
     */
    SkuVO querySkuByGoodsId(String goodsId);

    /**
     * 判断人群标签是否在拼团活动范围内
     * @param tagId 人群标签ID
     * @param userId 用户ID
     * @return true 如果在拼团活动范围内，false 如果不在范围内
     */
    boolean isTagCrowdRange(String tagId, String userId);

    boolean downgradeSwitch();

    boolean cutRange(String userId);

    TeamStatisticVO queryGroupTeamStatistic(Long activityId);

    /**
     * 查询用户拼团明细数据
     *
     * @param activityId 活动ID
     * @param userId 用户ID
     * @param count 个人数量
     * @return 用户拼团明细数据
     */
    List<UserGroupBuyOrderDetailEntity> getMyOrderDetailList(Long activityId, String userId,int count);


    List<UserGroupBuyOrderDetailEntity> getRandomOrderDetailList(Long activityId, String userId, int randomCount);

    List<BuyOrderListEntity> queryBuyOrderListByUserId(String userId);

}
