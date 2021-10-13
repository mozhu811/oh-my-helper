package io.cruii.bilibili.util;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author cruii
 * Created on 2021/10/13
 */
@Log4j2
public class ProxyUtil {
    private static String proxyApi;

    private ProxyUtil(){}

    public static String get() {
        if (CharSequenceUtil.isBlank(proxyApi)) {
            try {
                InputStream stream = ResourceUtil.getStream("proxy.properties");
                Properties properties = new Properties();
                properties.load(stream);
                proxyApi = properties.getProperty("proxy.api");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String body = HttpRequest.get(proxyApi)
                .execute().body();
        JSONObject resp = JSONUtil.parseObj(body);
        JSONArray proxyList = resp.getJSONArray("data");

        JSONObject proxyObj = (JSONObject) proxyList.get(0);
        String proxy = proxyObj.getStr("ip") + ":" + proxyObj.getInt("port");
        log.debug("本次获取代理地址: {}", proxy);

        return proxy;
    }
}
