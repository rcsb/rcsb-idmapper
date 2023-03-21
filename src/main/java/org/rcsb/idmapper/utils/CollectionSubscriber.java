package org.rcsb.idmapper.utils;

import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Subscriber that immediately requests Publisher to start streaming data
 *
 * @param <T> The publishers result type
 *
 * Created on 3/10/23.
 *
 * TODO: fix @since tag
 * @author Yana Rose
 * @since X.Y.Z
 */
public class CollectionSubscriber<T> extends ObservableSubscriber<T> implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(CollectionSubscriber.class);

    public String categoryName;
    public String collectionName;

    @Override
    public void onSubscribe(final Subscription s) {
        super.onSubscribe(s);
        // TODO do we need to pass an exact number of documents in the collection?
        s.request(Long.MAX_VALUE);
    }

    @Override
    public void run() {
        try {
            while (!isCompleted()) {
                await();
            }
        } catch (Throwable e) {
            logger.error("Error occurred while streaming: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }
}
