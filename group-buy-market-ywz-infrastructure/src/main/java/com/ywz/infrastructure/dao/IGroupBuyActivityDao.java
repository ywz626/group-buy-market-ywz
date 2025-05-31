package com.ywz.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ywz.infrastructure.dao.po.GroupBuyActivityPO;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 于汶泽
 * @description 拼团活动Dao
 * @create 2025-05-31
 */
@Mapper
public interface IGroupBuyActivityDao extends BaseMapper<GroupBuyActivityPO> {

}
