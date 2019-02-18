package orm;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.github.ixtf.persistence.mongo.Jmongo;
import com.github.ixtf.persistence.mongo.api.MongoUnitOfWork;
import lombok.SneakyThrows;
import orm.domain.Silk;
import orm.domain.SilkCar;
import orm.domain.SilkCarRecord;

import java.util.Optional;

import static com.github.ixtf.japp.core.Constant.MAPPER;

/**
 * @author jzb 2019-02-14
 */
public class OrmTest {
    @SneakyThrows
    public static void main(String[] args) {
//        readTest();
        writeTest();

        Thread.sleep(10000);
    }

    private static void writeTest() {
        final SilkCarRecord silkCarRecord = Jmongo.find(SilkCarRecord.class, "5bfde010d939c40001934416").get();
        final SilkCar silkCar = silkCarRecord.getSilkCar();

//        final SilkCar silkCar = Jmongo.find(SilkCar.class, "5bfd4b4f67e7ad00013055df").get();
        silkCar.setCol(6);
        final MongoUnitOfWork uow = Jmongo.uow();
        uow.registerDirty(silkCar).commit();
    }

    private static void readTest() {
        final Optional<SilkCar> silkCar = Jmongo.find(SilkCar.class, "5bfd4b4f67e7ad00013055df");
        ObjectNode objectNode = silkCar.map(it -> MAPPER.convertValue(it, ObjectNode.class))
                .orElseGet(MAPPER::createObjectNode);
        System.out.println(objectNode);

        final Optional<Silk> silk = Jmongo.find(Silk.class, "5bfde010d939c400019343eb");
        objectNode = silk.map(it -> MAPPER.convertValue(it, ObjectNode.class))
                .orElseGet(MAPPER::createObjectNode);
        System.out.println(objectNode);

        final Optional<SilkCarRecord> silkCarRecord = Jmongo.find(SilkCarRecord.class, "5bfde010d939c40001934416");
        objectNode = silkCarRecord.map(it -> MAPPER.convertValue(it, ObjectNode.class))
                .orElseGet(MAPPER::createObjectNode);
        System.out.println(objectNode);
    }

}
