package com.ywz.infrastructure.dao.po.base;

import com.baomidou.mybatisplus.annotation.TableField;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/6/7 12:40
 */
public class Page {
    @TableField(exist = false)
    private Integer count;

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
