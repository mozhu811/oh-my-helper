package io.cruii.execution.component;

import org.jetbrains.annotations.NotNull;

import java.util.HashSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author cruii
 * Created on 2021/9/24
 */
public final class BiliTaskExecutor extends ThreadPoolExecutor {

    private final HashSet<String> submittedTasks;

    public BiliTaskExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, @NotNull TimeUnit unit, @NotNull BlockingQueue<Runnable> workQueue, String threadNamePrefix) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, new CustomThreadFactory(threadNamePrefix));
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
        super.execute(command);
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

    private static class CustomThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;

        public CustomThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }

        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }
}
