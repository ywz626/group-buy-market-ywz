package com.ywz.infrastructure.dcc;

import com.ywz.types.annotations.DCCValue;
import com.ywz.types.common.Constants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * @author 于汶泽
 * @Description: 动态配置服务
 * @DateTime: 2025/6/3 12:29
 */
@Service
@Slf4j
public class DCCService {

    @DCCValue("downgradeSwitch:0")
    private String downgradeSwitch;

    @DCCValue("cutRange:0")
    private String cutRange;
    @DCCValue("scBlackList:s02c02")
    private String scBlackList;

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

    public boolean isScBlackList(String source,String channel) {
        List<String> list = Arrays.asList(scBlackList.split(Constants.SPLIT));
        // 判断是否在黑名单中
        log.info("source:{}, channel:{}, scBlackList:{},list:{}", source, channel, scBlackList, list);
        return list.contains(source + channel);
    }
}
