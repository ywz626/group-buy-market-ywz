package com.ywz.api.dto;

import lombok.Data;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 商品营销请求对象
 * @create 2025-02-02 12:19
 */
@Data
public class GoodsMarketRequestDTO {

    // 用户ID
    private String userId;
    // 渠道
    private String source;
    // 来源
    private String channel;
    // 商品ID
    private String goodsId;

}
