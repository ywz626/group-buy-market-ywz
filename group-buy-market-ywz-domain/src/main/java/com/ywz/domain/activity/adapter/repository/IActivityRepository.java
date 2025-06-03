package com.ywz.domain.activity.adapter.repository;


import com.ywz.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.ywz.domain.activity.model.valobj.SkuVO;

/**
 * @author 于汶泽
 * @description 活动仓储
 * @create 2025-6-01 17:43
 */
public interface IActivityRepository {

    /**
     * 查询拼团活动营销配置
     *
     * @param source 渠道
     * @param channel 来源
     * @return GroupBuyActivityDiscountVO
     */
    GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(String source, String channel,String goodsId);

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
}
