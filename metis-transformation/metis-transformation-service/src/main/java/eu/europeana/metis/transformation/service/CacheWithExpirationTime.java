package eu.europeana.metis.transformation.service;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import eu.europeana.metis.transformation.service.CacheValueSupplier.CacheValueSupplierException;

/**
 * <p>
 * A cache object that caches objects against a key with an expiration time.
 * </p>
 * <p>
 * The expiration time enforces that an object is not given out by the cache if its expiration time
 * has passed. The expiration time can be changed and takes effect immediately.
 * </p>
 * <p>
 * Another setting is the leniency mode. This indicates whether we allow using a previously obtained
 * version beyond its expiration time if a problem occurred getting the latest version.
 * </p>
 * 
 * @param <K> The key type of the map in the cache
 * @param <V> The value type of the map in the cache
 */
public class CacheWithExpirationTime<K, V> {

  /** The default expiration time. **/
  protected static final Duration DEFAULT_EXPIRATION_TIME = Duration.ZERO.plusHours(1);

  /** The default leniency mode. **/
  protected static final boolean DEFAULT_LENIENCY_MODE = true;

  // This value should be locked.
  private final Map<K, CacheItemWithExpirationTime<V>> cache = new HashMap<>();

  // This value should be locked.
  private Duration expirationTime;

  // This value should be locked.
  private boolean lenientWithReloads;

  private final ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * Constructor.
   * 
   * @param expirationTime The expiration time of the cache. A negative expiration time ensures that
   *        all data will always be reloaded.
   * @param lenientWithReloads Whether or not we apply the lenient mode.
   */
  public CacheWithExpirationTime(Duration expirationTime, boolean lenientWithReloads) {
    if (expirationTime == null) {
      throw new IllegalArgumentException();
    }
    this.expirationTime = expirationTime;
    this.lenientWithReloads = lenientWithReloads;
  }

  /**
   * Constructor using the default expiration time (given by {@link #DEFAULT_EXPIRATION_TIME}) and
   * the default lenient mode (given by {@link #DEFAULT_LENIENCY_MODE}).
   */
  public CacheWithExpirationTime() {
    this(DEFAULT_EXPIRATION_TIME, DEFAULT_LENIENCY_MODE);
  }

  /**
   * Set a new expiration time for this cache. This function will lock the cache for writing.
   * 
   * @param expirationTime The new expiration time. A negative expiration time ensures that all data
   *        will always be reloaded.
   */
  public void setExpirationTime(Duration expirationTime) {
    lock.writeLock().lock();
    try {
      this.expirationTime = expirationTime;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * @return The current expiration time.
   */
  Duration getExpirationTime() {
    lock.readLock().lock();
    try {
      return expirationTime;
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * Set a new leniency mode.
   * 
   * @param lenientWithReloads The new leniency mode.
   */
  public void setLenientWithReloads(boolean lenientWithReloads) {
    lock.writeLock().lock();
    try {
      this.lenientWithReloads = lenientWithReloads;
    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * @return The current leniency mode.
   */
  boolean isLenientWithReloads() {
    lock.readLock().lock();
    try {
      return lenientWithReloads;
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * This method looks up the cache item in the cache, creating the item if it is absent. It returns
   * a function that resolves the value of the cache item. This method will lock the cache for
   * reading and, if the cache item is not present, will then also lock it for writing in order to
   * add it.
   * 
   * @param key The key to look up.
   * @return A function that should be used to obtain the value. The function takes a supplier
   *         (obtaining the value in case the value is absent or expired, and only called if that is
   *         the case).
   */
  private CacheValueResolver<V> getValueResolver(K key) {

    // If we have the item and it is still valid, we return it.
    lock.readLock().lock();
    try {
      if (cache.containsKey(key)) {
        final CacheItemWithExpirationTime<V> cachedItem = cache.get(key);
        return supplier -> cachedItem.getValue(expirationTime, supplier, lenientWithReloads);
      }
    } finally {
      lock.readLock().unlock();
    }

    // We are still here: so either we don't have the item or it is no longer valid.
    lock.writeLock().lock();
    try {

      // Recheck the state: maybe the item was added while we waited for the write lock.
      if (cache.containsKey(key)) {
        final CacheItemWithExpirationTime<V> cachedItem = cache.get(key);
        return supplier -> cachedItem.getValue(expirationTime, supplier, lenientWithReloads);
      }

      // So we need to add the cache item.
      final CacheItemWithExpirationTime<V> result = new CacheItemWithExpirationTime<>();
      cache.put(key, result);
      return supplier -> result.getValue(expirationTime, supplier, lenientWithReloads);

    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Obtain the value for the given key. If the value is not present, or has expired, the supplier
   * is used to get the latest version of the value (and store it in cache).
   * 
   * @param key The key to look up.
   * @param valueSupplier A supplier that can be used to obtain the latest version of the value.
   * @return The value.
   * @throws CacheValueSupplierException In case a problem occurred while obtaining the latest
   *         version of the value.
   */
  public V getFromCache(K key, CacheValueSupplier<V> valueSupplier)
      throws CacheValueSupplierException {
    return getValueResolver(key).apply(valueSupplier);
  }

  /**
   * <p>
   * This method cleans the cache of any items that have not been accessed for a given amount of
   * time (i.e. in the time span given by the parameter). This method could be called by a scheduled
   * cleanup.
   * </p>
   * <p>
   * NOTE: It is theoretically possible that a cached item is being removed that is also currently
   * in the process of being requested. This is considered to be unlikely and warranting neither the
   * extra locking (i.e. performance hit) nor the more complex code that would need to be in place
   * to prevent this. Even if this does occur, the behavior of this cache remains unchanged. The
   * only consequence is that the requested value may need to be reloaded again when it is next
   * requested.
   * </p>
   * 
   * @param since The interval length of the period we want to check (which ends now). A negative
   *        duration cleans everything.
   */
  public void removeItemsNotAccessedSince(Duration since) {
    lock.writeLock().lock();
    try {
      cache.entrySet().removeIf(entry -> !entry.getValue().valueWasAccessedRecently(since));
    } finally {
      lock.writeLock().unlock();
    }
  }

  private static interface CacheValueResolver<V> {
    public V apply(CacheValueSupplier<V> cacheValueSupplier) throws CacheValueSupplierException;
  }
}
