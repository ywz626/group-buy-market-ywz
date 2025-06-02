package com.ywz.domain.tag.service;

/**
 * @author 于汶泽
 * @Description: 执行人群标签批次任务
 * @DateTime: 2025/6/2 13:08
 */
public interface ITagService {

    /**
     * 执行人群标签批次任务
     *
     * @param tagId   人群标签ID
     * @param batchId 批次ID
     */
    void execTagBatchJob(String tagId, String batchId);
}
