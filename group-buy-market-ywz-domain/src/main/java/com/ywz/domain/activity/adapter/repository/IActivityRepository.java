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
    GroupBuyActivityDiscountVO queryGroupBuyActivityDiscountVO(String source, String channel);

    /**
     * 根据商品ID查询商品信息
     *
     * @param goodsId 商品ID
     * @return SkuVO 商品信息
     */
    SkuVO querySkuByGoodsId(String goodsId);

}
