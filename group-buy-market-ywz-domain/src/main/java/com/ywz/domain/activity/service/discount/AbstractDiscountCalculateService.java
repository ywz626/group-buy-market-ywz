package com.ywz.domain.activity.service.discount;

import com.ywz.domain.activity.adapter.repository.IActivityRepository;
import com.ywz.domain.activity.model.valobj.DiscountTypeEnum;
import com.ywz.domain.activity.model.valobj.GroupBuyActivityDiscountVO;

import javax.annotation.Resource;
import java.math.BigDecimal;

/**
 * @author 于汶泽
 * @Description: 执行折扣计算的抽象类，抽取四个节点公共的人群标签过滤逻辑
 * @DateTime: 2025/6/1 21:31
 */
public abstract class AbstractDiscountCalculateService implements IDiscountCalculateService{

    @Resource
    private IActivityRepository repository;

    /**
     * 抽取四个节点公共的人群标签过滤逻辑
     * @param userId 用户ID
     * @param originalPrice 原价
     * @param groupBuyDiscount 拼团活动折扣信息
     * @return 计算后的价格
     */
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
        return repository.isTagCrowdRange(userId, tagId);
    }

    /**
     * 折扣优惠计算
     * @param originalPrice 原价
     * @param groupBuyDiscount 折扣信息
     * @return 计算后的价格
     */
    protected abstract BigDecimal doCalculate(BigDecimal originalPrice, GroupBuyActivityDiscountVO.GroupBuyDiscount groupBuyDiscount);
}
