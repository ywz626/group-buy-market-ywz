package com.ywz.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ywz.infrastructure.dao.po.GroupBuyOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @author Fuzhengwei bugstack.cn @小傅哥
 * @description 用户拼单
 * @create 2025-01-11 10:33
 */
@Mapper
public interface IGroupBuyOrderDao extends BaseMapper<GroupBuyOrder> {

    Integer getAllTeamUserCount(Long activityId);

    Integer getAllTeamCompleteCount(Long activityId);

    Integer getAllTeamCount(Long activityId);


//    List<Long> getTeamIdsByActivityId(Long activityId);
}
