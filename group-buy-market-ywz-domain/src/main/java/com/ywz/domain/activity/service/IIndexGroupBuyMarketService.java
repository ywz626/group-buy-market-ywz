package com.ywz.domain.activity.service;

import com.ywz.domain.activity.model.entity.MarketProductEntity;
import com.ywz.domain.activity.model.entity.TrialBalanceEntity;
import com.ywz.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import com.ywz.domain.activity.model.valobj.TeamStatisticVO;

import java.util.List;

/**
 * @author 于汶泽
 * @Description: 首页营销服务接口
 * @DateTime: 2025/6/1 15:33
 */
public interface IIndexGroupBuyMarketService {


    TrialBalanceEntity indexMarketTrial(MarketProductEntity marketProductEntity) throws Exception;

    /**
     * 查询拼团团队统计信息
     *
     *
     * @return 返回拼团市场配置
     */
    TeamStatisticVO queryGroupTeamStatistic(Long activityId);

    List<UserGroupBuyOrderDetailEntity> getTeamList(Long activityId, String userId,int ownerCount, int randomCount) ;
}
