package com.ywz.domain.trade.adapter.port;

import com.ywz.domain.trade.model.entity.NotifyTaskEntity;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/6/5 21:10
 */

public interface ITradePort {


    public String groupBuyNotify(NotifyTaskEntity notifyEntity) throws Exception;

}
