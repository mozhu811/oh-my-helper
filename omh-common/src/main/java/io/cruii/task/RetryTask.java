package io.cruii.task;

import java.util.concurrent.Callable;

/**
 * @author cruii
 * Created on 2021/10/11
 */
public class RetryTask implements Callable<Boolean> {
    private final Task task;

    public RetryTask(Task task) {
        this.task = task;
    }

    @Override
    public Boolean call() throws Exception {
        task.run();
        return true;
    }
}
