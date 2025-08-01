package eu.europeana.enrichment.api.external.impl;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import java.util.function.Function;

/**
 * The type Client entity resolver cache.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 */
public class ClientEntityResolverCache<K, V> {

  private final Cache<K, V> cacheMap;

  /**
   * Instantiates a new Client entity resolver cache.
   *
   * @param maxEntries the max entries
   */
  public ClientEntityResolverCache(int maxEntries) {
    cacheMap = Caffeine.newBuilder()
                       .maximumSize(maxEntries)
                       .recordStats()
                       .build();
  }

  /**
   * Put.
   *
   * @param key the key
   * @param value the value
   */
  public void put(K key, V value) {
    cacheMap.put(key, value);
  }

  /**
   * Get v.
   *
   * @param key the key
   * @return the v
   */
  public V get(K key) {
    return cacheMap.getIfPresent(key);
  }

  /**
   * Compute if absent v.
   *
   * @param key the key
   * @param mappingFunction the mapping function
   * @return the v
   */
  public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
    return cacheMap.get(key, mappingFunction);
  }

  /**
   * Stats cache stats.
   *
   * @return the cache stats
   */
  public CacheStats stats() {
    return cacheMap.stats();
  }
}
