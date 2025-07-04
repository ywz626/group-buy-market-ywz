package com.ywz.domain.trade.service.settlement;

import com.ywz.domain.trade.adapter.port.ITradePort;
import com.ywz.domain.trade.adapter.repository.ITradeRepository;
import com.ywz.domain.trade.model.aggregate.GroupBuyTeamSettlementAggregate;
import com.ywz.domain.trade.model.entity.*;
import com.ywz.domain.trade.service.ITradeSettlementService;
import com.ywz.domain.trade.service.settlement.factory.TradeSettlementRuleFilterFactory;
import com.ywz.types.design.framework.link.model2.chain.BusinessLinkedList;
import com.ywz.types.enums.NotifyTaskHTTPEnumVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    /**
     * 拼团支付订单结算
     *
     * @param tradePaySuccessEntity 支付成功实体
     * @return TradePaySettlementEntity 交易支付结算实体
     * @throws Exception 异常
     */
    @Override
    @Transactional(rollbackFor = Exception.class, timeout = 500)
    public TradePaySettlementEntity settlementMarketPayOrder(TradePaySuccessEntity tradePaySuccessEntity) throws Exception {

        TradeSettlementRuleFilterBackEntity tradeSettlementRuleFilterBackEntity = tradeSettlementRuleFilter.apply(TradeSettlementRuleCommandEntity.builder()
                .outTradeTime(tradePaySuccessEntity.getOutTradeTime())
                .channel(tradePaySuccessEntity.getChannel())
                .source(tradePaySuccessEntity.getSource())
                .userId(tradePaySuccessEntity.getUserId())
                .outTradeNo(tradePaySuccessEntity.getOutTradeNo())
                .build(), new TradeSettlementRuleFilterFactory.DynamicContext());

        GroupBuyTeamEntity groupBuyTeamEntity = GroupBuyTeamEntity.builder()
                .status(tradeSettlementRuleFilterBackEntity.getStatus())
                .targetCount(tradeSettlementRuleFilterBackEntity.getTargetCount())
                .completeCount(tradeSettlementRuleFilterBackEntity.getCompleteCount())
                .validEndTime(tradeSettlementRuleFilterBackEntity.getValidEndTime())
                .activityId(tradeSettlementRuleFilterBackEntity.getActivityId())
                .validStartTime(tradeSettlementRuleFilterBackEntity.getValidStartTime())
                .teamId(tradeSettlementRuleFilterBackEntity.getTeamId())
                .notifyUrl(tradeSettlementRuleFilterBackEntity.getNotifyUrl())
                .build();

        GroupBuyTeamSettlementAggregate groupBuyTeamSettlementAggregate = GroupBuyTeamSettlementAggregate.builder()
                .userEntity(UserEntity.builder().userId(tradePaySuccessEntity.getUserId()).build())
                .groupBuyTeamEntity(groupBuyTeamEntity)
                .tradePaySuccessEntity(tradePaySuccessEntity)
                .build();

        // 更新拼团组队状态为已完成
        boolean doNotifyTask = repository.settlementMarketPayOrder(groupBuyTeamSettlementAggregate);

        if (doNotifyTask){
            // 执行结算通知任务
            Map<String, Integer> notifyResultMap = execSettlementNotifyJob(groupBuyTeamEntity.getTeamId());
            log.info("回调通知拼团完结 result:{}", notifyResultMap);
        }
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
