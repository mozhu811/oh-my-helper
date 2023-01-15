package io.cruii.util;

import cn.hutool.core.text.CharSequenceUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.SocketException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * @author cruii
 * Created on 2021/12/20
 */
@Slf4j
public class HttpUtil {
    private HttpUtil() {
    }

    // 最大连接数
    private static final int MAX_CONNECTION = 100;
    // 每个route能使用的最大连接数，一般和MAX_CONNECTION取值一样
    private static final int MAX_CONCURRENT_CONNECTIONS = 100;
    // 建立连接的超时时间，单位毫秒
    private static final int CONNECTION_TIME_OUT = 10000;
    // 请求超时时间，单位毫秒
    private static final int REQUEST_TIME_OUT = 10000;
    // 最大失败重试次数
    private static final int MAX_FAIL_RETRY_COUNT = 10;

    //public static CloseableHttpClient buildHttpClient() {
    //    SocketConfig socketConfig = SocketConfig.custom()
    //            .setSoTimeout(REQUEST_TIME_OUT).setSoKeepAlive(true)
    //            .setTcpNoDelay(true).build();
    //    RequestConfig requestConfig = RequestConfig.custom()
    //            .setProxy(new HttpHost("127.0.0.1", 7890))
    //            .setSocketTimeout(REQUEST_TIME_OUT)
    //            .setConnectTimeout(CONNECTION_TIME_OUT).build();
    //    /*
    //     * 每个默认的 ClientConnectionPoolManager 实现将给每个route创建不超过2个并发连接，最多20个连接总数。
    //     */
    //    PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
    //    connManager.setMaxTotal(MAX_CONNECTION);
    //    connManager.setDefaultMaxPerRoute(MAX_CONCURRENT_CONNECTIONS);
    //    connManager.setDefaultSocketConfig(socketConfig);
    //
    //    return HttpClients.custom().setConnectionManager(connManager)
    //            .setDefaultRequestConfig(requestConfig)
    //            // 添加重试处理器
    //            .setRetryHandler(new HttpRequestRetryHandler()).build();
    //}

    public static CloseableHttpClient buildHttpClient() {

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(REQUEST_TIME_OUT).setSoKeepAlive(true)
                .setTcpNoDelay(true).build();

        RequestConfig.Builder builder = RequestConfig.custom()
                .setSocketTimeout(REQUEST_TIME_OUT)
                .setConnectTimeout(CONNECTION_TIME_OUT);

        // 从代理池获取ip并设置
        List<String> split = CharSequenceUtil.split(ProxyUtil.get(), ":");
        builder.setProxy(new HttpHost(split.get(0), Integer.parseInt(split.get(1))));

        RequestConfig requestConfig = builder.build();

        /*
         * 每个默认的 ClientConnectionPoolManager 实现将给每个route创建不超过2个并发连接，最多20个连接总数。
         */
        PoolingHttpClientConnectionManager connManager = new PoolingHttpClientConnectionManager();
        connManager.setMaxTotal(MAX_CONNECTION);
        connManager.setDefaultMaxPerRoute(MAX_CONCURRENT_CONNECTIONS);
        connManager.setDefaultSocketConfig(socketConfig);

        return HttpClients.custom().setConnectionManager(connManager)
                .setDefaultRequestConfig(requestConfig)
                // 添加重试处理器
                .setRetryHandler(new HttpRequestRetryHandler()).build();
    }

    public static URI buildUri(String url) {
        return buildUri(url, null);
    }

    public static URI buildUri(String url, Map<String, String> params) {
        URIBuilder uriBuilder;
        URI uri;
        try {
            uriBuilder = new URIBuilder(url);
            if (params != null) {
                for (Map.Entry<String, String> param : params.entrySet()) {
                    uriBuilder.addParameter(param.getKey(), param.getValue());
                }
            }
            uri = uriBuilder.build();
        } catch (URISyntaxException e) {
            log.error("解析URL失败", e);
            throw new RuntimeException(e);
        }
        return uri;
    }

    static class HttpRequestRetryHandler implements org.apache.http.client.HttpRequestRetryHandler {
        @Override
        public boolean retryRequest(IOException exception, int i, HttpContext context) {
            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();

            if (exception instanceof SocketException) {
                RequestConfig requestConfig = clientContext.getRequestConfig();
                HttpHost old = requestConfig.getProxy();
                Class<? extends RequestConfig> clazz = requestConfig.getClass();
                try {
                    Field proxy = clazz.getDeclaredField("proxy");
                    String[] proxyArr = ProxyUtil.get().split(":");
                    proxy.setAccessible(true);
                    proxy.set(requestConfig, new HttpHost(proxyArr[0], Integer.parseInt(proxyArr[1])));
                    log.debug("The HTTP proxy has been changed: {} -> {}",
                            old.getHostName() + ":" + old.getPort(), proxyArr[0] + ":" + proxyArr[1]);
                } catch (NoSuchFieldException | IllegalAccessException e) {
                    throw new RuntimeException(e);
                }
                return true;
            }
            if (i >= MAX_FAIL_RETRY_COUNT) {
                return false;
            }

            // 如果请求被认为是幂等的，则重试
            return !(request instanceof HttpEntityEnclosingRequest);
        }
    }
}
