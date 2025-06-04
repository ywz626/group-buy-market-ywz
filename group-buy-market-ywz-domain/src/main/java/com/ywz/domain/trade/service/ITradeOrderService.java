package com.ywz.domain.trade.service;

import com.ywz.domain.trade.model.entity.MarketPayOrderEntity;
import com.ywz.domain.trade.model.entity.PayActivityEntity;
import com.ywz.domain.trade.model.entity.PayDiscountEntity;
import com.ywz.domain.trade.model.entity.UserEntity;
import com.ywz.domain.trade.model.valobj.GroupBuyProgressVO;

/**
 * @author 于汶泽
 * @Description: 订单交易服务接口
 * @DateTime: 2025/6/3 19:06
 */
public interface ITradeOrderService {

    /**
     * 查询未支付的营销订单
     *
     * @param userId 用户ID
     * @param outTradeNo 外部交易号
     * @return 未支付的营销订单实体
     */
    MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo);

    /**
     * 查询拼团进度
     *
     * @param teamId 拼团ID
     * @return 拼团进度值对象
     */
    GroupBuyProgressVO queryGroupBuyProgress(String teamId);

    /**
     * 锁定营销优惠支付订单
     *
     * @param userEntity 用户实体
     * @param payActivityEntity 支付活动实体
     * @param payDiscountEntity 支付优惠实体
     * @return 锁定的营销支付订单实体
     */
    MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity, PayDiscountEntity payDiscountEntity) throws Exception;
}
