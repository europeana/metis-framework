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

  final int maxEntries = 3;
  ClientEntityResolverCache<String, String> cache;

  @BeforeEach
  void setUp() {
    cache = new ClientEntityResolverCache<>(maxEntries);
  }

  @Test
  void testPutAndGet() {
    cache.put("key1", "value1");
    assertEquals("value1", cache.get("key1"));
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
  void testStats() {
    AtomicInteger counter = new AtomicInteger(0);
    String result1 = cache.computeIfAbsent("k1", key -> {
      counter.incrementAndGet();
      return "computed1";
    });

    String result2 = cache.computeIfAbsent("k2", key -> {
      counter.incrementAndGet();
      return "computed2";
    });

    String result3 = cache.computeIfAbsent("k3", key -> {
      counter.incrementAndGet();
      return "computed3";
    });

    assertEquals("computed1", result1);
    assertEquals("computed2", result2);
    assertEquals("computed3", result3);
    assertEquals(3, counter.get());

    result1 = cache.computeIfAbsent("k1", key -> {
      counter.incrementAndGet();
      return "new-computed";
    });

    assertEquals("computed1", result1); // should still return original
    assertEquals(3, counter.get());

    assertEquals(1, cache.stats().hitCount());
    assertEquals(3, cache.stats().missCount());

  }
}
