package com.ywz.trigger.http;

import com.ywz.api.IDCCService;
import com.ywz.api.response.Response;
import com.ywz.types.enums.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RTopic;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author 于汶泽
 * @Description: 动态配置管理 Dynamic Configuration Control (DCC) 控制器
 * @DateTime: 2025/6/3 13:12
 */
@Slf4j
@RestController()
@CrossOrigin("*")
@RequestMapping("/api/v1/gbm/dcc/")
public class DCCController implements IDCCService {
    @Resource
    private RTopic dccTopic;

    /**
     * 动态值变更
     * <p>
     * curl <a href="http://127.0.0.1:8091/api/v1/gbm/dcc/update_config?key=downgradeSwitch&value=1">...</a>
     * curl <a href="http://127.0.0.1:8091/api/v1/gbm/dcc/update_config?key=cutRange&value=0">...</a>
     */
    @RequestMapping(value = "update_config", method = RequestMethod.GET)
    @Override
    public Response<Boolean> updateConfig(@RequestParam String key, @RequestParam String value) {
        try {
            log.info("DCC 动态配置值变更 key:{} value:{}", key, value);
            dccTopic.publish(key + "," + value);
            return Response.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getInfo())
                    .build();
        } catch (Exception e) {
            log.error("DCC 动态配置值变更失败 key:{} value:{}", key, value, e);
            return Response.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info(ResponseCode.UN_ERROR.getInfo())
                    .build();
        }
    }
}
