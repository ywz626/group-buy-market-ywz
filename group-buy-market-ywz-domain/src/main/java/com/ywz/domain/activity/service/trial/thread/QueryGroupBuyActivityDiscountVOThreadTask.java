package com.ywz.domain.activity.service.trial.thread;

import com.google.common.cache.Cache;
import com.ywz.domain.activity.adapter.repository.IActivityRepository;
import com.ywz.domain.activity.model.valobj.GroupBuyActivityDiscountVO;

import java.util.concurrent.Callable;

/**
 * @author 于汶泽
 * @Description: 查询拼团活动营销配置值对象的线程任务
 * @DateTime: 2025/6/1 18:14
 */
public class QueryGroupBuyActivityDiscountVOThreadTask implements Callable<GroupBuyActivityDiscountVO> {

    private final String source;
    private final String channel;
    private final IActivityRepository activityRepository;


    public QueryGroupBuyActivityDiscountVOThreadTask(String source, String channel, IActivityRepository activityRepository) {
        this.source = source;
        this.channel = channel;
        this.activityRepository = activityRepository;
    }


    @Override
    public GroupBuyActivityDiscountVO call() throws Exception {
        return activityRepository.queryGroupBuyActivityDiscountVO(source, channel);
    }
}
