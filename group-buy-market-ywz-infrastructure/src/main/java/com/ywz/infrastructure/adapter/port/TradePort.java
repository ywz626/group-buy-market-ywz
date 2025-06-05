package com.ywz.infrastructure.adapter.port;

import com.ywz.domain.trade.adapter.port.ITradePort;
import com.ywz.domain.trade.model.entity.NotifyTaskEntity;
import com.ywz.infrastructure.gateway.NotifyRequestDTO;
import com.ywz.infrastructure.redis.IRedisService;
import com.ywz.types.enums.NotifyTaskHTTPEnumVO;
import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RLock;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/6/5 21:16
 */
@Service
public class TradePort implements ITradePort {


    @Resource
    private NotifyRequestDTO notifyRequestDTO;
    @Resource
    private IRedisService redisService;

    @Override
    public String groupBuyNotify(NotifyTaskEntity notifyTask) throws Exception {
        RLock lock = redisService.getLock(notifyTask.lockKey());
        try {
            if(lock.tryLock()){
                try {
                    if (StringUtils.isBlank(notifyTask.getNotifyUrl()) || "暂无".equals(notifyTask.getNotifyUrl())) {
                        return NotifyTaskHTTPEnumVO.SUCCESS.getCode();
                    }
                    return notifyRequestDTO.groupBuyNotify(notifyTask.getNotifyUrl(), notifyTask.getParameterJson());
                }finally {
                    if(lock.isLocked() && lock.isHeldByCurrentThread()){
                        lock.unlock();
                    }
                }
            }
            return NotifyTaskHTTPEnumVO.NULL.getCode();
        }catch (Exception e){
            Thread.currentThread().interrupt();
            return NotifyTaskHTTPEnumVO.ERROR.getCode();
        }


    }
}
