package com.ywz.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ywz.infrastructure.dao.po.ScSkuActivity;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 于汶泽
 * @Description: 渠道商品活动数据访问对象
 * @DateTime: 2025/6/2 16:12
 */
@Mapper
public interface IScSkuActivityDao extends BaseMapper<ScSkuActivity> {
}
