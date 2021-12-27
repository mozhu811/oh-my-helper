package io.cruii.bilibili.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.protocol.HttpContext;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
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
    private static final int MAX_FAIL_RETRY_COUNT = 5;

    public static CloseableHttpClient buildHttpClient() {
        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(REQUEST_TIME_OUT).setSoKeepAlive(true)
                .setTcpNoDelay(true).build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(REQUEST_TIME_OUT)
                .setConnectTimeout(CONNECTION_TIME_OUT).build();
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

    public static CloseableHttpClient buildHttpClient(String ip, int port) {

        SocketConfig socketConfig = SocketConfig.custom()
                .setSoTimeout(REQUEST_TIME_OUT).setSoKeepAlive(true)
                .setTcpNoDelay(true).build();
        RequestConfig requestConfig = RequestConfig.custom()
                .setProxy(new HttpHost(ip, port))
                .setSocketTimeout(REQUEST_TIME_OUT)
                .setConnectTimeout(CONNECTION_TIME_OUT).build();
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
            if (i >= MAX_FAIL_RETRY_COUNT) {
                return false;
            }

            if (exception instanceof InterruptedIOException) {
                // 超时
                return true;
            }
            if (exception instanceof UnknownHostException) {
                // 未知主机
                return false;
            }

            if (exception instanceof SSLException) {
                // SSL handshake exception
                return false;
            }

            HttpClientContext clientContext = HttpClientContext.adapt(context);
            HttpRequest request = clientContext.getRequest();
            // 如果请求被认为是幂等的，则重试
            return !(request instanceof HttpEntityEnclosingRequest);
        }
    }
}
