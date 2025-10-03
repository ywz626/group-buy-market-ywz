package com.ywz.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 拼团活动
 * @author ywz
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "group_buy_activity")
public class GroupBuyActivityPO {
    /**
     * 自增
     */
    @TableId(value = "id",type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long id;

    /**
     * 活动ID
     */
    @TableField(value = "activity_id")
    private Long activityId;

    /**
     * 活动名称
     */
    private String activityName;


    /**
     * 折扣ID
     */
    private String discountId;

    /**
     * 拼团方式（0自动成团、1达成目标拼团）
     */
    private Integer groupType;

    /**
     * 拼团次数限制
     */
    private Integer takeLimitCount;

    /**
     * 拼团目标
     */
    private Integer target;

    /**
     * 拼团时长（分钟）
     */
    private Integer validTime;

    /**
     * 活动状态（0创建、1生效、2过期、3废弃）
     */
    private Integer status;

    /**
     * 活动开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /**
     * 活动结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /**
     * 人群标签规则标识
     */
    private String tagId;

    /**
     * 人群标签规则范围（多选；1可见限制、2参与限制）
     */
    private String tagScope;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updateTime;

    public static String getCacheRedisKey(Long activityId){
        return "group_buy_activity_com.ywz.infrastructure.dao.po.GroupBuyActivityPO_" + activityId;
    }
}