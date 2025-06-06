package com.ywz.domain.trade.adapter.port;

import com.ywz.domain.trade.model.entity.NotifyTaskEntity;

/**
 * @author 于汶泽
 * @Description: 在domain层定义的交易端口接口 具体实现在infrastructure层
 * @DateTime: 2025/6/5 21:10
 */

public interface ITradePort {


    /**
     * 拼团活动通知
     * @param notifyEntity 通知任务实体
     * @return 返回通知结果
     * @throws Exception 异常
     */
    public String groupBuyNotify(NotifyTaskEntity notifyEntity) throws Exception;

}
