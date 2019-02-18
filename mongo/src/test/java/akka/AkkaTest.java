package akka;

import akka.actor.ActorSystem;
import akka.stream.ActorMaterializer;
import akka.stream.Materializer;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;

/**
 * @author jzb 2019-02-18
 */
public class AkkaTest {
    public static void main(String[] args) {
        final ActorSystem system = ActorSystem.create("QuickStart");
        final Materializer materializer = ActorMaterializer.create(system);

        final Source<Integer, NotUsed> source = Source.range(1, 10);
        source.map(i -> i).runWith(Sink.fold(0, (agg, next) -> {
            System.out.println("stream: " + Thread.currentThread());
            return agg + next;
        }), materializer).whenComplete((i, err) -> {
            System.out.println(i);
            System.out.println("whenComplete: " + Thread.currentThread());
        });
        System.out.println("main: " + Thread.currentThread());
    }
}
