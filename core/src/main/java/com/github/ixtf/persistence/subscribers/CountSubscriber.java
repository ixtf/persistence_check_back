package com.github.ixtf.persistence.subscribers;

/**
 * @author jzb 2019-02-18
 */
public class CountSubscriber extends OperationSubscriber<Long> {
    public Long getCount() {
        await();
        return getReceived().get(0);
    }
}
