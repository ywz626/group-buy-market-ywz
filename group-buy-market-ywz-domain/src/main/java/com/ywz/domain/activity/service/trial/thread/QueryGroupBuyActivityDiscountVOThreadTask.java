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


    /**
     * 执行团购活动折扣信息查询任务
     *
     * @return GroupBuyActivityDiscountVO 团购活动折扣信息对象，如果未找到对应活动则返回null
     * @throws Exception 执行过程中可能抛出的异常
     */
    @Override
    public GroupBuyActivityDiscountVO call() throws Exception {
        Long scSkuActivityId = activityId;
        // 如果activityId为空，则通过source、channel、goodsId查询活动信息
        if(scSkuActivityId == null){
            ScSkuActivityVO scSkuActivityVO = activityRepository.queryScSkuActivityVO(source, channel, goodsId);
            if(scSkuActivityVO == null) {
                return null;
            }
            scSkuActivityId = scSkuActivityVO.getActivityId();
        }
        // 根据活动ID查询团购活动折扣信息
        return activityRepository.queryGroupBuyActivityDiscountVO(scSkuActivityId);
    }

}
