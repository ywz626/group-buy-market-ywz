package com.ywz.domain.activity.service.discount;

import com.ywz.domain.activity.model.valobj.DiscountTypeEnum;
import com.ywz.domain.activity.model.valobj.GroupBuyActivityDiscountVO;

import java.math.BigDecimal;

/**
 * @author 于汶泽
 * @Description: 折扣计算服务接口
 * @DateTime: 2025/6/1 21:28
 */
public interface IDiscountCalculateService {




    BigDecimal calculate(String userId, BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount);
}
