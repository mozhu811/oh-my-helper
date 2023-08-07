package io.cruii.push.pusher;

/**
 * @author cruii
 * Created on 2021/9/23
 */
public interface Pusher {
    void notifyExpired(String id);

    boolean push(String content);
}
