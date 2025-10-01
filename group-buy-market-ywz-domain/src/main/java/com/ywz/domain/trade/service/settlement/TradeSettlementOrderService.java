package com.ywz.domain.trade.service.settlement;

import com.alibaba.fastjson.JSON;
import com.ywz.domain.trade.adapter.port.ITradePort;
import com.ywz.domain.trade.adapter.repository.ITradeRepository;
import com.ywz.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.ywz.domain.trade.model.entity.*;
import com.ywz.domain.trade.model.valobj.NotifyConfigVO;
import com.ywz.domain.trade.service.ITradeSettlementService;
import com.ywz.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import com.ywz.types.design.framework.link.model2.chain.BusinessLinkedList;
import com.ywz.types.enums.NotifyTaskHTTPEnumVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author 于汶泽
 * @Description: 订单支付结算服务
 * @DateTime: 2025/6/4 21:14
 */
@Service
@Slf4j
public class TradeSettlementOrderService implements ITradeSettlementService {


    @Resource
    private ITradeRepository repository;

    @Resource
    private ITradePort port;

    @Resource(name = "tradeSettlementRuleFilter")
    BusinessLinkedList<TradeSettlementRuleCommandEntity, TradeSettlementRuleFilterFactory.DynamicContext, TradeSettlementRuleFilterBackEntity> tradeSettlementRuleFilter;


    @Override
    public Map<String, Integer> execSettlementNotifyJob(NotifyTaskEntity notifyTask) throws Exception {
        return execSettlementNotifyJob(Collections.singletonList(notifyTask));
    }

    @Override
    public Map<String, Integer> execSettlementNotifyJob(String teamId) throws Exception {
        // 查询未执行任务

        List<NotifyTaskEntity> notifyTasklist = repository.queryNotifyTaskEntityListByTeamId(teamId);
        return execSettlementNotifyJob(notifyTasklist);
    }

    @Override
    public Map<String, Integer> execSettlementNotifyJob() throws Exception {
        // 查询未执行任务
        List<NotifyTaskEntity> notifyTasklist = repository.queryNotifyTaskEntityListByStatus();

        return execSettlementNotifyJob(notifyTasklist);

    }


    public Map<String, Integer> execSettlementNotifyJob(List<NotifyTaskEntity> notifyTaskEntityList) throws Exception {
        int successCount = 0;
        int failCount = 0;
        int retryCount = 0;
        for (NotifyTaskEntity notifyTaskEntity : notifyTaskEntityList) {
            String response = port.groupBuyNotify(notifyTaskEntity);
            if(response.equals(NotifyTaskHTTPEnumVO.SUCCESS.getCode())){
                int updateCount = repository.updateNotifyTaskStatusSuccess(notifyTaskEntity.getTeamId());
                if(updateCount == 1){
                    successCount++;
                }
            }else if(response.equals(NotifyTaskHTTPEnumVO.ERROR.getCode())){
                if(notifyTaskEntity.getNotifyCount() >= 5){
                    int updateCount = repository.updateNotifyTaskStatusError(notifyTaskEntity.getTeamId());
                    if(updateCount == 1){
                        failCount++;
                    }
                }
                else {
                    int updateCount = repository.updateNotifyTaskStatusRetry(notifyTaskEntity.getTeamId());
                    if(updateCount == 1){
                        retryCount++;
                    }
                }
            }
        }

        Map<String, Integer> resultMap = new HashMap<>();
        resultMap.put("waitCount", notifyTaskEntityList.size());
        resultMap.put("successCount", successCount);
        resultMap.put("errorCount", failCount);
        resultMap.put("retryCount", retryCount);

        return resultMap;
    }


    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

/**
 * 拼团支付订单结算
 * <p>
 * 该方法用于处理拼团场景下的支付成功后的结算逻辑。包括：
 * - 根据支付信息获取拼团规则并构建拼团队伍实体；
 * - 构建拼团结算聚合根对象；
 * - 调用仓储层更新拼团状态为已完成；
 * - 若存在通知任务，则异步执行拼团完成的通知回调。
 *
 * @param tradePaySuccessEntity 支付成功的交易实体，包含支付相关信息如外部交易号、用户ID、渠道等
 * @return TradePaySettlementEntity 交易支付结算结果实体，包含结算相关的基本信息
 * @throws Exception 结算过程中可能抛出的异常
 */
@Override
@Transactional(rollbackFor = Exception.class, timeout = 500)
public TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) throws Exception {
    // 应用拼团结算规则过滤器，获取拼团相关配置和状态信息
    TradeSettlementRuleFilterBackEntity tradeSettlementRuleFilterBackEntity = tradeSettlementRuleFilter.apply(TradeSettlementRuleCommandEntity.builder()
            .outTradeTime(tradePaySuccessEntity.getOutTradeTime())
            .channel(tradePaySuccessEntity.getChannel())
            .source(tradePaySuccessEntity.getSource())
            .userId(tradePaySuccessEntity.getUserId())
            .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
            .build(), new TradeSettlementRuleFilterFactory.DynamicContext());
    // 构建拼团队伍实体
    GroupBuyTeamEntity groupBuyTeamEntity = GroupBuyTeamEntity.builder()
            .status(tradeSettlementRuleFilterBackEntity.getStatus())
            .targetCount(tradeSettlementRuleFilterBackEntity.getTargetCount())
            .completeCount(tradeSettlementRuleFilterBackEntity.getCompleteCount())
            .validEndTime(tradeSettlementRuleFilterBackEntity.getValidEndTime())
            .activityId(tradeSettlementRuleFilterBackEntity.getActivityId())
            .validStartTime(tradeSettlementRuleFilterBackEntity.getValidStartTime())
            .teamId(tradeSettlementRuleFilterBackEntity.getTeamId())
            .notifyConfig(tradeSettlementRuleFilterBackEntity.getNotifyConfigVO())
            .build();
    // 构建拼团结算聚合根对象
    GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate = GroupBuyTeamSettlementAggregate.builder()
            .userEntity(UserEntity.builder().userId(tradePaySuccessEntity.getUserId()).build())
            .groupBuyTeamEntity(groupBuyTeamEntity)
            .tradePaySuccessEntity(tradePaySuccessEntity)
            .build();
    // 更新拼团组队状态为已完成，并获取是否需要执行通知任务
    NotifyTaskEntity doNotifyTask = repository.settlementMarketPayOrder(groupBuyTeamSettlementAggregate);
    // 如果存在通知任务，则提交到线程池异步执行回调通知
    if (doNotifyTask != null){
        threadPoolExecutor.execute(() -> {
            Map<String, Integer> notifyResultMap = null;
            try {
                // 执行拼团完成后的回调通知任务
                notifyResultMap = execSettlementNotifyJob(doNotifyTask);
                log.info("回调通知拼团完结 result:{}", JSON.toJSONString(notifyResultMap));
            } catch (Exception e) {
                log.error("回调通知拼团完结失败 result:{}", JSON.toJSONString(notifyResultMap), e);
                throw new RuntimeException(e);
            }
        });
    }
    // 返回拼团支付结算结果实体
    return TradePaySettlementEntity.builder()
            .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
            .userId(tradePaySuccessEntity.getUserId())
            .activityId(groupBuyTeamEntity.getActivityId())
            .teamId(groupBuyTeamEntity.getTeamId())
            .channel(tradePaySuccessEntity.getChannel())
            .source(tradePaySuccessEntity.getSource())
            .build();
}
}
