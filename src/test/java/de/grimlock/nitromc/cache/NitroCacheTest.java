package de.grimlock.nitromc.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.concurrent.atomic.AtomicReference;
import static org.junit.jupiter.api.Assertions.*;

class NitroCacheTest {

    private NitroCache cache;

    @BeforeEach
    void setUp() {
        cache = new NitroCache();
        cache.onEnable();
    }

    @Test
    void testBasicSetGet() {
        cache.set("testKey", "testValue");
        assertEquals("testValue", cache.get("testKey", String.class));
    }

    @Test
    void testAtomicIncrement() {
        long val1 = cache.incr("counter");
        long val2 = cache.incr("counter");
        assertEquals(1, val1);
        assertEquals(2, val2);
    }

    @Test
    void testPubSub() {
        AtomicReference<String> received = new AtomicReference<>();
        cache.subscribe("testChannel", received::set);
        cache.publish("testChannel", "hello");
        assertEquals("hello", received.get());
    }
}
