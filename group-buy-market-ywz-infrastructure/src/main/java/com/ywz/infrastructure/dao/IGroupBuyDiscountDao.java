package com.ywz.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.ywz.infrastructure.dao.po.GroupBuyDiscountPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 于汶泽
 * @description 折扣配置Dao
 * @create 2025:5:31
 */
@Mapper
public interface IGroupBuyDiscountDao extends BaseMapper<GroupBuyDiscountPO> {

}
