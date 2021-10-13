package io.cruii.bilibili.util;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/10/13
 */
@Log4j2
public class ProxyUtil {
    private static String proxyApi;
    private static final List<String> proxyList = new ArrayList<>();

    private ProxyUtil() {
    }


    public static synchronized String get() {
        try {
            TimeUnit.SECONDS.sleep(5L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        if (CharSequenceUtil.isBlank(proxyApi)) {
            try {
                InputStream stream = ResourceUtil.getStream("proxy.properties");
                Properties properties = new Properties();
                properties.load(stream);
                proxyApi = properties.getProperty("proxy.api");
            } catch (IOException e) {
                log.error("无法获取proxy.properties文件");
            }
        }

        if (proxyList.isEmpty()) {
            log.debug("请求新一批代理");
            String body = HttpRequest.get(proxyApi)
                    .execute().body();
            JSONObject resp = JSONUtil.parseObj(body);
            proxyList.addAll(resp.getJSONArray("data")
                    .stream()
                    .map(JSONUtil::parseObj)
                    .map(obj -> obj.getStr("ip") + ":" + obj.getInt("port"))
                    .collect(Collectors.toList()));
        }
        String proxy = proxyList.get(0);
        proxyList.remove(proxy);
        log.debug("本次获取代理地址: {}", proxy);
        log.debug("当前剩余代理数: {}", proxyList.size());
        return proxy;
    }

}
