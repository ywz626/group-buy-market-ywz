package com.ywz.api;


import com.ywz.api.response.Response;

/**
 * @author 于汶泽
 * @description DCC 动态配置中心
 * @create 2025-06-03 13:13
 */
public interface IDCCService {

    Response<Boolean> updateConfig(String key, String value);

}
