package eu.europeana.metis.transformation.service;

import static org.junit.Assert.assertEquals;
import java.time.Duration;
import org.junit.Test;
import eu.europeana.metis.transformation.service.CacheItemWithExpirationTimeTest.SpyableCacheValueSupplier;
import eu.europeana.metis.transformation.service.CacheValueSupplier.CacheValueSupplierException;

public class CacheWithExpirationTimeTest {

  @Test
  public void testConstruction() {

    // Constructor with arguments
    final Duration expirationTime = CacheWithExpirationTime.DEFAULT_EXPIRATION_TIME.plusHours(1);
    final boolean lenientWithReloads = !CacheWithExpirationTime.DEFAULT_LENIENCY_MODE;
    final CacheWithExpirationTime<String, String> cache1 =
        new CacheWithExpirationTime<>(expirationTime, lenientWithReloads);
    assertEquals(expirationTime, cache1.getExpirationTime());
    assertEquals(lenientWithReloads, cache1.isLenientWithReloads());

    // Constructor without arguments
    final CacheWithExpirationTime<String, String> cache2 = new CacheWithExpirationTime<>();
    assertEquals(CacheWithExpirationTime.DEFAULT_EXPIRATION_TIME, cache2.getExpirationTime());
    assertEquals(CacheWithExpirationTime.DEFAULT_LENIENCY_MODE, cache2.isLenientWithReloads());

    // Change the values
    cache2.setExpirationTime(expirationTime);
    cache2.setLenientWithReloads(lenientWithReloads);
    assertEquals(expirationTime, cache2.getExpirationTime());
    assertEquals(lenientWithReloads, cache2.isLenientWithReloads());
  }

  @Test
  public void testGetFromCache() throws CacheValueSupplierException {

    final CacheWithExpirationTime<String, String> cache = new CacheWithExpirationTime<>();

    final String testKey1 = "key1";
    final String testValue1 = "test1";
    final String testKey2 = "key2";
    final String testValue2 = "test2";

    final String returnedValue1 =
        cache.getFromCache(testKey1, new SpyableCacheValueSupplier(testValue1));
    assertEquals(testValue1, returnedValue1);
    final String returnedValue2 =
        cache.getFromCache(testKey2, new SpyableCacheValueSupplier(testValue2));
    assertEquals(testValue2, returnedValue2);
    final String returnedValue3 =
        cache.getFromCache(testKey1, new SpyableCacheValueSupplier(testValue1));
    assertEquals(testValue1, returnedValue3);

  }
}
