package com.ywz.domain.trade.service.lock.filter;

import com.ywz.domain.trade.adapter.repository.ITradeRepository;
import com.ywz.domain.trade.model.entity.GroupBuyActivityEntity;
import com.ywz.domain.trade.model.entity.TradeLockRuleCommandEntity;
import com.ywz.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import com.ywz.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
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
public class ActivityUsabilityRuleFilter implements ILogicLinkHandler<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity>{

    @Resource
    private ITradeRepository repository;

    /**
     * 应用交易锁定规则过滤器，校验团购活动的有效性
     *
     * @param requestParameter 交易锁定规则命令实体，包含活动ID等请求参数
     * @param dynamicContext 动态上下文，用于在过滤器链中传递动态数据
     * @return TradeLockRuleFilterBackEntity 过滤器处理结果实体
     * @throws Exception 处理过程中可能抛出的异常
     */
    @Override
    public TradeLockRuleFilterBackEntity apply(TradeLockRuleCommandEntity requestParameter, TradeLockRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        // 校验活动的可用性：包括活动状态和有效期检查
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
