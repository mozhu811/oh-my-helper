package io.cruii.push.service;

/**
 * @author cruii
 * Created on 2022/4/6
 */
public interface PushService {
    boolean push(String dedeuserid, String content);
}
