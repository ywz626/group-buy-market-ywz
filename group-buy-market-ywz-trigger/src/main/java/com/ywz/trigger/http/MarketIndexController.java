package com.ywz.trigger.http;

import com.alibaba.fastjson.JSON;
import com.ywz.api.IMarketIndexService;
import com.ywz.api.dto.BuyOrderListRequestDTO;
import com.ywz.api.dto.BuyOrderListResponseDTO;
import com.ywz.api.dto.GoodsMarketRequestDTO;
import com.ywz.api.dto.GoodsMarketResponseDTO;
import com.ywz.api.response.Response;
import com.ywz.domain.activity.model.entity.BuyOrderListEntity;
import com.ywz.domain.activity.model.entity.MarketProductEntity;
import com.ywz.domain.activity.model.entity.TrialBalanceEntity;
import com.ywz.domain.activity.model.entity.UserGroupBuyOrderDetailEntity;
import com.ywz.domain.activity.model.valobj.TeamStatisticVO;
import com.ywz.domain.activity.service.IIndexGroupBuyMarketService;
import com.ywz.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author 于汶泽
 * @Description: 首页信息查询
 * @DateTime: 2025/6/6 17:09
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/index/")
public class MarketIndexController implements IMarketIndexService {
    @Resource
    private IIndexGroupBuyMarketService indexGroupBuyMarketService;

    @Override
    @PostMapping("query_group_buy_market_config")
    public Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfig(@RequestBody GoodsMarketRequestDTO goodsMarketRequestDTO) {
        try {
            String goodsId = goodsMarketRequestDTO.getGoodsId();
            String userId = goodsMarketRequestDTO.getUserId();
            String source = goodsMarketRequestDTO.getSource();
            String channel = goodsMarketRequestDTO.getChannel();
            if (StringUtils.isBlank(goodsId) || StringUtils.isBlank(userId) || StringUtils.isBlank(source) || StringUtils.isBlank(channel)) {
                return Response.<GoodsMarketResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }
            // 营销优惠试算
            TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(MarketProductEntity.builder()
                    .userId(userId)
                    .goodsId(goodsId)
                    .source(source)
                    .channel(channel)
                    .build());

            // 获取拼团组队统计信息
            Long activityId = trialBalanceEntity.getGroupBuyActivityDiscountVO().getActivityId();
            TeamStatisticVO teamStatisticVO = indexGroupBuyMarketService.queryGroupTeamStatistic(activityId);

            // 获取队伍信息
            List<UserGroupBuyOrderDetailEntity> team = indexGroupBuyMarketService.getTeamList(activityId, userId, 1, 2);

            List<GoodsMarketResponseDTO.Team> teams = new ArrayList<>();
            if (null != team && !team.isEmpty()) {
                for (UserGroupBuyOrderDetailEntity userGroupBuyOrderDetailEntity : team) {
                    GoodsMarketResponseDTO.Team goodsTeam = GoodsMarketResponseDTO.Team.builder()
                            .userId(userGroupBuyOrderDetailEntity.getUserId())
                            .teamId(userGroupBuyOrderDetailEntity.getTeamId())
                            .activityId(userGroupBuyOrderDetailEntity.getActivityId())
                            .targetCount(userGroupBuyOrderDetailEntity.getTargetCount())
                            .completeCount(userGroupBuyOrderDetailEntity.getCompleteCount())
                            .lockCount(userGroupBuyOrderDetailEntity.getLockCount())
                            .validStartTime(userGroupBuyOrderDetailEntity.getValidStartTime())
                            .validEndTime(userGroupBuyOrderDetailEntity.getValidEndTime())
                            .validTimeCountdown(GoodsMarketResponseDTO.Team.differenceDateTime2Str(new Date(), userGroupBuyOrderDetailEntity.getValidEndTime()))
                            .outTradeNo(userGroupBuyOrderDetailEntity.getOutTradeNo())
                            .build();
                    teams.add(goodsTeam);
                }
            }
            GoodsMarketResponseDTO goodsMarketResponseDTO = GoodsMarketResponseDTO.builder()
                    .goods(GoodsMarketResponseDTO.Goods.builder()
                            .deductionPrice(trialBalanceEntity.getDeductionPrice())
                            .goodsId(goodsId)
                            .payPrice(trialBalanceEntity.getPayPrice())
                            .originalPrice(trialBalanceEntity.getOriginalPrice())
                            .build())
                    .teamList(teams)
                    .activityId(trialBalanceEntity.getGroupBuyActivityDiscountVO().getActivityId())
                    .teamStatistic(GoodsMarketResponseDTO.TeamStatistic.builder()
                            .allTeamCompleteCount(teamStatisticVO.getAllTeamCompleteCount())
                            .allTeamUserCount(teamStatisticVO.getAllTeamUserCount())
                            .allTeamCount(teamStatisticVO.getAllTeamCount())
                            .build())
                    .build();
            Response<GoodsMarketResponseDTO> response = Response.<GoodsMarketResponseDTO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(goodsMarketResponseDTO)
                    .build();

            log.info("查询拼团营销配置完成:{} goodsId:{} response:{}", goodsMarketRequestDTO.getUserId(), goodsMarketRequestDTO.getGoodsId(), JSON.toJSONString(response));

            return response;
        } catch (Exception e) {
            log.error("查询拼团营销配置失败:{} goodsId:{}", goodsMarketRequestDTO.getUserId(), goodsMarketRequestDTO.getGoodsId(), e);
            return Response.<GoodsMarketResponseDTO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }

    }

    @Override
    @GetMapping("query_group_buy_order_list")
    public Response<List<BuyOrderListResponseDTO>> queryGroupBuyMarketOrderList(BuyOrderListRequestDTO requestDTO) {
        try {
            List<BuyOrderListEntity> buyOrderListEntity = indexGroupBuyMarketService.queryOrderListByUserId(requestDTO.getUserId());
            List<BuyOrderListResponseDTO> responseData = new ArrayList<>();
            for (BuyOrderListEntity entity : buyOrderListEntity) {
                BuyOrderListResponseDTO data = BuyOrderListResponseDTO.builder()
                        .goodName(entity.getGoodName())
                        .tradeCreateTime(entity.getTradeCreateTime())
                        .userNo(entity.getUserNo())
                        .status(entity.getStatus())
                        .outTradeNo(entity.getOutTradeNo())
                        .build();
                responseData.add(data);
            }
            Response<List<BuyOrderListResponseDTO>> response = Response.<List<BuyOrderListResponseDTO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .data(responseData)
                    .build();
            return response;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
