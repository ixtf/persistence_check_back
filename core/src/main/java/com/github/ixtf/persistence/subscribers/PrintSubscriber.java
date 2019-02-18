package com.github.ixtf.persistence.subscribers;

/**
 * @author jzb 2019-02-13
 */
public class PrintSubscriber<T> extends OperationSubscriber<T> {
    private final String message;

    public PrintSubscriber(final String message) {
        this.message = message;
    }

    @Override
    public void onComplete() {
        super.onComplete();
        System.out.println(String.format(message, getReceived()));
    }
}
