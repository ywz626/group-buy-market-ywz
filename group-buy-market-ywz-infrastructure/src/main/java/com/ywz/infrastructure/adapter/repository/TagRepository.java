package com.ywz.infrastructure.adapter.repository;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.QueryChainWrapper;
import com.ywz.domain.tag.adapter.repository.ITagRepository;
import com.ywz.domain.tag.model.entity.CrowdTagsJobEntity;
import com.ywz.infrastructure.dao.ICrowdTagsDao;
import com.ywz.infrastructure.dao.ICrowdTagsDetailDao;
import com.ywz.infrastructure.dao.ICrowdTagsJobDao;
import com.ywz.infrastructure.dao.po.CrowdTags;
import com.ywz.infrastructure.dao.po.CrowdTagsDetail;
import com.ywz.infrastructure.dao.po.CrowdTagsJob;
import com.ywz.infrastructure.redis.IRedisService;
import org.redisson.api.RBitSet;
import org.redisson.api.RTransaction;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/6/2 13:10
 */
@Repository
public class TagRepository implements ITagRepository {


    @Resource
    private ICrowdTagsDao crowdTagsDao;
    @Resource
    private ICrowdTagsDetailDao crowdTagsDetailDao;
    @Resource
    private ICrowdTagsJobDao crowdTagsJobDao;
    @Resource
    private IRedisService redisService;


    @Override
    public CrowdTagsJobEntity queryCrowdTagsJobEntity(String tagId, String batchId) {
        CrowdTagsJob crowdTagsJob = crowdTagsJobDao.selectOne(new LambdaQueryWrapper<CrowdTagsJob>().eq(CrowdTagsJob::getTagId, tagId)
                .eq(CrowdTagsJob::getBatchId, batchId));

        if (crowdTagsJob == null) {
            return null;
        }
        return CrowdTagsJobEntity.builder()
                .tagType(crowdTagsJob.getTagType())
                .tagRule(crowdTagsJob.getTagRule())
                .statStartTime(crowdTagsJob.getStatStartTime())
                .statEndTime(crowdTagsJob.getStatEndTime())
                .build();
    }

    @Override
    public RTransaction addCrowdTagsUserId(String tagId, String userId) {
        CrowdTagsDetail crowdTagsDetail = new CrowdTagsDetail();
        crowdTagsDetail.setUserId(userId);
        crowdTagsDetail.setTagId(tagId);
        crowdTagsDetailDao.insert(crowdTagsDetail);
        // 获取BitSet
        RBitSet bitSet = redisService.getBitSet(tagId);
        RTransaction transaction = redisService.createTransaction();
        try{
            // 尝试添加用户ID到人群标签的BitSet中
            bitSet.setAsync(redisService.getIndexFromUserId(userId), true);
        } catch (DuplicateKeyException e) {
            // 如果用户ID已存在，则回滚事务
            transaction.rollback();
            // 用户ID已存在，直接返回
            return null;
        }
        // 返回事务对象，表示操作成功
        return transaction;
    }

    @Override
    public void updateCrowdTagsStatistics(String tagId, int count,RTransaction transaction) {
        try {
            crowdTagsDao.update(new LambdaUpdateWrapper<CrowdTags>()
                    .setSql("statistics = statistics + {0}", count)
                    .eq(CrowdTags::getTagId, tagId));
        } catch (Exception e) {
            transaction.rollback();
            throw new RuntimeException(e);
        }
        transaction.commit();
    }
}
