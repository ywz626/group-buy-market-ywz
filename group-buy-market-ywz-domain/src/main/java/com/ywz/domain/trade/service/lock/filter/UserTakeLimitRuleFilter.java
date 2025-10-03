package com.ywz.domain.trade.service.lock.filter;

import cn.bugstack.wrench.design.framework.link.model2.handler.ILogicHandler;
import com.ywz.domain.trade.adapter.repository.ITradeRepository;
import com.ywz.domain.trade.model.entity.GroupBuyActivityEntity;
import com.ywz.domain.trade.model.entity.TradeLockRuleCommandEntity;
import com.ywz.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import com.ywz.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
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
public class UserTakeLimitRuleFilter implements ILogicHandler<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> {

    @Resource
    private ITradeRepository repository;

        /**
     * 应用交易锁定规则过滤器，校验用户参与团购活动的次数是否超过限制
     *
     * @param requestParameter 交易锁定规则命令实体，包含用户ID和活动ID等请求参数
     * @param dynamicContext 动态上下文，包含团购活动信息
     * @return TradeLockRuleFilterBackEntity 包含用户参与订单数的返回实体
     * @throws Exception 当用户参与次数达到上限时抛出业务异常
     */
    @Override
    public TradeLockRuleFilterBackEntity apply(TradeLockRuleCommandEntity requestParameter, TradeLockRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        // 获取团购活动信息和用户参与次数限制
        GroupBuyActivityEntity groupBuyActivity = dynamicContext.getGroupBuyActivity();
        Integer takeLimitCount = groupBuyActivity.getTakeLimitCount();

        // 查询用户在当前活动中的订单数量
        Integer orderCount = repository.queryOrerCount(requestParameter.getUserId(),requestParameter.getActivityId());

        // 校验用户参与次数是否超过限制
        if(takeLimitCount != null && orderCount >= takeLimitCount){
            log.info("用户参与次数校验，已达可参与上限 activityId:{}", requestParameter.getActivityId());
            throw new AppException(ResponseCode.E0103);
        }
        dynamicContext.setUserTakeOrderCount(orderCount);

        return next(requestParameter, dynamicContext);
    }


}
