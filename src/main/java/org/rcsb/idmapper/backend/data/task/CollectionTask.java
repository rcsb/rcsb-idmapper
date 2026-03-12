package org.rcsb.idmapper.backend.data.task;

import com.mongodb.reactivestreams.client.MongoDatabase;
import org.bson.Document;
import org.rcsb.common.constants.ContentType;
import org.rcsb.common.constants.MongoCollections;
import org.rcsb.idmapper.backend.data.Repository;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Map;

import static com.mongodb.client.model.Projections.*;

/**
 * Base class for tasks that operate on collections in the database.
 * <p>
 * Provides common functionality for collection-based tasks, including
 * repository access, collection name, and field inclusion configuration.
 * <p>
 * Created on 3/10/23.
 *
 * @author Yana Rose
 */
public abstract class CollectionTask {

    private final Logger logger = LoggerFactory.getLogger(CollectionTask.class);

    private static final Map<String, ContentType> COLL_NAME_TO_CONTENT_TYPE_MAP = Map.of(
            MongoCollections.COLL_PDBX_CORE_ENTRY, ContentType.experimental,
            MongoCollections.COLL_PDBX_CORE_POLYMER_ENTITY, ContentType.experimental,
            MongoCollections.COLL_PDBX_CORE_NONPOLYMER_ENTITY, ContentType.experimental,
            MongoCollections.COLL_PDBX_CORE_BRANCHED_ENTITY, ContentType.experimental,
            MongoCollections.COLL_PDBX_COMP_MODEL_CORE_ENTRY, ContentType.computational,
            MongoCollections.COLL_PDBX_COMP_MODEL_CORE_POLYMER_ENTITY, ContentType.computational
    );

    public final Repository repository;
    public final String collectionName;
    public final List<String> includeFields;
    public final ContentType structureType;

    public CollectionTask(final String coll, final Repository r, @Nonnull List<List<String>> fieldsToInclude) {
        this.repository = r;
        this.collectionName = coll;
        this.structureType = getStructureContentType(coll);
        this.includeFields = fieldsToInclude.stream()
                .map(f -> String.join(".", f))
                .toList();
    }

    private static ContentType getStructureContentType(String collectionName) {
        return COLL_NAME_TO_CONTENT_TYPE_MAP.get(collectionName);
    }

    public Flux<Runnable> findDocuments(final MongoDatabase db) {
        Publisher<Document> publisher = db.getCollection(collectionName)
                .find()
                .projection(fields(excludeId(), include(includeFields)));
        return Flux.from(publisher)
                .doOnSubscribe(s -> logger.info("Subscribed document task to collection [ {} ]", collectionName))
                //TODO replace with async debug or remove entirely before prod
//                .doOnNext(d -> logger.info("Processing document from [ {} ]", collectionName))
                .doOnError(t -> logger.error(t.getMessage()))
                .doOnComplete(() -> logger.info("Processed documents from [ {} ] collection ", collectionName))
                .map(this::createDocumentRunnable);
    }

    public Mono<Runnable> countDocuments(final MongoDatabase db) {
        return Mono.from(db.getCollection(collectionName).countDocuments())
                .doOnSubscribe(s -> logger.info("Subscribed count task to collection [ {} ]", collectionName))
                .doOnSuccess(count -> logger.info("Collection [ {} ] has [ {} ] documents", collectionName, count))
                .doOnError(t -> logger.error(t.getMessage()))
                .map(this::createCountRunnable);
    }

    abstract Runnable createDocumentRunnable(Document d);

    public Runnable createCountRunnable(Long count) {
        return () -> repository.addCount(collectionName, count);
    }
}
