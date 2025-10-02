package com.ywz.domain.trade.service.lock;

import com.ywz.domain.trade.adapter.repository.ITradeRepository;
import com.ywz.domain.trade.model.aggregate.GroupBuyOrderAggregate;
import com.ywz.domain.trade.model.entity.*;
import com.ywz.domain.trade.model.valobj.GroupBuyProgressVO;
import com.ywz.domain.trade.service.ITradeLockOrderService;
import com.ywz.domain.trade.service.lock.factory.TradeLockRuleFilterFactory;
import com.ywz.types.design.framework.link.model2.chain.BusinessLinkedList;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;


/**
 * @author 于汶泽
 * @Description: 仓储服务实现类
 * @DateTime: 2025/6/3 19:07
 */
@Service
public class TradeLockLockOrderService implements ITradeLockOrderService {

    private final ITradeRepository tradeRepository;

    @Resource(name = "tradeLockRuleFilter")
    private BusinessLinkedList<TradeLockRuleCommandEntity, TradeLockRuleFilterFactory.DynamicContext, TradeLockRuleFilterBackEntity> tradeLockRuleFilter;

    public TradeLockLockOrderService(ITradeRepository tradeRepository) {
        this.tradeRepository = tradeRepository;
    }

    @Override
    public MarketPayOrderEntity queryNoPayMarketPayOrderByOutTradeNo(String userId, String outTradeNo) {
        return tradeRepository.queryMarketLockOrderByOutTradeNo(userId, outTradeNo);
    }

    /**
     * 查询指定团队的团购活动进度信息。
     *
     * @param teamId 团队唯一标识符，用于定位特定的团购活动实例
     * @return GroupBuyProgressVO 包含团购活动的实时进度数据，包含但不限于：
     * - 当前参团人数
     * - 团购目标人数
     * - 活动剩余时间
     * - 当前活动状态（进行中/已结束/已取消）
     * - 已达成的优惠条件等关键业务指标
     */
    @Override
    public GroupBuyProgressVO queryGroupBuyProgress(String teamId) {
        return tradeRepository.queryGroupBuyProgress(teamId);
    }


    /**
     * 锁定市场支付订单并应用交易规则处理
     *
     * @param userEntity        用户实体对象，包含用户基础信息
     * @param payActivityEntity 支付活动实体，包含活动配置参数
     * @param payDiscountEntity 支付优惠实体，包含折扣规则信息
     * @return MarketPayOrderEntity 返回锁定后的市场支付订单实体
     * @throws Exception 当规则校验失败或订单锁定异常时抛出
     */
    @Override
    public MarketPayOrderEntity lockMarketPayOrder(UserEntity userEntity, PayActivityEntity payActivityEntity, PayDiscountEntity payDiscountEntity) throws Exception {
        // 创建交易规则校验命令对象
        TradeLockRuleCommandEntity tradeRuleCommand = TradeLockRuleCommandEntity.builder()
                .activityId(payActivityEntity.getActivityId())
                .userId(userEntity.getUserId())
                .teamId(payActivityEntity.getTeamId())
                .build();

        // 执行交易规则过滤器链处理
        // 通过动态上下文执行规则引擎，获取用户参与订单统计信息
        TradeLockRuleFilterBackEntity tradeBackEntity = tradeLockRuleFilter.apply(tradeRuleCommand, new TradeLockRuleFilterFactory.DynamicContext());
        Integer userTakeOrderCount = tradeBackEntity.getUserTakeOrderCount();

        // 构建团购订单聚合根对象
        // 整合用户、活动、优惠及参与记录等核心业务数据
        GroupBuyOrderAggregate groupBuyOrderAggregate = GroupBuyOrderAggregate.builder()
                .userEntity(userEntity)
                .payActivityEntity(payActivityEntity)
                .payDiscountEntity(payDiscountEntity)
                .userTakeOrderCount(userTakeOrderCount)
                .build();

        // 持久化订单锁定操作
        // 通过仓储层执行市场支付订单的最终锁定
        try {
            return tradeRepository.lockMarketPayOrder(groupBuyOrderAggregate);
        } catch (Exception e) {
            tradeRepository.recoveryTeamStock(tradeBackEntity.getRecoveryTeamStockKey());
            throw e;
        }
    }
}
