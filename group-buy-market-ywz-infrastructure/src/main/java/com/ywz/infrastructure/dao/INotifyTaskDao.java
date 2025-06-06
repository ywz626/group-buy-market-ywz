package com.ywz.infrastructure.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.ywz.infrastructure.dao.po.NotifyTask;
import org.apache.ibatis.annotations.Mapper;

/**
 * @author 于汶泽
 * @Description: 执行回调任务数据访问对象
 * @DateTime: 2025/6/4 21:04
 */
@Mapper
public interface INotifyTaskDao extends BaseMapper<NotifyTask> {
}
