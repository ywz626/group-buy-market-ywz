package com.ywz.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author 于汶泽
 * @Description: 渠道来源活动商品关联PO
 * @DateTime: 2025/6/2 16:11
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScSkuActivity {
    /**
     * 自增ID
     */
    @TableId(value = "id",type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Integer id;

    /**
     * 渠道
     */
    private String source;

    /**
     * 来源
     */
    private String channel;

    /**
     * 活动ID
     */
    private Long activityId;

    /**
     * 商品ID
     */
    private String goodsId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
