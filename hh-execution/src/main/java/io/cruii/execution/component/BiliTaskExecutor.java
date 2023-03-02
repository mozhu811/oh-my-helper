package io.cruii.execution.component;

import io.cruii.util.ThreadMdcUtil;
import org.jetbrains.annotations.NotNull;
import org.slf4j.MDC;

import java.util.HashSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author cruii
 * Created on 2021/9/24
 */
public final class BiliTaskExecutor extends ThreadPoolExecutor {
    private final HashSet<String> submittedTasks;

    public BiliTaskExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
        submittedTasks = new HashSet<>();
    }

    @Override
    public void execute(@NotNull Runnable command) {
        if (command instanceof BiliTask) {
            BiliTask biliTask = (BiliTask) command;
            String dedeuserid = biliTask.getConfig().getDedeuserid();
            synchronized (submittedTasks) {
                if (submittedTasks.contains(dedeuserid)) {
                    throw new RejectedExecutionException("Task with dedeuserid " + dedeuserid + " already exists in the queue");
                }
                submittedTasks.add(dedeuserid);
            }
        }
        super.execute(ThreadMdcUtil.wrap(command, MDC.getCopyOfContextMap()));
    }

    @Override
    protected void afterExecute(Runnable r, Throwable t) {
        if (r instanceof BiliTask) {
            BiliTask biliTask = (BiliTask) r;
            String dedeuserid = biliTask.getConfig().getDedeuserid();
            synchronized (submittedTasks) {
                submittedTasks.remove(dedeuserid);
            }
        }
        super.afterExecute(r, t);
    }
}
