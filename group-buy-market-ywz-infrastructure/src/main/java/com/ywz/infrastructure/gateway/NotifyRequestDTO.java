package com.ywz.infrastructure.gateway;

import com.ywz.types.enums.ResponseCode;
import com.ywz.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
 * @author 于汶泽
 * @Description: 调用外部接口进行拼团回调通知的服务
 * @DateTime: 2025/6/5 21:13
 */
@Service
@Slf4j
public class NotifyRequestDTO {

    @Resource
    private OkHttpClient okHttpClient;

    public String groupBuyNotify(String url, String json) {
        try {
            log.info("拼团完成结算回调接口被调用！！！！！！！！！");
            // 1. 构建参数
            MediaType mediaType = MediaType.parse("application/json");
            RequestBody body = RequestBody.create(mediaType, json);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .addHeader("content-type", "application/json")
                    .build();

            // 2. 调用接口
            Response response = okHttpClient.newCall(request).execute();

            // 3. 返回结果
            if(!response.isSuccessful()){
                return "error";
            }
            return response.body().string();
        } catch (Exception e) {
            log.error("拼团回调 HTTP 接口服务异常 {}", url, e);
            throw new AppException(ResponseCode.HTTP_EXCEPTION);
        }
    }
}
