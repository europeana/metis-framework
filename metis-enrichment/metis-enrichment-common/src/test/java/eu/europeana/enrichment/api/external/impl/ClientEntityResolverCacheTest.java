package eu.europeana.enrichment.api.external.impl;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * The type Client entity resolver cache test.
 */
class ClientEntityResolverCacheTest {

  final long ttlMillis = 1000;
  ClientEntityResolverCache<String, String> cache;

  @BeforeEach
  void setUp() {
    cache = new ClientEntityResolverCache<>(ttlMillis);
  }

  @Test
  void testPutAndGet() {
    cache.put("key1", "value1");
    assertEquals("value1", cache.get("key1"));
  }

  @Test
  void testTTLExpiration() {
    cache.put("key2", "value2");
    // Wait until the entry is expired
    await().atMost(Duration.ofSeconds(2))
           .pollInterval(Duration.ofMillis(100))
           .until(() -> cache.get("key2") == null);

    assertNull(cache.get("key2"), "Expected entry to expire");
  }

  @Test
  void testComputeIfAbsent_CachesValue() {
    AtomicInteger counter = new AtomicInteger(0);
    String result = cache.computeIfAbsent("k1", key -> {
      counter.incrementAndGet();
      return "computed";
    });

    assertEquals("computed", result);
    assertEquals(1, counter.get());

    // Fetch again; should not recompute
    result = cache.computeIfAbsent("k1", key -> {
      counter.incrementAndGet();
      return "new-computed";
    });

    assertEquals("computed", result); // should still return original
    assertEquals(1, counter.get());
  }

  @Test
  void testComputeIfAbsent_RecomputesAfterTTL() {
    AtomicInteger counter = new AtomicInteger(0);
    String result = cache.computeIfAbsent("k2", key -> {
      counter.incrementAndGet();
      return "value1";
    });

    assertEquals("value1", result);
    assertEquals(1, counter.get());

    await().atMost(Duration.ofSeconds(2))
           .pollInterval(Duration.ofMillis(100))
           .until(() -> cache.get("k2") == null);

    result = cache.computeIfAbsent("k2", key -> {
      counter.incrementAndGet();
      return "value2";
    });

    assertEquals("value2", result); // should return new value
    assertEquals(2, counter.get());
  }

  @Test
  void testRemove() {
    cache.put("key3", "value3");
    assertEquals("value3", cache.get("key3"));
    cache.remove("key3");
    assertNull(cache.get("key3"));
  }

  @Test
  void testSize() {
    cache.put("a", "1");
    cache.put("b", "2");
    assertEquals(2, cache.size());
    cache.remove("a");
    assertEquals(1, cache.size());
  }

}
