package com.ywz.domain.tag.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author ywz
 * @description 批次任务对象
 * @create 2025-6-02-13-07
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CrowdTagsJobEntity {

    /** 标签类型（参与量、消费金额） */
    private Integer tagType;
    /** 标签规则（限定类型 N次） */
    private String tagRule;
    /** 统计数据，开始时间 */
    private Date statStartTime;
    /** 统计数据，结束时间 */
    private Date statEndTime;

}
