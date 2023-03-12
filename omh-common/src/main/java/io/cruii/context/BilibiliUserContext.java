package io.cruii.context;


import io.cruii.model.BiliUser;

/**
 * @author cruii
 * Created on 2021/10/05
 */
public class BilibiliUserContext {
    private static final InheritableThreadLocal<BiliUser> threadLocal = new InheritableThreadLocal<>();

    private BilibiliUserContext() {
    }

    public static void set(BiliUser user) {
        threadLocal.set(user);
    }

    public static BiliUser get() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }
}
