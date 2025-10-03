package com.ywz.domain.activity.service.trial.node;

import cn.bugstack.wrench.design.framework.tree.StrategyHandler;
import com.alibaba.fastjson.JSON;
import com.ywz.domain.activity.model.entity.MarketProductEntity;
import com.ywz.domain.activity.model.entity.TrialBalanceEntity;
import com.ywz.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.ywz.domain.activity.model.valobj.SkuVO;
import com.ywz.domain.activity.service.discount.IDiscountCalculateService;
import com.ywz.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import com.ywz.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import com.ywz.domain.activity.service.trial.thread.QueryGroupBuyActivityDiscountVOThreadTask;
import com.ywz.domain.activity.service.trial.thread.QuerySkuVOFromDBThreadTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author 于汶泽
 * @Description: 营销优惠节点
 * @DateTime: 2025/6/1 15:24
 */
@Service
@Slf4j
public class MarketNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {

    @Resource
    private ErrorNode errorNode;
    @Resource
    private TagNode tagNode;
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;
    @Resource
    private Map<String, IDiscountCalculateService> discountCalculateServiceMap;

    @Override
    public TrialBalanceEntity doApply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        log.info("拼团商品查询试算服务-MarketNode userId:{} requestParameter:{}", requestParameter.getUserId(), JSON.toJSONString(requestParameter));
        if(dynamicContext.getGroupBuyActivityDiscountVO() == null) {
            log.warn("拼团商品查询试算服务-MarketNode userId:{} 未查询到拼团活动优惠信息", requestParameter.getUserId());
            return router(requestParameter, dynamicContext);
        }
        GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount = dynamicContext.getGroupBuyActivityDiscountVO().getGroupBuyDiscount();
        if (groupBuyDiscount == null) {
            log.warn("拼团商品查询试算服务-MarketNode userId:{} 拼团活动优惠信息为空", requestParameter.getUserId());
            return router(requestParameter, dynamicContext);
        }
        IDiscountCalculateService iDiscountCalculateService = discountCalculateServiceMap.get(groupBuyDiscount.getMarketPlan());
        if (iDiscountCalculateService == null) {
            throw new IllegalArgumentException("不支持的营销优惠类型: " + groupBuyDiscount.getMarketPlan());
        }
        SkuVO skuVO = dynamicContext.getSkuVO();
        if( skuVO == null) {
            log.warn("拼团商品查询试算服务-MarketNode userId:{} 未查询到商品信息", requestParameter.getUserId());
            return router(requestParameter, dynamicContext);
        }
        // 执行营销优惠计算
        BigDecimal payPrice = iDiscountCalculateService.calculate(requestParameter.getUserId(), skuVO.getOriginalPrice(), groupBuyDiscount);
        dynamicContext.setDeductionPrice(skuVO.getOriginalPrice().subtract(payPrice));
        dynamicContext.setPayPrice(payPrice);
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        if(dynamicContext.getGroupBuyActivityDiscountVO() == null || dynamicContext.getGroupBuyActivityDiscountVO().getGroupBuyDiscount() == null || dynamicContext.getSkuVO() == null) {
            return errorNode;
        }
        return tagNode;
    }

    @Override
    protected void multiThread(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws ExecutionException, InterruptedException, TimeoutException {
        QueryGroupBuyActivityDiscountVOThreadTask queryGroupBuyActivityDiscountVOThreadTask = new QueryGroupBuyActivityDiscountVOThreadTask(requestParameter.getSource(), requestParameter.getChannel(),requestParameter.getGoodsId(), requestParameter.getActivityId(), repository);
        FutureTask<GroupBuyActivityDiscountVO> groupBuyActivityDiscountVOFutureTask = new FutureTask<>(queryGroupBuyActivityDiscountVOThreadTask);
        threadPoolExecutor.execute(groupBuyActivityDiscountVOFutureTask);

        QuerySkuVOFromDBThreadTask querySkuVOFromDBThreadTask = new QuerySkuVOFromDBThreadTask(requestParameter.getGoodsId(), repository);
        FutureTask<SkuVO> skuVOFutureTask = new FutureTask<>(querySkuVOFromDBThreadTask);
        threadPoolExecutor.execute(skuVOFutureTask);

        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = groupBuyActivityDiscountVOFutureTask.get(timeout, TimeUnit.SECONDS);
        dynamicContext.setGroupBuyActivityDiscountVO(groupBuyActivityDiscountVO);
        dynamicContext.setSkuVO(skuVOFutureTask.get(timeout, TimeUnit.SECONDS));
        log.info("拼团商品查询试算服务-MarketNode userId:{} 异步线程加载数据「GroupBuyActivityDiscountVO、SkuVO」完成", requestParameter.getUserId());
    }
}
