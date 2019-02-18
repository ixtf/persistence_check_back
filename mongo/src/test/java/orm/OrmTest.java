package orm;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.subscribers.OperationSubscriber;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mongodb.bulk.BulkWriteResult;
import com.mongodb.client.model.InsertOneModel;
import com.mongodb.client.model.WriteModel;
import com.mongodb.reactivestreams.client.MongoCollection;
import lombok.SneakyThrows;
import org.bson.Document;
import orm.domain.Silk;
import orm.domain.SilkCar;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static com.github.ixtf.japp.core.Constant.MAPPER;
import static java.util.stream.Collectors.toList;

/**
 * @author jzb 2019-02-14
 */
public class OrmTest {
    @SneakyThrows
    public static void main(String[] args) {
        final Optional<SilkCar> silkCar = Jmongo.find(SilkCar.class, "5bfd4b4f67e7ad00013055df");
        ObjectNode objectNode = silkCar.map(it -> MAPPER.convertValue(it, ObjectNode.class))
                .orElseGet(MAPPER::createObjectNode);
        System.out.println(objectNode);

        final Optional<Silk> silk = Jmongo.find(Silk.class, "5bfde010d939c400019343eb");
        objectNode = silk.map(it -> MAPPER.convertValue(it, ObjectNode.class))
                .orElseGet(MAPPER::createObjectNode);
        System.out.println(objectNode);


//        final Optional<Silk> silk = Jmongo.find(Silk.class, "5c0086d751e9c40001574ddc");
//        System.out.println(silk);
//        testTx();
    }

    @SneakyThrows
    private static void testTx() {
        final List<Object> list = Collections.synchronizedList(Lists.newArrayList());
        final ExecutorService executorService = Executors.newFixedThreadPool(10);
        final CountDownLatch countDownLatch = new CountDownLatch(10);
        final Runnable runnable = () -> {
            IntStream.rangeClosed(1, 1000).parallel().forEach(list::add);
            countDownLatch.countDown();
        };
        IntStream.rangeClosed(1, 10).parallel()
                .mapToObj(i -> executorService.submit(runnable))
                .collect(toList());
        countDownLatch.await();

        System.out.println(list.size());

//        final ClientSession clientSession = Jmongo.session();
        final MongoCollection<Document> collection = Jmongo.collection("test");
        final ImmutableList.Builder<WriteModel<Document>> builder = ImmutableList.builder();
        builder.add(new InsertOneModel<>(new Document("_id", "tx-1")));
        builder.add(new InsertOneModel<>(new Document("_id", "tx-1")));
        final OperationSubscriber<BulkWriteResult> subscriber = new OperationSubscriber<>();
        collection.bulkWrite(builder.build()).subscribe(subscriber);
        final List<BulkWriteResult> received = subscriber.getReceived();
        subscriber.await();
        System.out.println(received);
    }

}
