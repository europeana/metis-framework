package eu.europeana.metis.transformation.service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.metis.transformation.service.CacheValueSupplier.CacheValueSupplierException;

/**
 * This class represents a cache item.
 *
 * @param <V> The type of the value.
 */
class CacheItemWithExpirationTime<V> {

  private static final Logger LOGGER = LoggerFactory.getLogger(CacheItemWithExpirationTime.class);

  private V value;
  private Instant creationTime = null;
  private ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * Obtains the value. This method will lock the cache for reading and, if the value is not
   * present, will then also lock it for writing in order to obtain it.
   * 
   * @param expirationTime The expiration time to be applied.
   * @param supplier A supplier that can be used to obtain the latest version of the value. Note
   *        that null will be considered a legitimate value (and not a failed supply).
   * @param lenientWithReloads Whether or not we are in lenient mode. If true, this method will
   *        return a previously obtained value even if it has expired in the case that we have
   *        problems obtaining the latest value. If we have no previously obtained value this mode
   *        will have no effect.
   * @return The value.
   * @throws CacheValueSupplierException In case the value could not be obtained and we are either
   *         not in lenient mode or no previous version exists to use instead.
   */
  public V getValue(Duration expirationTime, CacheValueSupplier<V> supplier,
      boolean lenientWithReloads) throws CacheValueSupplierException {

    // If we have the item and it is still valid, we return it.
    lock.readLock().lock();
    try {
      if (valueHasNotExpired(expirationTime)) {
        return value;
      }
    } finally {
      lock.readLock().unlock();
    }

    // We are still here: so either we don't have the item or it is no longer valid.
    lock.writeLock().lock();
    try {

      // Recheck the state: maybe the item was added while we waited for the write lock.
      if (valueHasNotExpired(expirationTime)) {
        return value;
      }

      // So we need to obtain the item. If something goes wrong and we have a previous version, use
      // that only if we are in lenient mode.
      try {
        value = supplier.get();
      } catch (CacheValueSupplierException e) {
        if (creationTime == null || !lenientWithReloads) {
          throw e;
        } else {
          LOGGER.warn("Could not obtain value for caching.", e);
        }
      }

      // Always set the creation time (in lenient mode we don't want to try often).
      creationTime = getNow();

      // Done.
      return value;

    } finally {
      lock.writeLock().unlock();
    }
  }

  /**
   * Determines whether the value in this cache item has expired given the provided expiration time
   * (i.e. the age of the data that we no longer accept). If a cache item was never loaded it will
   * be seen as expired.
   * 
   * @param expirationTime The expiration time interval.
   * @return Whether or not this item is expired.
   */
  boolean valueHasNotExpired(Duration expirationTime) {
    return getCreationTime() != null
        && getCreationTime().plus(expirationTime).isAfter(getNow());
  }

  /**
   * @return The creation time of the current value, or null if the value has not been loaded yet.
   */
  Instant getCreationTime() {
    lock.readLock().lock();
    try {
      return creationTime;
    } finally {
      lock.readLock().unlock();
    }
  }

  /**
   * @return An instant representing the current time.
   */
  Instant getNow() {
    return Instant.now();
  }
}
