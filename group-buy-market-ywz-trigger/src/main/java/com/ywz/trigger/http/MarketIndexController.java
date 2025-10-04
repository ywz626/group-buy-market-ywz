package com.ywz.trigger.http;

import cn.bugstack.wrench.rate.limiter.types.annotations.RateLimiterAccessInterceptor;
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

    /**
     * 查询拼团营销配置接口
     * <p>
     * 根据商品ID、用户ID、来源和渠道等信息，获取该商品参与的拼团活动相关营销配置，
     * 包括优惠试算结果、队伍统计信息以及当前用户的队伍详情。
     *
     * @param goodsMarketRequestDTO 请求参数对象，包含以下字段：
     *                              - goodsId: 商品ID（必填）
     *                              - userId: 用户ID（必填）
     *                              - source: 来源（必填）
     *                              - channel: 渠道（必填）
     * @return 返回拼团营销相关信息，包括商品价格信息、队伍列表及活动统计数据。
     *         若请求参数缺失或处理异常，则返回错误码与提示信息。
     */
    @Override
    @RateLimiterAccessInterceptor(key = "userId", fallbackMethod = "queryGroupBuyMarketConfigFallBack", permitsPerSecond = 1.0d, blacklistCount = 1)
    @PostMapping("query_group_buy_market_config")
    public Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfig(@RequestBody GoodsMarketRequestDTO goodsMarketRequestDTO) {
        try {
            String goodsId = goodsMarketRequestDTO.getGoodsId();
            String userId = goodsMarketRequestDTO.getUserId();
            String source = goodsMarketRequestDTO.getSource();
            String channel = goodsMarketRequestDTO.getChannel();

            // 参数校验：确保必要参数不为空
            if (StringUtils.isBlank(goodsId) || StringUtils.isBlank(userId) || StringUtils.isBlank(source) || StringUtils.isBlank(channel)) {
                return Response.<GoodsMarketResponseDTO>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info(ResponseCode.ILLEGAL_PARAMETER.getInfo())
                        .build();
            }

            // 营销优惠试算：根据传入的商品和用户信息计算折扣后价格
            TrialBalanceEntity trialBalanceEntity = indexGroupBuyMarketService.indexMarketTrial(MarketProductEntity.builder()
                    .userId(userId)
                    .goodsId(goodsId)
                    .source(source)
                    .channel(channel)
                    .build());

            // 获取拼团活动ID并查询该活动的整体组队统计信息
            Long activityId = trialBalanceEntity.getGroupBuyActivityDiscountVO().getActivityId();
            TeamStatisticVO teamStatisticVO = indexGroupBuyMarketService.queryGroupTeamStatistic(activityId);

            // 查询当前用户所在团队的信息（最多查两个队伍）
            List<UserGroupBuyOrderDetailEntity> team = indexGroupBuyMarketService.getTeamList(activityId, userId, 1, 2);

            // 构造返回给前端的队伍信息列表
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

            // 组装最终响应数据结构
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

        /**
     * 查询拼团营销配置的降级处理方法
     * 当查询拼团营销配置接口触发限流时，会调用此降级方法返回限流响应
     *
     * @param requestDTO 商品营销请求参数对象，包含用户ID等信息
     * @return 返回限流响应结果，包含限流错误码和错误信息
     */
    public Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfigFallBack(@RequestBody GoodsMarketRequestDTO requestDTO) {
        // 记录限流错误日志，输出触发限流的用户ID
        log.error("查询拼团营销配置限流:{}", requestDTO.getUserId());
        // 构建限流响应结果
        return Response.<GoodsMarketResponseDTO>builder()
                .code(ResponseCode.RATE_LIMITER.getCode())
                .info(ResponseCode.RATE_LIMITER.getInfo())
                .build();
    }



    /**
     * 查询团购订单列表
     *
     * @param requestDTO 包含用户ID等查询条件的请求参数对象
     * @return 返回包含订单列表信息的响应对象，其中data字段为BuyOrderListResponseDTO列表
     */
    @Override
    @GetMapping("query_group_buy_order_list")
    public Response<List<BuyOrderListResponseDTO>> queryGroupBuyMarketOrderList(BuyOrderListRequestDTO requestDTO) {
        try {
            // 根据用户ID查询订单列表
            List<BuyOrderListEntity> buyOrderListEntity = indexGroupBuyMarketService.queryOrderListByUserId(requestDTO.getUserId());

            // 将实体对象转换为响应DTO对象
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

            // 构建成功响应结果
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
