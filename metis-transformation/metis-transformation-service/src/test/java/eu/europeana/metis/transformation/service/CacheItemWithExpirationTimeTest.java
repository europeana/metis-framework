package eu.europeana.metis.transformation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import eu.europeana.metis.transformation.service.CacheValueSupplier.CacheValueSupplierException;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CacheItemWithExpirationTimeTest {

  @Test
  void testIsInstantInInterval() {

    // Spy
    final CacheItemWithExpirationTime<?> cacheItem = spy(new CacheItemWithExpirationTime<>());
    final Instant now = Instant.now();

    // Test with null.
    when(cacheItem.getCreationTime()).thenReturn(null);
    when(cacheItem.getLastAccessTime()).thenReturn(null);
    assertFalse(cacheItem.isInstantInInterval(null, Duration.ZERO.plusHours(1), Instant.now()));

    // Test with actual value.
    final Duration duration = Duration.ZERO.plusSeconds(600);
    assertFalse(cacheItem.isInstantInInterval(now, duration, now.minusSeconds(900)));
    assertFalse(cacheItem.isInstantInInterval(now, duration, now.minusSeconds(600)));
    assertFalse(cacheItem.isInstantInInterval(now, duration, now.minusSeconds(300)));
    assertTrue(cacheItem.isInstantInInterval(now, duration, now));
    assertTrue(cacheItem.isInstantInInterval(now, duration, now.plusSeconds(300)));
    assertTrue(cacheItem.isInstantInInterval(now, duration, now.plusSeconds(600)));
    assertFalse(cacheItem.isInstantInInterval(now, duration, now.plusSeconds(900)));

    // Test with negative duration.
    final Duration negativeDuration = Duration.ZERO.minusSeconds(600);
    assertFalse(cacheItem.isInstantInInterval(now, negativeDuration, now.minusSeconds(900)));
    assertFalse(cacheItem.isInstantInInterval(now, negativeDuration, now.minusSeconds(600)));
    assertFalse(cacheItem.isInstantInInterval(now, negativeDuration, now.minusSeconds(300)));
    assertFalse(cacheItem.isInstantInInterval(now, negativeDuration, now));
    assertFalse(cacheItem.isInstantInInterval(now, negativeDuration, now.plusSeconds(300)));
    assertFalse(cacheItem.isInstantInInterval(now, negativeDuration, now.plusSeconds(600)));
    assertFalse(cacheItem.isInstantInInterval(now, negativeDuration, now.plusSeconds(900)));
  }

  @Test
  void testValueWasAccessedRecently() {

    // Define times
    final Duration duration = Duration.ZERO.plusSeconds(600);
    final Instant time1 = Instant.now();
    final Instant time2 = Instant.now().plusSeconds(300);

    // Spy object
    final CacheItemWithExpirationTime<String> cacheItem = spy(new CacheItemWithExpirationTime<>());
    when(cacheItem.isInstantInInterval(time1, duration, time2)).thenReturn(true);
    when(cacheItem.getLastAccessTime()).thenReturn(time1);
    when(cacheItem.getNow()).thenReturn(time2);

    // Check call.
    assertTrue(cacheItem.valueWasAccessedRecently(duration));
    verify(cacheItem, times(1)).isInstantInInterval(any(), any(), any());
    verify(cacheItem, times(1)).isInstantInInterval(time1, duration, time2);
  }

  @Test
  void testGetValue() throws CacheValueSupplierException {

    // Spy cache item.
    final CacheItemWithExpirationTime<String> cacheItem = spy(new CacheItemWithExpirationTime<>());

    // Check: there is no creation time set.
    assertNull(cacheItem.getCreationTime());

    // Load a value for the first time.
    final Instant time1 = Instant.now();
    when(cacheItem.getNow()).thenReturn(time1);
    when(cacheItem.isInstantInInterval(any(), any(), eq(time1))).thenReturn(false);
    final String testValue1 = "test1";
    final SpyableCacheValueSupplier supplier1 = spy(new SpyableCacheValueSupplier(testValue1));
    final String returnedValue1 = cacheItem.getValue(Duration.ZERO, supplier1, true);
    verify(supplier1, times(1)).get();
    verifyNoMoreInteractions(supplier1);
    assertEquals(testValue1, returnedValue1);
    assertEquals(time1, cacheItem.getCreationTime());
    assertEquals(time1, cacheItem.getLastAccessTime());

    // Get cached value (before expiration)
    final Instant time2 = time1.plusSeconds(1000);
    when(cacheItem.getNow()).thenReturn(time2);
    when(cacheItem.isInstantInInterval(any(), any(), eq(time2))).thenReturn(true);
    final String returnedValue2 = cacheItem.getValue(Duration.ZERO, supplier1, true);
    verifyNoMoreInteractions(supplier1);
    assertEquals(testValue1, returnedValue2);
    assertEquals(time1, cacheItem.getCreationTime());
    assertEquals(time2, cacheItem.getLastAccessTime());

    // Load a new value (after expiration)
    final Instant time3 = time1.plusSeconds(2000);
    when(cacheItem.getNow()).thenReturn(time3);
    when(cacheItem.isInstantInInterval(any(), any(), eq(time3))).thenReturn(false);
    final String testValue2 = "test2";
    final SpyableCacheValueSupplier supplier2 = spy(new SpyableCacheValueSupplier(testValue2));
    final String returnedValue3 = cacheItem.getValue(Duration.ZERO, supplier2, true);
    verify(supplier2, times(1)).get();
    verifyNoMoreInteractions(supplier2);
    assertEquals(testValue2, returnedValue3);
    assertEquals(time3, cacheItem.getCreationTime());
    assertEquals(time3, cacheItem.getLastAccessTime());
  }

  @Test
  void testWithNullValue() throws CacheValueSupplierException {

    // Spy - check that creation time is null.
    final CacheItemWithExpirationTime<String> cacheItem = spy(new CacheItemWithExpirationTime<>());
    final Instant now = Instant.now();
    when(cacheItem.getNow()).thenReturn(now);

    // Load null value.
    when(cacheItem.isInstantInInterval(any(), any(), eq(now))).thenReturn(false);
    final SpyableCacheValueSupplier supplier = spy(new SpyableCacheValueSupplier(null));
    final String returnedValue1 = cacheItem.getValue(Duration.ZERO, supplier, true);
    assertNull(returnedValue1);
    assertEquals(now, cacheItem.getCreationTime());
  }

  @Test
  void testFirstLoadWithExceptionStrict() {
    assertThrows(CacheValueSupplierException.class, () -> new CacheItemWithExpirationTime<String>()
        .getValue(Duration.ZERO, new ValueSupplierWithException(), false));
  }

  @Test
  void testFirstLoadWithExceptionLenient() {
    assertThrows(CacheValueSupplierException.class,
        () -> new CacheItemWithExpirationTime<String>().getValue(Duration.ZERO,
            new ValueSupplierWithException(), true));

  }

  @Test
  void testSecondLoadWithExceptionStrict() throws CacheValueSupplierException {
    final CacheItemWithExpirationTime<String> cacheItem = spy(new CacheItemWithExpirationTime<>());
    when(cacheItem.isInstantInInterval(any(), any(), any())).thenReturn(false);
    cacheItem.getValue(Duration.ZERO, new SpyableCacheValueSupplier("test"), false);
    try {
      cacheItem.getValue(Duration.ZERO, new ValueSupplierWithException(), false);
      fail("");
    } catch (CacheValueSupplierException e) {
      // Nothing to do.
    }
  }

  @Test
  void testSecondLoadWithExceptionLenient() throws CacheValueSupplierException {

    // Create cache item.
    final CacheItemWithExpirationTime<String> cacheItem = spy(new CacheItemWithExpirationTime<>());
    final Instant now = Instant.now();
    when(cacheItem.getNow()).thenReturn(now);
    when(cacheItem.isInstantInInterval(any(), any(), eq(now))).thenReturn(false);

    // First load.
    final String testValue = "test";
    cacheItem.getValue(Duration.ZERO, new SpyableCacheValueSupplier(testValue), false);

    // Second load.
    final Instant secondLoadTime = now.plusSeconds(300);
    when(cacheItem.getNow()).thenReturn(secondLoadTime);
    final String returnedValue =
        cacheItem.getValue(Duration.ZERO, new ValueSupplierWithException(), true);
    assertEquals(testValue, returnedValue);
    assertEquals(secondLoadTime, cacheItem.getCreationTime());
  }

  static class SpyableCacheValueSupplier implements CacheValueSupplier<String> {

    private final String value;

    SpyableCacheValueSupplier(String value) {
      this.value = value;
    }

    @Override
    public String get() {
      return value;
    }
  }

  private class ValueSupplierWithException implements CacheValueSupplier<String> {

    @Override
    public String get() throws CacheValueSupplierException {
      throw new CacheValueSupplierException(null);
    }
  }
}
