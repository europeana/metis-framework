package eu.europeana.enrichment.api.external.impl;

import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * The type Client entity resolver cache.
 *
 * @param <K> the type parameter
 * @param <V> the type parameter
 */
public class ClientEntityResolverCache<K, V> {

  private final long ttlMillis;
  private final ConcurrentHashMap<K, CacheEntry<V>> cacheMap = new ConcurrentHashMap<>();

  /**
   * Instantiates a new Client entity resolver cache.
   *
   * @param ttlMillis the ttl millis
   */
  public ClientEntityResolverCache(long ttlMillis) {
    this.ttlMillis = ttlMillis;
  }

  /**
   * Put.
   *
   * @param key the key
   * @param value the value
   */
  public void put(K key, V value) {
    long expiryTime = System.currentTimeMillis() + ttlMillis;
    cacheMap.put(key, new CacheEntry<>(value, expiryTime));
  }

  /**
   * Get v.
   *
   * @param key the key
   * @return the v
   */
  public V get(K key) {
    CacheEntry<V> entry = cacheMap.get(key);
    if (entry == null || entry.isExpired()) {
      cacheMap.remove(key); // remove expired entry
      return null;
    }
    return entry.value;
  }

  /**
   * Compute if absent v.
   *
   * @param key the key
   * @param mappingFunction the mapping function
   * @return the v
   */
  public V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
    /*
     * Instead of using synchronized
     * Suppose two threads (A and B) call computeIfAbsent("key") at the same time:
     *   a) Both check and see that "key" is missing or expired.
     *   b) Both compute a new value (say, from a database).
     *   c) Now both try to put their computed value into the cache.
     * Only one will succeed in putIfAbsent. The other gets back the existing value.
     * But — what if the existing value just got replaced by another thread while this thread was still computing?
     * That’s why we re-check the result of putIfAbsent.
     */
    while (true) {
      CacheEntry<V> entry = cacheMap.get(key);
      if (entry != null && !entry.isExpired()) {
        return entry.value; // valid cache hit
      }

      // missing or expired, compute a new value
      V newValue = mappingFunction.apply(key);
      CacheEntry<V> newEntry = new CacheEntry<>(newValue, System.currentTimeMillis() + ttlMillis);
      CacheEntry<V> existing = cacheMap.putIfAbsent(key, newEntry);

      // If another thread inserted a non-expired value, use it
      if (existing == null || existing.isExpired()) {
        // Either we inserted it, or the existing one is expired
        return newValue;
      } else {
        // Another thread already inserted a valid entry — use that
        return existing.value;
      }
    }
  }

  /**
   * Remove.
   *
   * @param key the key
   */
  public void remove(K key) {
    cacheMap.remove(key);
  }

  /**
   * Size int.
   *
   * @return the int
   */
  public int size() {
    return cacheMap.size();
  }

  /**
   * Cleanup.
   */
  public void cleanup() {
    final long now = System.currentTimeMillis();
    for (Entry<K, CacheEntry<V>> entry : cacheMap.entrySet()) {
      if (entry != null && entry.getValue().expiryTime <= now) {
        cacheMap.remove(entry.getKey());
      }
    }
  }

  /**
   * @param value The Value.
   * @param expiryTime The Expiry time.
   */
  private record CacheEntry<V>(V value, long expiryTime) {

    /**
     * Is expired boolean.
     *
     * @return the boolean
     */
    boolean isExpired() {
      return System.currentTimeMillis() > expiryTime;
    }
  }

}
