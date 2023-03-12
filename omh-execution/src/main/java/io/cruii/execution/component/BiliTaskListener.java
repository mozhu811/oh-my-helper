package io.cruii.execution.component;

import io.cruii.model.custom.BiliTaskResult;

/**
 * @author cruii
 * Created on 2023/2/24
 */
public interface BiliTaskListener {
    void onCompletion(BiliTaskResult result);
}
