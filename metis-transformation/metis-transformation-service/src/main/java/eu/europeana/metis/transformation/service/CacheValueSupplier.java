package eu.europeana.metis.transformation.service;

/**
 * Supplier for cache values that can throw a {@link CacheValueSupplierException}.
 *
 * @param <V> The type of the value to be supplied.
 */
@FunctionalInterface
public interface CacheValueSupplier<V> {

  /**
   * Obtain the cache value.
   * 
   * @return The cache value.
   * @throws CacheValueSupplierException In case the value could not be retrieved.
   */
  V get() throws CacheValueSupplierException;

  /**
   * Exception indicated that the cache value could not be retrieved.
   */
  public static class CacheValueSupplierException extends Exception {

    /** Implements {@link java.io.Serializable}. **/
    private static final long serialVersionUID = 7580902638383421547L;

    /**
     * Constructor.
     * 
     * @param cause The cause of the exception.
     */
    public CacheValueSupplierException(Throwable cause) {
      super(cause);
    }
  }
}
