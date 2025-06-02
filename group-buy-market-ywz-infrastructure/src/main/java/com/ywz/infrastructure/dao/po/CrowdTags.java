package com.ywz.infrastructure.dao.po;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author ywz
 * @description 人群标签
 * @create 2025-6-02 13:04
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "crowd_tags")
public class CrowdTags {

    /** 自增ID */
    @TableId(value = "id", type = com.baomidou.mybatisplus.annotation.IdType.AUTO)
    private Long id;
    /** 人群ID */
    private String tagId;
    /** 人群名称 */
    private String tagName;
    /** 人群描述 */
    private String tagDesc;
    /** 人群标签统计量 */
    private Integer statistics;
    /** 创建时间 */
    private Date createTime;
    /** 更新时间 */
    private Date updateTime;

}
