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

  // Locked: change only with write lock.
  private V value;

  // Locked: change only with write lock.
  private Instant creationTime = null;

  // No need to be locked: is independent of other data.
  private Instant lastAccessTime = null;

  private ReadWriteLock lock = new ReentrantReadWriteLock();

  /**
   * Obtains the value. This method will lock the cache for reading and, if the value is not
   * present, will then also lock it for writing in order to obtain it.
   * 
   * @param expirationTime The expiration time to be applied. A negative duration time always
   *        prompts a reload.
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

    // Mark this access.
    lastAccessTime = getNow();

    // If we have the item and it is still valid, we return it.
    lock.readLock().lock();
    try {
      if (isInstantInInterval(creationTime, expirationTime, getNow())) {
        return value;
      }
    } finally {
      lock.readLock().unlock();
    }

    // We are still here: so either we don't have the item or it is no longer valid.
    lock.writeLock().lock();
    try {

      // Recheck the state: maybe the item was loaded while we waited for the write lock.
      if (isInstantInInterval(creationTime, expirationTime, getNow())) {
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
   * Determines whether a given instant is in the interval defined by the starting point and the
   * length. If the instant is equal to either boundary of the interval, it is considered not to be
   * in the interval.
   * 
   * @param start The start of the interval. Can be null (in which case false is returned).
   * @param length The length of the interval. If negative, the method will always return false.
   * @param instant The instant to test.
   * @return Whether or not the instant is inside the interval.
   */
  boolean isInstantInInterval(Instant start, Duration length, Instant instant) {
    return start != null && !start.isAfter(instant) && !start.plus(length).isBefore(instant);
  }

  /**
   * Determines whether the value in this cache item was accessed recently (i.e. in the time span
   * given by the parameter).
   * 
   * @param since The interval length of the period we want to check (which ends now).
   * @return Whether the value was accessed in that time.
   */
  boolean valueWasAccessedRecently(Duration since) {
    return isInstantInInterval(getLastAccessTime(), since, getNow());
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
   * @return The last accessed time of the current value, or null if the value has not been accessed
   *         yet.
   */
  Instant getLastAccessTime() {
    return lastAccessTime;
  }

  /**
   * @return An instant representing the current time.
   */
  Instant getNow() {
    return Instant.now();
  }
}
