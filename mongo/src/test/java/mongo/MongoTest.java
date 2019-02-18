package mongo;

import com.github.ixtf.japp.core.J;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.subscribers.ObservableSubscriber;
import com.github.ixtf.persistence.subscribers.OperationSubscriber;
import com.github.ixtf.persistence.subscribers.PrintSubscriber;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.reactivestreams.client.ClientSession;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoCollection;
import com.mongodb.reactivestreams.client.Success;
import lombok.SneakyThrows;
import org.bson.Document;
import org.bson.types.Code;
import org.bson.types.Decimal128;
import org.bson.types.ObjectId;
import org.reactivestreams.Publisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static com.mongodb.client.model.Filters.eq;

/**
 * @author jzb 2019-02-13
 */
public class MongoTest {

    public static void main(String[] args) {
        final MongoClient mongoClient = Jmongo.client();
        System.out.println(mongoClient);

        final MongoCollection<Document> testCollection = Jmongo.collection("test");

        mongoClient.startSession().subscribe(new ObservableSubscriber<>() {
            @Override
            public void onNext(ClientSession clientSession) {
                super.onNext(clientSession);
                final ObservableSubscriber<BulkWriteResult> subscriber = new ObservableSubscriber<>();
                testCollection.bulkWrite(clientSession, Lists.newArrayList()).subscribe(subscriber);
            }
        });
//        testInsert(testCollection);
        testUpdate(testCollection);
        testReplace(testCollection);
        testFind(testCollection);
    }

    @SneakyThrows
    private static void testReplace(MongoCollection<Document> collection) {
        final Collection<Document> array = IntStream.rangeClosed(1, 4).mapToObj(i -> {
            final String value = "test" + i;
            return new Document("test", value);
        }).collect(Collectors.toList());
        final Document replace = new Document("_id", "5c647ff21fbfe4287be8a199")
                .append("replace-test", null)
                .append("cdt", new Date())
                .append("code-test", new Code("code-test"))
                .append("long-test", 1.23)
                .append("bd-test", BigDecimal.valueOf(1.23))
                .append("ld-test", LocalDate.now())
                .append("lt-test", LocalTime.now())
                .append("ldt-test", LocalDateTime.now())
                .append("collection", array);
        final Publisher<UpdateResult> updateResultPublisher = collection.replaceOne(eq("_id", "5c647ff21fbfe4287be8a199"), replace);
        final ObservableSubscriber<UpdateResult> subscriber = new ObservableSubscriber<>() {
            @Override
            public void onNext(UpdateResult updateResult) {
                super.onNext(updateResult);
                System.out.println(updateResult);
            }
        };
        updateResultPublisher.subscribe(subscriber);
        subscriber.await();
    }

    @SneakyThrows
    private static void testUpdate(MongoCollection<Document> collection) {
        final Document set = new Document("update-test", "update-test");
        final Publisher<UpdateResult> updateResultPublisher = collection.updateOne(eq("_id", "5c647ff21fbfe4287be8a199"), new Document("$set", set));
//        final Publisher<UpdateResult> updateResultPublisher = collection.updateOne(eq("_id", "5c647ff21fbfe4287be8a199"), set);
        final ObservableSubscriber<UpdateResult> subscriber = new ObservableSubscriber<>() {
            @Override
            public void onNext(UpdateResult updateResult) {
                super.onNext(updateResult);
                System.out.println(updateResult);
            }
        };
        updateResultPublisher.subscribe(subscriber);
        subscriber.await();
    }

    @SneakyThrows
    private static void testFind(MongoCollection<Document> collection) {
        final ObservableSubscriber<Document> subscriber = new OperationSubscriber<>() {
            @SneakyThrows
            @Override
            public void onNext(Document document) {
                super.onNext(document);
                final Map<Object, Object> result = Maps.newHashMap();
                final LocalDate ld = J.localDate(document.getDate("ld-test"));
                result.put("ld", ld);
                final LocalTime lt = J.localTime(document.getDate("lt-test"));
                result.put("lt", lt);
                final LocalDateTime ldt = J.localDateTime(document.getDate("ldt-test"));
                result.put("ldt", ldt);
                final BigDecimal bd = document.get("bd-test", Decimal128.class).bigDecimalValue();
                result.put("bd", bd);
                final List<Document> list = document.getList("collection", Document.class);
                final String s = MAPPER.writeValueAsString(result);
                System.out.println(s);
            }
        };
        collection.find().subscribe(subscriber);
        subscriber.await();
        System.out.println("end");
    }

    @SneakyThrows
    private static void testInsert(MongoCollection<Document> collection) {
        Document doc = new Document("_id", new ObjectId().toHexString())
                .append("name", "MongoDB")
                .append("type", "database")
                .append("count", 1)
                .append("cdt", new Date())
                .append("ld-test", LocalDate.now())
                .append("lt-test", LocalTime.now())
                .append("ldt-test", LocalDateTime.now())
                .append("info", new Document("x", 203).append("y", 102));
        final Publisher<Success> insertOnePublisher = collection.insertOne(doc);
        final PrintSubscriber subscriber = new PrintSubscriber(Success.SUCCESS.name());
        insertOnePublisher.subscribe(subscriber);
        subscriber.await();
    }
}
