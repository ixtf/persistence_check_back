package netty;

import reactor.core.publisher.Flux;
import reactor.netty.http.server.HttpServer;

import java.time.Duration;

/**
 * @author jzb 2019-02-16
 */
public class NettyTest {
    public static void main(String[] args) {
        HttpServer.create().handle((req, res) -> res.sendWebsocket((i, o) ->
                o.options(opt -> opt.flushOnEach()).sendString(
                        Flux.just("test")
                                .delayElements(Duration.ofMillis(100))
                                .repeat()
                )))
                .host("127.0.0.1")
                .port(8080)
                .bind();
    }
}
