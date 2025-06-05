package com.ywz.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ywz.infrastructure.dao.po.GroupBuyOrderList;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 用户拼单明细
 * @create 2025-01-11 09:07
 */
@Mapper
public interface IGroupBuyOrderListDao extends BaseMapper<com.ywz.infrastructure.dao.po.GroupBuyOrderList> {


    /**
     * 查询用户拼单明细列表
     * @param teamId 队伍Id
     * @return 用户拼单明细列表
     */
    List<String> selectAllOutTradeNoByTeamId(String teamId);
}
