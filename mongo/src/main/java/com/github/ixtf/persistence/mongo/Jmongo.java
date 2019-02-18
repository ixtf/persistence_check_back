package com.github.ixtf.persistence.mongo;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.persistence.api.EntityConverter;
import com.github.ixtf.persistence.mongo.api.DocumentEntityConverter;
import com.github.ixtf.persistence.mongo.spi.MongoProvider;
import com.github.ixtf.persistence.reflection.ClassRepresentation;
import com.github.ixtf.persistence.reflection.ClassRepresentations;
import com.github.ixtf.persistence.subscribers.CountSubscriber;
import com.github.ixtf.persistence.subscribers.EntitySubscriber;
import com.github.ixtf.persistence.subscribers.OperationSubscriber;
import com.mongodb.reactivestreams.client.ClientSession;
import com.mongodb.reactivestreams.client.FindPublisher;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.reactivestreams.Publisher;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.in;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-02-14
 */
@Slf4j
public class Jmongo {
    private static final MongoClientProviderRegistry MONGO_PROVIDERS = new MongoClientProviderRegistry();

    public static <T> Optional<T> find(Class<T> entityClass, Object id) {
        final Bson condition = eq("_id", id);
        return query(entityClass, condition).findFirst();
    }

    public static <T> Stream<T> list(Class<T> entityClass, Iterable ids) {
        final Bson condition = in("_id", ids);
        return query(entityClass, condition);
    }

    public static <T> Stream<T> listAll(Class<T> entityClass) {
        return query(entityClass, null);
    }

    public static <T> Stream<T> query(Class<T> entityClass, Bson condition) {
        return query(null, entityClass, condition, 0, Integer.MAX_VALUE);
    }

    public static Long count(Class entityClass, Bson condition) {
        final MongoCollection<Document> collection = collection(entityClass);
        final Publisher<Long> publisher = condition == null ? collection.countDocuments() : collection.countDocuments(condition);
        final CountSubscriber subscriber = new CountSubscriber();
        publisher.subscribe(subscriber);
        return subscriber.getCount();
    }

    public static <T> Stream<T> query(Class<T> entityClass, Bson condition, int skip, int limit) {
        return query(null, entityClass, condition, skip, limit);
    }

    private static <T> Stream<T> query(ClientSession session, Class<T> entityClass, Bson condition, int skip, int limit) {
        final MongoCollection<Document> collection = collection(entityClass);
        final FindPublisher<Document> publisher;
        if (session == null) {
            publisher = condition == null ? collection.find() : collection.find(condition);
        } else {
            publisher = condition == null ? collection.find(session) : collection.find(session, condition);
        }
        final EntityConverter entityConverter = DocumentEntityConverter.get(entityClass);
        final EntitySubscriber<T, Document> subscriber = new EntitySubscriber(entityClass, entityConverter);
        publisher.skip(skip).limit(limit).subscribe(subscriber);
        return subscriber.entities();
    }

    public static MongoClient client() {
        return MONGO_PROVIDERS.get().client();
    }

    public static MongoCollection<Document> collection(Class entityClass) {
        final ClassRepresentation classRepresentation = ClassRepresentations.create(entityClass);
        return collection(classRepresentation.getTableName());
    }

    public static MongoCollection<Document> collection(String name) {
        return MONGO_PROVIDERS.get().collection(name);
    }

    public static ClientSession session() {
        final OperationSubscriber<ClientSession> subscriber = new OperationSubscriber<>();
        client().startSession().subscribe(subscriber);
        subscriber.await();
        return subscriber.getReceived().get(0);
    }

    private static class MongoClientProviderRegistry {
        private volatile List<MongoProvider> mongoProviders;

        private MongoProvider get() {
            if (mongoProviders == null) {
                load();
            }
            if (J.isEmpty(mongoProviders)) {
                throw new RuntimeException("没有 MongoProvider");
            }
            if (mongoProviders.size() == 1) {
                return mongoProviders.get(0);
            }
            throw new RuntimeException("多个 MongoProvider");
        }

        synchronized private void load() {
            if (mongoProviders == null) {
                mongoProviders = StreamSupport.stream(ServiceLoader.load(MongoProvider.class).spliterator(), false)
                        .collect(collectingAndThen(toList(), Collections::unmodifiableList));
            }
        }
    }
}
