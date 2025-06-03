package com.ywz.test.Trade;


import com.alibaba.fastjson.JSON;
import com.ywz.api.dto.LockMarketPayOrderRequestDTO;
import com.ywz.api.dto.LockMarketPayOrderResponseDTO;
import com.ywz.api.response.Response;
import com.ywz.domain.activity.service.IIndexGroupBuyMarketService;
import com.ywz.domain.trade.service.ITradeOrderService;
import com.ywz.trigger.http.MarketTradeController;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 交易订单服务测试
 * @create 2025-01-11 11:52
 */
@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class ITradeOrderServiceTest {

    @Resource
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;

    @Resource
    private ITradeOrderService tradeOrderService;
    @Resource
    private MarketTradeController controller;

    @Test
    public void test_lockMarketPayOrder() throws Exception {
        // 入参信息
        Long activityId = 100123L;
        String userId = "xiaofuge";
        String goodsId = "9890001";
        String source = "s01";
        String channel = "c01";
        String teamId = "44225112"; // 团队ID，拼团时使用
        String outTradeNo = RandomStringUtils.randomAlphanumeric(12);

        LockMarketPayOrderRequestDTO lockMarketPayOrderRequestDTO = new LockMarketPayOrderRequestDTO();
        lockMarketPayOrderRequestDTO.setUserId(userId);
        lockMarketPayOrderRequestDTO.setTeamId(teamId);
        lockMarketPayOrderRequestDTO.setActivityId(activityId);
        lockMarketPayOrderRequestDTO.setGoodsId(goodsId);
        lockMarketPayOrderRequestDTO.setSource(source);
        lockMarketPayOrderRequestDTO.setChannel(channel);
        lockMarketPayOrderRequestDTO.setOutTradeNo(outTradeNo);

        Response<LockMarketPayOrderResponseDTO> lockMarketPayOrderResponseDTOResponse = controller.lockMarketPayOrder(lockMarketPayOrderRequestDTO);
//        // 1. 获取试算优惠，有【activityId】优先使用
//        TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(MarketProductEntity.builder()
//                .userId(userId)
//                .source(source)
//                .channel(channel)
//                .goodsId(goodsId)
//                .activityId(activityId)
//                .build());
//
//        GroupBuyActivityDiscountVO groupBuyActivityDiscountVO = trialBalanceEntity.getGroupBuyActivityDiscountVO();
//
//        // 查询 outTradeNo 是否已经存在交易记录
//        MarketPayOrderEntity marketPayOrderEntityOld = tradeOrderService.queryNoPayMarketPayOrderByOutTradeNo(userId, outTradeNo);
//        if (null != marketPayOrderEntityOld) {
//            log.info("测试结果(Old):{}", JSON.toJSONString(marketPayOrderEntityOld));
//            return;
//        }
//
//        // 2. 锁定，营销预支付订单；商品下单前，预购锁定。
//        MarketPayOrderEntity marketPayOrderEntityNew = tradeOrderService.lockMarketPayOrder(
//                UserEntity.builder().userId(userId).build(),
//                PayActivityEntity.builder()
//                        .teamId(teamId)
//                        .activityId(groupBuyActivityDiscountVO.getActivityId())
//                        .activityName(groupBuyActivityDiscountVO.getActivityName())
//                        .startTime(groupBuyActivityDiscountVO.getStartTime())
//                        .endTime(groupBuyActivityDiscountVO.getEndTime())
//                        .targetCount(groupBuyActivityDiscountVO.getTarget())
//                        .build(),
//                PayDiscountEntity.builder()
//                        .source(source)
//                        .channel(channel)
//                        .goodsId(goodsId)
//                        .goodsName(trialBalanceEntity.getGoodsName())
//                        .originalPrice(trialBalanceEntity.getOriginalPrice())
//                        .deductionPrice(trialBalanceEntity.getDeductionPrice())
//                        .outTradeNo(outTradeNo)
//                        .build());

        log.info("测试结果(New):{}",JSON.toJSONString(lockMarketPayOrderResponseDTOResponse));
    }

}
