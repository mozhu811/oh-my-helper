package io.cruii.task;

/**
 * 任务接口
 *
 * @author cruii
 * Created on 2021/9/15
 */
public interface Task {

    void run() throws Exception;

    String getName();

}
