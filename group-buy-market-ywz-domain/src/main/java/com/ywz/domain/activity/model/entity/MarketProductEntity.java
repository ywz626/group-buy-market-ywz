package com.ywz.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author ywz
 * @description 营销商品实体信息，通过这样一个信息获取商品优惠信息
 * @create 2024-12-14 13:46
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarketProductEntity {

    /** 活动ID */
    private Long activityId;
    /** 用户ID */
    private String userId;
    /** 商品ID */
    private String goodsId;
    /** 渠道 */
    private String source;
    /** 来源 */
    private String channel;

}
