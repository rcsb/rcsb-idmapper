package org.rcsb.idmapper.backend.data;

import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoDatabase;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.bson.Document;
import org.rcsb.idmapper.utils.CollectionSubscriber;

import static com.mongodb.client.model.Projections.*;

/**
 * Created on 3/21/23.
 * TODO: fix @since tag
 *
 * @author Yana Rose
 * @since X.Y.Z
 */
public class CollectionFetcher implements Runnable {

    private final MongoDatabase db;
    private final CollectionSubscriber<Document> worker;

    public CollectionFetcher(CollectionSubscriber<Document> s, MongoDatabase db) {
        this.worker = s;
        this.db = db;
    }

    @Override
    public void run() {
        FindPublisher<Document> publisher = db.getCollection(worker.collectionName)
                .find()
                .projection(fields(excludeId(), include(worker.categoryName)));
        Flowable.fromPublisher(publisher)
                .subscribeOn(Schedulers.io())
                .subscribe(worker);
        worker.run();
    }
}
