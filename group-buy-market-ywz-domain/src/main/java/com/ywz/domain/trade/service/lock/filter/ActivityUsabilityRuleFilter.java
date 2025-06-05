package com.ywz.domain.trade.service.lock.filter;

import com.ywz.domain.trade.adapter.repository.ITradeRepository;
import com.ywz.domain.trade.model.entity.GroupBuyActivityEntity;
import com.ywz.domain.trade.model.entity.TradeRuleCommandEntity;
import com.ywz.domain.trade.model.entity.TradeRuleFilterBackEntity;
import com.ywz.domain.trade.service.lock.factory.TradeRuleFilterFactory;
import com.ywz.types.design.framework.link.model2.handler.ILogicLinkHandler;
import com.ywz.types.enums.ActivityStatusEnumVO;
import com.ywz.types.enums.ResponseCode;
import com.ywz.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Date;

/**
 * @author 于汶泽
 * @Description: 查询活动的可用性规则过滤器
 * @DateTime: 2025/6/4 16:50
 */
@Slf4j
@Service
public class ActivityUsabilityRuleFilter implements ILogicLinkHandler<TradeRuleCommandEntity, TradeRuleFilterFactory.DynamicContext, TradeRuleFilterBackEntity>{

    @Resource
    private ITradeRepository repository;

    @Override
    public TradeRuleFilterBackEntity apply(TradeRuleCommandEntity requestParameter, TradeRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        // 活动的有效期、状态，以及个人参与拼团的次数
        // 查询活动状态
        GroupBuyActivityEntity groupBuyActivityEntity = repository.queryGroupBuyActivityEntity(requestParameter.getActivityId());
        if(!groupBuyActivityEntity.getStatus().equals(ActivityStatusEnumVO.EFFECTIVE)){
            log.info("活动的可用性校验，非生效状态 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0101);
        }

        // 查询活动的有效期
        Date currentTime = new Date();
        if(groupBuyActivityEntity.getStartTime().after(currentTime) || groupBuyActivityEntity.getEndTime().before(currentTime)){
            log.info("活动的可用性校验，活动不在有效期内 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0102);
        }

        dynamicContext.setGroupBuyActivity(groupBuyActivityEntity);
        return next(requestParameter, dynamicContext);
    }
}
