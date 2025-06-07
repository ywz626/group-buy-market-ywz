package com.ywz.domain.activity.service;

import com.ywz.domain.activity.adapter.repository.IActivityRepository;
import com.ywz.domain.activity.model.entity.MarketProductEntity;
import com.ywz.domain.activity.model.entity.TrialBalanceEntity;
import com.ywz.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import com.ywz.domain.activity.model.valobj.TeamStatisticVO;
import com.ywz.domain.activity.service.trial.factory.DefaultActivityStrategyFactory;
import com.ywz.types.design.framework.tree.StrategyHandler;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author 于汶泽
 * @Description: 首页营销服务
 * @DateTime: 2025/6/1 15:35
 */
@Component
public class IndexGroupBuyMarketServiceImpl implements IIndexGroupBuyMarketService {

    @Resource
    private DefaultActivityStrategyFactory defaultFactory;
    @Resource
    private IActivityRepository repository;


    @Override
    public TrialBalanceEntity indexMarketTrial(MarketProductEntity marketProductEntity) throws Exception {
        StrategyHandler<MarketProductEntity, DefaultActivityStrategyFactory.DynamicContext, TrialBalanceEntity> strategyHandler = defaultFactory.strategyHandler();

        return strategyHandler.apply(marketProductEntity, new DefaultActivityStrategyFactory.DynamicContext());
    }

    @Override
    public TeamStatisticVO queryGroupTeamStatistic(Long activityId) {
        return repository.queryGroupTeamStatistic(activityId);
    }

    @Override
    public List<UserGroupBuyOrderDetailEntity> getTeamList(Long activityId, String userId,int ownerCount, int randomCount) {
        // 先查询自己有没有订单
        List<UserGroupBuyOrderDetailEntity> list = new ArrayList<>();

        if(ownerCount != 0){
            List<UserGroupBuyOrderDetailEntity> myOrderDetailList = repository.getMyOrderDetailList(activityId, userId, ownerCount);
            if( myOrderDetailList != null && !myOrderDetailList.isEmpty()) {
                list.addAll(myOrderDetailList);
            }
        }

        if(randomCount != 0){
            List<UserGroupBuyOrderDetailEntity> randomOrderDetailList = repository.getRandomOrderDetailList(activityId, userId, randomCount);
            if(randomOrderDetailList != null && !randomOrderDetailList.isEmpty()) {
                list.addAll(randomOrderDetailList);
            }
        }
        return list;
    }
}
