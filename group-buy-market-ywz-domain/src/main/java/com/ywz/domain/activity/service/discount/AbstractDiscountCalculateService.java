package com.ywz.domain.activity.service.discount;

import com.ywz.domain.activity.model.valobj.DiscountTypeEnum;
import com.ywz.domain.activity.model.valobj.GroupBuyActivityDiscountVO;

import java.math.BigDecimal;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/6/1 21:31
 */
public abstract class AbstractDiscountCalculateService implements IDiscountCalculateService{

    @Override
    public BigDecimal calculate(String userId, BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount) {
        // 1. 人群标签过滤
        if (DiscountTypeEnum.TAG.equals(groupBuyDiscount.getDiscountType())){
            boolean isCrowdRange = filterTagId(userId, groupBuyDiscount.getTagId());
            if (!isCrowdRange) {
                return originalPrice;
            }
        }
        // 2. 折扣优惠计算
        return doCalculate(originalPrice, groupBuyDiscount);
    }

    /**
     * 人群过滤 - 限定人群优惠
     */
    private boolean filterTagId(String userId, String tagId) {
        // todo xiaofuge 后续开发这部分
        return true;
    }

    /**
     * 折扣优惠计算
     * @param originalPrice 原价
     * @param groupBuyDiscount 折扣信息
     * @return 计算后的价格
     */
    protected abstract BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount);
}
