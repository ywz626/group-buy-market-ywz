package com.ywz.api;

import com.ywz.api.dto.BuyOrderListRequestDTO;
import com.ywz.api.dto.BuyOrderListResponseDTO;
import com.ywz.api.dto.GoodsMarketRequestDTO;
import com.ywz.api.dto.GoodsMarketResponseDTO;
import com.ywz.api.response.Response;

import java.util.List;

/**
 * @author 于汶泽
 * @Description: 前端发起的首页信息查询服务
 * @DateTime: 2025/6/6 17:08
 */
public interface IMarketIndexService {

    /**
     * 查询拼团首页信息
     *
     * @param requestDTO 请求参数
     * @return 返回拼团市场配置
     */
    Response<GoodsMarketResponseDTO> queryGroupBuyMarketConfig(GoodsMarketRequestDTO requestDTO);

    /**
     * 查询用户参加的拼团列表
     *
     * @param requestDTO 请求参数
     * @return 返回拼团市场配置
     */
    Response<List<BuyOrderListResponseDTO>> queryGroupBuyMarketOrderList(BuyOrderListRequestDTO requestDTO);
}
