package com.ywz.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ywz.infrastructure.dao.po.Sku;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 于汶泽
 * @description 商品查询
 * @create 2025-6-01 17:47
 */
@Mapper
public interface ISkuDao extends BaseMapper<Sku> {

}
