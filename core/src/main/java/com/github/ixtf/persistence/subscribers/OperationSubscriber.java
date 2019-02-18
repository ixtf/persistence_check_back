package com.github.ixtf.persistence.subscribers;

import org.reactivestreams.Subscription;

/**
 * @author jzb 2019-02-13
 */
public class OperationSubscriber<T> extends ObservableSubscriber<T> {

    @Override
    public void onSubscribe(final Subscription s) {
        super.onSubscribe(s);
        s.request(Integer.MAX_VALUE);
    }
}
