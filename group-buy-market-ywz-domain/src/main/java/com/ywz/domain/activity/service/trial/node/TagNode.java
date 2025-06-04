package com.ywz.domain.activity.service.trial.node;

import com.ywz.domain.activity.model.entity.MarketProductEntity;
import com.ywz.domain.activity.model.entity.TrialBalanceEntity;
import com.ywz.domain.activity.model.valobj.GroupBuyActivityDiscountVO;
import com.ywz.domain.activity.model.valobj.TagScopeEnumVO;
import com.ywz.domain.activity.service.trial.AbstractGroupBuyMarketSupport;
import com.ywz.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import com.ywz.types.design.framework.tree.StrategyHandler;
import jodd.util.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.swing.text.html.HTML;

/**
 * @author 于汶泽
 * @Description: 过滤人群标签
 * @DateTime: 2025/6/2 20:20
 */
@Service
public class TagNode extends AbstractGroupBuyMarketSupport<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> {
    @Resource
    private EndNode endNode;

    @Override
    protected TrialBalanceEntity doApply(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {

        // 判断活动是否对该用户可见可参与
        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = dynamicContext.getGroupBuyActivityDiscountVO();
        String tagId = groupBuyActivityDiscountVO.getTagId();
        boolean isVisible = groupBuyActivityDiscountVO.isValid();
        boolean isEnable = groupBuyActivityDiscountVO.isEnabled();
        if(StringUtil.isBlank(tagId)){
            dynamicContext.setVisible(TagScopeEnumVO.VISIBLE.getAllow());
            dynamicContext.setEnable(TagScopeEnumVO.ENABLE.getAllow());
            return router(requestParameter, dynamicContext);
        }
        // 如果标签ID不为空，则需要判断用户是否在标签范围内
        boolean isWithin = repository.isTagCrowdRange(tagId,requestParameter.getUserId());
        dynamicContext.setVisible(isVisible || isWithin);
        dynamicContext.setEnable(isEnable || isWithin);
        return router(requestParameter, dynamicContext);
    }

    @Override
    public StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> get(MarketProductEntity requestParameter, DefaultActivityStrategyFactory.DynamicContext dynamicContext) throws Exception {
        return endNode;
    }
}
