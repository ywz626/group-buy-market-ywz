package com.ywz.domain.tag.service;

import com.ywz.domain.tag.adapter.repository.ITagRepository;
import com.ywz.domain.tag.model.entity.CrowdTagsJobEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/6/2 13:29
 */
@Service
public class ITagServiceImpl implements ITagService {
    @Resource
    private ITagRepository repository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void execTagBatchJob(String tagId, String batchId) {
        // 1. 查询批次任务
        CrowdTagsJobEntity crowdTagsJobEntity = repository.queryCrowdTagsJobEntity(tagId, batchId);

        // 2. 采集用户数据 - 这部分需要采集用户的消费类数据，后续有用户发起拼单后再处理。

        // 3. 数据写入记录
        List<String> userIds = new ArrayList<String>(){{
            add("xiaofuge");
            add("ywz");
        }};

        // 4. 一般人群标签的处理在公司中，会有专门的数据数仓团队通过脚本方式写入到数据库，就不用这样一个个或者批次来写。
        for (String userId : userIds) {
            repository.addCrowdTagsUserId(tagId, userId);
        }

        // 5. 更新人群标签统计量
        repository.updateCrowdTagsStatistics(tagId, userIds.size());
    }
}
