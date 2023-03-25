package org.rcsb.idmapper.backend.data;

import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoDatabase;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.bson.Document;
import org.rcsb.idmapper.backend.data.subscribers.CollectionSubscriber;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

import static com.mongodb.client.model.Projections.*;

/**
 * Created on 3/21/23.
 * TODO: fix @since tag
 *
 * @author Yana Rose
 * @since X.Y.Z
 */
public class CollectionFetcher implements Callable<Observable<Document>> {

    private final Logger logger = LoggerFactory.getLogger(CollectionFetcher.class);

    private final MongoDatabase db;
    private final CollectionSubscriber<Document> worker;

    public CollectionFetcher(CollectionSubscriber<Document> s, MongoDatabase db) {
        this.worker = s;
        this.db = db;
    }

    private Long countDocuments() {
        final Long[] docCount = new Long[1];
        Publisher<Long> p = db.getCollection(worker.collectionName).countDocuments();
        p.subscribe(new Subscriber<>() {
            @Override
            public void onSubscribe(Subscription subscription) {
                subscription.request(1);
            }
            @Override
            public void onNext(Long count) {
                docCount[0] = count;
            }
            @Override
            public void onError(Throwable t) {
                logger.error("Documents count in [ {} ] returned error: {}", worker.collectionName, t.getMessage());
                Thread.currentThread().interrupt();
            }
            @Override
            public void onComplete() {
                logger.info("Documents count in [ {} ] returned [ {} ]", worker.collectionName, docCount[0]);
            }
        });
        Observable.fromPublisher(p).subscribeOn(Schedulers.io()).blockingSubscribe();
        return docCount[0];
    }

    @Override
    public Observable<Document> call() {
        FindPublisher<Document> publisher = db.getCollection(worker.collectionName)
                .find()
                .projection(fields(excludeId(), include(worker.categoryName)));
        worker.setDocumentCount(countDocuments());
        publisher.subscribe(worker);
        return Observable.fromPublisher(publisher)
                .subscribeOn(Schedulers.io());
    }
}
