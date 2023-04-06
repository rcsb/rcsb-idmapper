package org.rcsb.idmapper.backend.data.subscribers;

import org.bson.Document;
import org.rcsb.idmapper.backend.Repository;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Subscriber that immediately requests Publisher to start streaming data
 *
 * Created on 3/10/23.
 *
 * TODO: fix @since tag
 * @author Yana Rose
 * @since X.Y.Z
 */
public class CollectionSubscriber implements Subscriber<Document> {

    private final Logger logger = LoggerFactory.getLogger(CollectionSubscriber.class);

    public final String categoryName;
    public final String collectionName;
    public final Repository repository;

    public CollectionSubscriber(final String coll, final String cat, final Repository r) {
        this.collectionName = coll;
        this.categoryName = cat;
        this.repository = r;
    }

    @Override
    public void onSubscribe(final Subscription s) {
        s.request(Long.MAX_VALUE);
    }

    @Override
    public void onNext(Document t) { }

    @Override
    public void onError(Throwable t) {
        logger.error("Processing [ {} ] data from [ {} ] collection returned error: {}",
                categoryName, collectionName, t.getMessage());
        Thread.currentThread().interrupt();
    }

    @Override
    public void onComplete() {
        logger.info("Processed [ {} ] data in documents from [ {} ] collection ",
                categoryName, collectionName);
    }
}
