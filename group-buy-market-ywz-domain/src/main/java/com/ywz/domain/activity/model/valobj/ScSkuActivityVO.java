package com.ywz.domain.activity.model.valobj;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @Author: ywz
 * @CreateTime: 2025-10-03
 * @Description: 商品来源渠道
 * @Version: 1.0
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ScSkuActivityVO {

    private String source;
    private String channel;
    private Long activityId;
    private String goodsId;
}
