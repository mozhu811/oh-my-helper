package io.cruii.util;

import org.slf4j.MDC;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * @author cruii
 * Created on 2021/9/24
 */
public class ThreadMdcUtil {
    private ThreadMdcUtil() {
    }

    public static void setTraceIdIfAbsent() {
        if (MDC.get("traceId") == null || MDC.get("traceId").length() == 0) {
            String tid = UUID.randomUUID().toString();
            MDC.put("traceId", tid);
        }
    }

    public static <T> Callable<T> wrap(final Callable<T> callable, final Map<String, String> context) {
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            setTraceIdIfAbsent();
            try {
                return callable.call();
            } finally {
                MDC.clear();
            }
        };
    }

    public static Runnable wrap(final Runnable runnable, final Map<String, String> context) {
        return () -> {
            if (context == null) {
                MDC.clear();
            } else {
                MDC.setContextMap(context);
            }
            setTraceIdIfAbsent();
            try {
                runnable.run();
            } finally {
                MDC.clear();
            }
        };
    }
}
