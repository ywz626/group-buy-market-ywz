package com.ywz.domain.activity.service.trial.thread;

import com.ywz.domain.activity.adapter.repository.IActivityRepository;
import com.ywz.domain.activity.model.valobj.SkuVO;

import javax.annotation.Resource;
import java.util.concurrent.Callable;

/**
 * @author 于汶泽
 * @Description: 查询商品信息的线程任务
 * @DateTime: 2025/6/1 18:15
 */
public class QuerySkuVOFromDBThreadTask implements Callable<SkuVO> {

    private final String goodsId;
    private final IActivityRepository activityRepository;

    public QuerySkuVOFromDBThreadTask(String goodsId, IActivityRepository activityRepository) {
        this.goodsId = goodsId;
        this.activityRepository = activityRepository;
    }

    @Override
    public SkuVO call() throws Exception {
        SkuVO skuVO = activityRepository.querySkuByGoodsId(goodsId);
        return skuVO;
    }
}
