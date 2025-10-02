package com.ywz.domain.trade.service.lock.filter;


import com.ywz.domain.trade.adapter.repository.ITradeRepository;
import com.ywz.domain.trade.model.entity.GroupBuyActivityEntity;
import com.ywz.domain.trade.model.entity.TradeLockRuleCommandEntity;
import com.ywz.domain.trade.model.entity.TradeLockRuleFilterBackEntity;
import com.ywz.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import com.ywz.types.design.framework.link.model2.handler.ILogicLinkHandler;
import com.ywz.types.enums.ResponseCode;
import com.ywz.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @Author: ywz
 * @CreateTime: 2025-10-02
 * @Description: 库存责任链过滤
 * @Version: 1.0
 */
@Service
@Slf4j
public class TeamStockOccupyRuleFilter implements ILogicLinkHandler<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext , TradeLockRuleFilterBackEntity> {


    @Resource
    private ITradeRepository repository;

    @Override
    public TradeLockRuleFilterBackEntity apply(TradeLockRuleCommandEntity requestParameter, TradeLockRuleFilterFactory.DynamicContext dynamicContext) throws Exception {
        String teamId = requestParameter.getTeamId();
        if(StringUtils.isBlank(teamId)){
            log.info("第一次创建拼团,拼团id为空,直接放行");
            return TradeLockRuleFilterBackEntity.builder()
                    .userTakeOrderCount(dynamicContext.getUserTakeOrderCount())
                    .build();
        }
        GroupBuyActivityEntity groupBuyActivity = dynamicContext.getGroupBuyActivity();
        Integer targetCount = groupBuyActivity.getTarget();
        Integer validTime = groupBuyActivity.getValidTime();
        String teamStockKey = dynamicContext.generateTeamStockKey(teamId);
        String recoveryTeamStockKey = dynamicContext.generateRecoveryTeamStockKey(teamId);
        boolean isOccupyTeamStock = repository.occupyTeamStock(teamStockKey,recoveryTeamStockKey,targetCount,validTime);

        if(!isOccupyTeamStock){
            log.error("库存占用失败 teamId:{}", teamId);
            throw new AppException(ResponseCode.E0008);
        }

        return TradeLockRuleFilterBackEntity.builder()
                .userTakeOrderCount(dynamicContext.getUserTakeOrderCount())
                .recoveryTeamStockKey(recoveryTeamStockKey)
                .build();
    }
}
