package com.ywz.domain.activity.model.valobj;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author ywz
 * @description 折扣优惠类型
 * @create 2024-12-22 12:37
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum DiscountTypeEnum {

    /**
     * 基础优惠：直接折扣
     * 人群标签：根据用户标签进行折扣
     */
    BASE(0, "基础优惠"),
    TAG(1, "人群标签"),
    ;

    @EnumValue
    private Integer code;
    private String info;

    public static DiscountTypeEnum get(Integer code) {
        switch (code) {
            case 0:
                return BASE;
            case 1:
                return TAG;
            default:
                throw new RuntimeException("err code!");
        }
    }

}
