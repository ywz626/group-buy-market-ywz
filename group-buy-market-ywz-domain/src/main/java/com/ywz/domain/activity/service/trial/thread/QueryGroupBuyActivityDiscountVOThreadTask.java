package com.ywz.domain.activity.service.trial.thread;

import com.google.common.cache.Cache;
import com.ywz.domain.activity.adapter.repository.IActivityRepository;
import com.ywz.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.ywz.domain.activity.model.valobj.ScSkuActivityVO;

import java.util.concurrent.Callable;

/**
 * @author 于汶泽
 * @Description: 查询拼团活动营销配置值对象的线程任务
 * @DateTime: 2025/6/1 18:14
 */
public class QueryGroupBuyActivityDiscountVOThreadTask implements Callable<GroupBuyActivityDiscountVO> {

    private final String source;
    private final String channel;
    private final String goodsId;
    private final Long activityId;
    private final IActivityRepository activityRepository;


    public QueryGroupBuyActivityDiscountVOThreadTask(String source, String channel, String goodsId, Long activityId, IActivityRepository activityRepository) {
        this.source = source;
        this.channel = channel;
        this.goodsId = goodsId;
        this.activityId = activityId;
        this.activityRepository = activityRepository;
    }


    @Override
    public GroupBuyActivityDiscountVO call() throws Exception {
        Long scSkuActivityId = activityId;
        if(scSkuActivityId == null){
            ScSkuActivityVO scSkuActivityVO = activityRepository.queryScSkuActivityVO(source, channel, goodsId);
            if(scSkuActivityVO == null) {
                return null;
            }
            scSkuActivityId = scSkuActivityVO.getActivityId();
        }
        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = activityRepository.queryGroupBuyActivityDiscountVO(scSkuActivityId);
        return groupBuyActivityDiscountVO;
    }
}
