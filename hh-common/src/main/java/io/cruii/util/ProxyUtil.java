package io.cruii.util;

import cn.hutool.core.io.resource.ResourceUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * @author cruii
 * Created on 2021/10/13
 */
@Log4j2
public class ProxyUtil {
    private static String proxyApi;
    private static final List<Proxy> proxyList = new ArrayList<>();

    private static final ReentrantLock LOCK = new ReentrantLock();

    private ProxyUtil() {
    }

    static {
        try {
            InputStream stream = ResourceUtil.getStream("config.properties");
            Properties properties = new Properties();
            properties.load(stream);
            proxyApi = properties.getProperty("proxy.api");
        } catch (IOException e) {
            log.error("无法获取proxy.properties文件");
        }
    }

    public static Proxy get() {
        try {
            LOCK.lock();
            int size = proxyList.size();
            if (proxyList.isEmpty()) {
                String body = HttpRequest.get(proxyApi).execute().body();
                JSONObject resp = JSONUtil.parseObj(body);
                JSONArray data = resp.getJSONArray("data");
                size = data.size();
                proxyList.addAll(data.stream()
                        .map(JSONUtil::parseObj)
                        .map(obj -> new Proxy(Proxy.Type.HTTP, new InetSocketAddress(obj.getStr("ip"), obj.getInt("port"))))
                        .collect(Collectors.toList()));
            }
            Proxy proxy = proxyList.get(RandomUtil.randomInt(size - 1));

            if (testProxy(proxy)) {
                log.debug("本次获取代理地址: {}", proxy);
                return proxy;
            } else {
                log.debug("代理 {} 不可用，切换代理", proxy);
                proxyList.remove(proxy);
                return get();
            }
        } catch (Exception e) {
            log.error("获取代理地址异常: {}", e.getMessage());
            return null;
        } finally {
            LOCK.unlock();
        }

    }

    private static boolean testProxy(Proxy proxy) {
        HttpURLConnection connection = null;
        try {
            connection = (HttpURLConnection) new URL("https://www.bilibili.com/").openConnection(proxy);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            int statusCode = connection.getResponseCode();
            return statusCode == HttpURLConnection.HTTP_OK;
        } catch (IOException e) {
            return false;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}
