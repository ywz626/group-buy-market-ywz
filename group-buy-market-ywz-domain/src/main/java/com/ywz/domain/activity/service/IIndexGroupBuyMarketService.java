package com.ywz.domain.activity.service;

import com.ywz.domain.activity.model.entity.MarketProductEntity;
import com.ywz.domain.activity.model.entity.TrialBalanceEntity;

/**
 * @author 于汶泽
 * @Description: 首页营销服务接口
 * @DateTime: 2025/6/1 15:33
 */
public interface IIndexGroupBuyMarketService {
    /**
     * 获取试算结果
     *
     * @param marketProductEntity 市场产品实体
     * @return 试算结果实体
     * @throws Exception 异常
     */
    TrialBalanceEntity getTrialBalance(MarketProductEntity marketProductEntity) throws Exception;
}
