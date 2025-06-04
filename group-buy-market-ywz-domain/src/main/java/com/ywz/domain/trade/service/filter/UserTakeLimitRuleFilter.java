package com.ywz.domain.trade.service.filter;

import com.ywz.domain.tag.adapter.repository.ITagRepository;
import com.ywz.domain.trade.adapter.repository.ITradeRepository;
import com.ywz.domain.trade.model.entity.GroupBuyActivityEntity;
import com.ywz.domain.trade.model.entity.TradeRuleCommandEntity;
import com.ywz.domain.trade.model.entity.TradeRuleFilterBackEntity;
import com.ywz.domain.trade.service.factory.TradeRuleFilterFactory;
import com.ywz.types.design.framework.link.model2.handler.ILogicLinkHandler;
import com.ywz.types.enums.ResponseCode;
import com.ywz.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author 于汶泽
 * @Description: 用户参与拼团次数限制规则过滤器
 * @DateTime: 2025/6/4 17:08
 */
@Slf4j
@Service
public class UserTakeLimitRuleFilter implements ILogicLinkHandler<TradeRuleCommandEntity, TradeRuleFilterFactory.DynamicContext, TradeRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;

    @Override
    public TradeRuleFilterBackEntity apply(TradeRuleCommandEntity requestParameter, TradeRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        GroupBuyActivityEntity groupBuyActivity = dynamicContext.getGroupBuyActivity();
        Integer takeLimitCount = groupBuyActivity.getTakeLimitCount();
        Integer orderCount = repository.queryOrerCount(requestParameter.getUserId(),requestParameter.getActivityId());
        if(takeLimitCount != null && orderCount >= takeLimitCount){
            log.info("用户参与次数校验，已达可参与上限 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0103);
        }
        return TradeRuleFilterBackEntity.builder()
                .userTakeOrderCount(orderCount)
                .build();
    }
}
