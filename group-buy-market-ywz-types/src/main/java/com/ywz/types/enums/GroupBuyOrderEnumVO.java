package com.ywz.types.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author ywz
 * @description 拼团订单枚举
 * @create
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public enum GroupBuyOrderEnumVO {


    /** * 拼团订单状态
     */
    PROGRESS(0, "拼单中"),
    COMPLETE(1, "完成"),
    FAIL(2, "失败"),
    ;

    private Integer code;
    private String info;

    public static GroupBuyOrderEnumVO valueOf(Integer code) {
        switch (code) {
            case 0:
                return PROGRESS;
            case 1:
                return COMPLETE;
            case 2:
                return FAIL;
        }
        throw new RuntimeException("err code not exist!");
    }

}
