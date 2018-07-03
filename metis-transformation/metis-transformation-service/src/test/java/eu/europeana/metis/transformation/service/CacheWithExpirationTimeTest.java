package eu.europeana.metis.transformation.service;

import static org.junit.Assert.assertEquals;
import java.time.Duration;
import org.junit.Test;
import static org.mockito.Mockito.*;
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

    // Variables
    final String testKey1 = "key1";
    final String testValue1 = "test1";
    final String testKey2 = "key2";
    final String testValue2 = "test2";

    // Add first item.
    final SpyableCacheValueSupplier supplier1 = spy(new SpyableCacheValueSupplier(testValue1));
    final String returnedValue1 = cache.getFromCache(testKey1, supplier1);
    assertEquals(testValue1, returnedValue1);
    verify(supplier1, times(1)).get();

    // Add second item.
    final SpyableCacheValueSupplier supplier2 = spy(new SpyableCacheValueSupplier(testValue2));
    final String returnedValue2 = cache.getFromCache(testKey2, supplier2);
    assertEquals(testValue2, returnedValue2);
    verify(supplier2, times(1)).get();

    // Retrieve first item.
    final SpyableCacheValueSupplier supplier3 = spy(new SpyableCacheValueSupplier(testValue1));
    final String returnedValue3 = cache.getFromCache(testKey1, supplier3);
    assertEquals(testValue1, returnedValue3);
    verify(supplier3, never()).get();
    
    // Purge items with large interval (should not remove anything)
    final SpyableCacheValueSupplier supplier4 = spy(new SpyableCacheValueSupplier(testValue1));
    cache.removeItemsNotAccessedSince(Duration.ZERO.plusDays(1));
    final String returnedValue4 = cache.getFromCache(testKey1, supplier4);
    assertEquals(testValue1, returnedValue4);
    verify(supplier4, never()).get();
    
    // Purge items with small interval (should remove everything)
    final SpyableCacheValueSupplier supplier5 = spy(new SpyableCacheValueSupplier(testValue1));
    cache.removeItemsNotAccessedSince(Duration.ZERO.minusMillis(1));
    final String returnedValue5 = cache.getFromCache(testKey1, supplier5);
    assertEquals(testValue1, returnedValue5);
    verify(supplier5, times(1)).get();
  }
}
