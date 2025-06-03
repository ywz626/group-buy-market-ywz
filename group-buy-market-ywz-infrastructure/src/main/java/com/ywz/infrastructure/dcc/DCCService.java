package com.ywz.infrastructure.dcc;

import com.ywz.types.annotations.DCCValue;
import org.springframework.stereotype.Service;

/**
 * @author 于汶泽
 * @Description: 动态配置服务
 * @DateTime: 2025/6/3 12:29
 */
@Service
public class DCCService {

    @DCCValue("downgradeSwitch:0")
    private String downgradeSwitch;

    @DCCValue("cutRange:100")
    private String cutRange;

    public boolean isDowngradeSwitch() {
        return "1".equals(downgradeSwitch);
    }

    public boolean isCutRange(String userId) {
        // 计算哈希码的绝对值
        int hashCode = Math.abs(userId.hashCode());

        // 获取最后两位
        int lastTwoDigits = hashCode % 100;

        // 判断是否在切量范围内
        return lastTwoDigits <= Integer.parseInt(cutRange);
    }
}
