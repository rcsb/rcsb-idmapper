package org.rcsb.idmapper.backend.data.task;

import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoDatabase;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import org.bson.Document;
import org.rcsb.common.constants.ContentType;
import org.rcsb.common.constants.IdentifierRegex;
import org.rcsb.idmapper.backend.data.Repository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.mongodb.client.model.Projections.*;

/**
 *
 * Created on 3/10/23.
 *
 * @author Yana Rose
 */
public abstract class CollectionTask {

    private final Logger logger = LoggerFactory.getLogger(CollectionTask.class);

    public final Repository repository;
    public final String collectionName;
    public List<String> includeFields;

    public CollectionTask(final String coll, final Repository r) {
        this.repository = r;
        this.collectionName = coll;
    }

    void setIncludeFields(final List<String> include) {
        includeFields = include;
    }

    private List<String> getIncludeFields() {
        if (includeFields == null)
            includeFields = List.of();
        return includeFields;
    }

    ContentType getStructureType(String entryId) {
        boolean isCsm = ! IdentifierRegex.PDB_ID_REGEX.matcher(entryId).matches();
        return isCsm ? ContentType.computational : ContentType.experimental;
    }

    public Flowable<Runnable> createFlowable(final MongoDatabase db) {
        FindPublisher<Document> publisher = db.getCollection(collectionName)
                .find()
                .projection(fields(excludeId(), include(getIncludeFields())));
        return Flowable.fromPublisher(publisher)
                .subscribeOn(Schedulers.io())
                .doOnError(t -> logger.error(t.getMessage()))
                .doOnComplete(() -> logger.info("Processed documents from [ {} ] collection ", collectionName))
                .map(this::createRunnable);
    }

    abstract Runnable createRunnable(Document d);
}
