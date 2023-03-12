package io.cruii.component;

import feign.RetryableException;
import feign.Retryer;

import java.util.concurrent.TimeUnit;

public class CruiiRetry implements Retryer {
    @Override
    public void continueOrPropagate(RetryableException e) {
        throw e;
    }

    @Override
    public Retryer clone() {
        return new Default(100, TimeUnit.SECONDS.toMillis(1), 5);
    }
}
