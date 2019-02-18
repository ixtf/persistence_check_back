package com.github.ixtf.persistence.mongo.api;

import com.github.ixtf.persistence.subscribers.OperationSubscriber;
import com.mongodb.bulk.BulkWriteResult;

/**
 * @author jzb 2019-02-18
 */
public class BulkWriteResultSubscriber extends OperationSubscriber<BulkWriteResult> {

    public BulkWriteResult getBulkWriteResult() {
        await();
        final Throwable error = getError();
        if (error != null) {
            throw new RuntimeException(error);
        }
        final BulkWriteResult bulkWriteResult = getReceived().get(0);
        return bulkWriteResult;
    }
}
