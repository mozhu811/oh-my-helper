package io.cruii.context;


import io.cruii.pojo.po.BilibiliUser;

/**
 * @author cruii
 * Created on 2021/10/05
 */
public class BilibiliUserContext {
    private static final InheritableThreadLocal<BilibiliUser> threadLocal = new InheritableThreadLocal<>();

    private BilibiliUserContext() {
    }

    public static void set(BilibiliUser user) {
        threadLocal.set(user);
    }

    public static BilibiliUser get() {
        return threadLocal.get();
    }

    public static void remove() {
        threadLocal.remove();
    }
}
