package org.rcsb.idmapper.backend.data.subscribers;

import org.rcsb.idmapper.backend.Repository;
import org.reactivestreams.Subscriber;
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
public class CollectionSubscriber<T> implements Subscriber<T> {

    private final Logger logger = LoggerFactory.getLogger(CollectionSubscriber.class);

    public final String categoryName;
    public final String collectionName;
    public final Repository repository;

    private Long documentCount;

    public CollectionSubscriber(final String coll, final String cat, final Repository r) {
        this.collectionName = coll;
        this.categoryName = cat;
        this.repository = r;
    }

    @Override
    public void onSubscribe(final Subscription s) {
        s.request(documentCount);
    }

    public void setDocumentCount(Long documentCount) {
        this.documentCount = documentCount;
    }

    @Override
    public void onNext(T t) { }

    @Override
    public void onError(Throwable t) {
        logger.error("Processing [ {} ] data from [ {} ] collection returned error: {}",
                categoryName, collectionName, t.getMessage());
        Thread.currentThread().interrupt();
    }

    @Override
    public void onComplete() {
        logger.info("Processed [ {} ] data in [ {} ] documents from [ {} ] collection ",
                categoryName, documentCount, collectionName);
    }
}
