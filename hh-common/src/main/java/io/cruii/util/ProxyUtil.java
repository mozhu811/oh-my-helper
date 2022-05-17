package io.cruii.util;

import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/10/13
 */
@Log4j2
public class ProxyUtil {
    private static final String PROXY_API;
    private static final List<String> PROXY_LIST = new ArrayList<>();

    private ProxyUtil() {
    }

    static {
        try {
            InputStream stream = ResourceUtil.getStream("proxy.properties");
            Properties properties = new Properties();
            properties.load(stream);
            PROXY_API = properties.getProperty("proxy.api");
        } catch (IOException e) {
            log.error("无法获取proxy.properties文件");
            throw new RuntimeException("无法获取proxy.properties文件", e);
        }
    }

    public synchronized static String get() {
        if (PROXY_LIST.isEmpty()) {
            log.debug("获取代理地址");
            try (HttpResponse httpResponse = HttpRequest.get(PROXY_API).execute()) {
                JSONObject resp = JSONUtil.parseObj(httpResponse.body());
                PROXY_LIST.addAll(resp.getJSONArray("data")
                        .stream()
                        .map(JSONUtil::parseObj)
                        .map(obj -> obj.getStr("ip") + ":" + obj.getInt("port"))
                        .collect(Collectors.toList()));
            }
        }
        String proxy = PROXY_LIST.get(0);
        PROXY_LIST.remove(proxy);
        log.debug("本次获取代理地址: {}", proxy);
        log.debug("当前剩余代理数: {}", PROXY_LIST.size());
        while (!checkProxy(proxy)) {
            proxy = ProxyUtil.get();
        }
        return proxy;
    }

    private static boolean checkProxy(String proxy) {
        String host = proxy.split(":")[0];
        int port = Integer.parseInt(proxy.split(":")[1]);
        try (HttpResponse response = HttpRequest.get("https://www.bilibili.com")
                .setHttpProxy(host, port)
                .setConnectionTimeout(10000)
                .setReadTimeout(10000)
                .execute()) {
            if (response.getStatus() == 200) {
                log.debug("该代理地址可用");
                return true;
            }
        } catch (IORuntimeException e) {
            log.debug("该代理地址不可用");
            return false;
        }
        log.debug("该代理地址不可用");
        return false;
    }
}
