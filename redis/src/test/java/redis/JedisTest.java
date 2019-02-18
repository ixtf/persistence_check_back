package redis;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author jzb 2019-02-13
 */
public class JedisTest {
    private static final JedisPool JEDIS_POOL = new JedisPool(new JedisPoolConfig(), "localhost");

    public static void main(String[] args) {
        try (Jedis jedis = JEDIS_POOL.getResource()) {
//            final List<String> strings = jedis.pubsubChannels("SilkBarcodePrinter-*");
//            System.out.println(strings);
//            final Map<String, String> map = jedis.pubsubNumSub("SilkBarcodePrinter-*");
//            System.out.println(map);
//            jedis.hset("test", ImmutableMap.of("key2", "key2-update", "key4", "key4"));
//            System.out.println(jedis);

            jedis.set("ttl-test", "3");
            final Long incr = jedis.incr("ttl-test");
            System.out.println(incr);
            final String s = jedis.get("ttl-test");
            System.out.println(s);
            final Long ttl = jedis.ttl("ttl-test");
            System.out.println(ttl);
        }
        System.out.println("end");
    }
}