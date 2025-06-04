package com.ywz.domain.tag.adapter.repository;

import com.ywz.domain.tag.model.entity.CrowdTagsJobEntity;
import org.redisson.api.RTransaction;
import org.springframework.stereotype.Repository;

/**
 * @author 于汶泽
 * @Description: Tag标签仓储接口
 * @DateTime: 2025/6/2 13:09
 */
public interface ITagRepository {
    /**
     * 查询人群标签批次任务
     *
     * @param tagId   标签ID
     * @param batchId 批次ID
     */
    CrowdTagsJobEntity queryCrowdTagsJobEntity(String tagId, String batchId);

    /**
     * 添加人群标签用户ID
     *
     * @param tagId  标签ID
     * @param userId 用户ID
     */
    RTransaction addCrowdTagsUserId(String tagId, String userId);

    /**
     * 更新人群标签统计量
     *
     * @param tagId 标签ID
     * @param count 统计数量
     */
    void updateCrowdTagsStatistics(String tagId, int count,RTransaction rTransaction);

}
