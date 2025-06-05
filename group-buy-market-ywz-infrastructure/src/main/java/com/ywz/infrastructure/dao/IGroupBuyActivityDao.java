package com.ywz.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ywz.infrastructure.dao.po.GroupBuyActivityPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Date;

/**
 * @author 于汶泽
 * @description 拼团活动Dao
 * @create 2025-05-31
 */
@Mapper
public interface IGroupBuyActivityDao extends BaseMapper<GroupBuyActivityPO> {

    /**
     * 查询活动有效时间
     * @param activityId 活动ID
     * @return 活动有效时间
     */
    int getActivityValidTime(Long activityId);
}
