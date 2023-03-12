package io.cruii.util;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okio.Buffer;
import okio.BufferedSource;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * @author cruii
 * Created on 2023/2/2
 */
@Slf4j
public class OkHttpUtil {
    private static final int MAX_RETRY = 10;

    private OkHttpUtil() {
    }

    private static OkHttpClient getClient(boolean useProxy) {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .addInterceptor(new CustomRequestInterceptor());

        if (useProxy) {
            builder.proxy(ProxyUtil.get());
        }

        return builder.build();
    }

    public static Response executeWithRetry(Request request) throws IOException {
        return executeWithRetry(request, true);
    }
    public static Response executeWithRetry(Request request, boolean safeMode) throws IOException {
        int count = 0;
        while (true) {
            try {
                log.debug("第{}次请求: {}", count + 1, request.url());
                OkHttpClient okHttpClient = getClient(safeMode);
                Call call = okHttpClient.newCall(request);
                return call.execute();
            } catch (Exception e) {
                if (count > MAX_RETRY) {
                    throw e;
                }
                count++;
            }
        }
    }

}

@Slf4j
class CustomRequestInterceptor implements Interceptor {
    @NotNull
    @Override
    public Response intercept(@NotNull Chain chain) throws IOException {
        Request request = chain.request();
        log.debug("==============");
        log.debug("Request URL: {}", request.url());
        log.debug("Headers: {}", request.headers());
        log.debug("Cookies: {}", request.headers("Cookie"));
        if (request.body() != null) {
            Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            log.debug("Request body: {}", buffer.readString(StandardCharsets.UTF_8));
        }
        Response response = chain.proceed(request);
        if (response.body() != null) {
            BufferedSource source = response.body().source();
            source.request(Long.MAX_VALUE);
            try (Buffer respBuffer = source.getBuffer().clone();) {
                String resp = respBuffer.readString(StandardCharsets.UTF_8);
                log.debug("响应代码: {}", response.code());
                if (!FileUtil.extName(request.url().toString()).equals("jpg")) {
                    log.debug("响应结果: {}", resp);
                }
            }
        }
        log.debug("==============");
        return response;
    }
}