package com.github.ixtf.persistence.mongo.api;

import com.github.ixtf.persistence.api.AbstractUnitOfWork;
import com.github.ixtf.persistence.api.EntityConverter;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.reflection.ClassRepresentation;
import com.github.ixtf.persistence.reflection.ClassRepresentations;
import com.github.ixtf.persistence.reflection.FieldRepresentation;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.DeleteOneModel;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.UpdateOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.Document;
import org.reactivestreams.Publisher;

import java.util.*;
import java.util.stream.Stream;

import static com.github.ixtf.persistence.mongo.Jmongo.ID_COL;
import static java.util.stream.Collectors.*;

/**
 * @author jzb 2019-02-18
 */
@Slf4j
public class MongoUnitOfWork extends AbstractUnitOfWork {

    @Override
    synchronized public MongoUnitOfWork commit() {
        Stream<Pair<String, WriteModel<Document>>> stream = Stream.empty();
        stream = Stream.concat(stream, newList.stream().map(this::newListWriteModel));
        stream = Stream.concat(stream, dirtyList.stream().map(this::dirtyListWriteModel));
        stream = Stream.concat(stream, deleteList.stream().map(this::deleteListWriteModel));
        final List<BulkWriteResultSubscriber> subscribers = stream
                .collect(groupingBy(Pair::getKey, LinkedHashMap::new, mapping(Pair::getRight, toList())))
                .entrySet().stream().map(this::bulkWrite)
                .collect(collectingAndThen(toList(), Collections::unmodifiableList));
        checkAck(subscribers);
        return this;
    }

    private void checkAck(List<BulkWriteResultSubscriber> subscribers) {
        final List<BulkWriteResult> bulkWriteResults = subscribers.stream()
                .map(BulkWriteResultSubscriber::getBulkWriteResult)
                .collect(toList());
        log.debug("" + bulkWriteResults);
        // todo 更新的数量是否一致
    }

    private BulkWriteResultSubscriber bulkWrite(Map.Entry<String, List<WriteModel<Document>>> entry) {
        final MongoCollection<Document> collection = Jmongo.collection(entry.getKey());
        final Publisher<BulkWriteResult> publisher = collection.bulkWrite(entry.getValue());
        final BulkWriteResultSubscriber subscriber = new BulkWriteResultSubscriber();
        publisher.subscribe(subscriber);
        return subscriber;
    }

    private Pair<String, WriteModel<Document>> newListWriteModel(Object o) {
        final InsertOneModel<Document> model = new InsertOneModel<>(toDocument(o));
        return Pair.of(collectionName(o), model);
    }

    private Pair<String, WriteModel<Document>> dirtyListWriteModel(Object o) {
//        final ReplaceOneModel<Document> model = new ReplaceOneModel<>(new Document(ID_COL, idValue(o)), toDocument(o));
        final Document document = toDocument(o);
        document.remove(ID_COL);
        final Document $set = new Document("$set", document);
        final UpdateOneModel<Document> model = new UpdateOneModel<>(new Document(ID_COL, idValue(o)), $set);
        return Pair.of(collectionName(o), model);
    }

    private Pair<String, WriteModel<Document>> deleteListWriteModel(Object o) {
        final DeleteOneModel<Document> model = new DeleteOneModel<>(new Document(ID_COL, idValue(o)));
        return Pair.of(collectionName(o), model);
    }

    private String collectionName(Object o) {
        final ClassRepresentation classRepresentation = ClassRepresentations.create(o.getClass());
        return classRepresentation.getTableName();
    }

    @SneakyThrows
    private Object idValue(Object o) {
        final ClassRepresentation classRepresentation = ClassRepresentations.create(o.getClass());
        final Optional<FieldRepresentation> id = classRepresentation.getId();
        final String nativeFieldName = id.map(FieldRepresentation::getFieldName).get();
        return PropertyUtils.getProperty(o, nativeFieldName);
    }

    private Document toDocument(Object o) {
        final ClassRepresentation classRepresentation = ClassRepresentations.create(o);
        if (!classRepresentation.hasId()) {
            throw new RuntimeException("Class[" + o.getClass() + "]，id不存在");
        }
        final EntityConverter entityConverter = DocumentEntityConverter.get(o.getClass());
        return entityConverter.toDbData(new Document(), o);
    }

    @Override
    synchronized public MongoUnitOfWork rollback() {
        // todo mongo 需要集群才支持事务，后续实现
        return this;
    }
}
