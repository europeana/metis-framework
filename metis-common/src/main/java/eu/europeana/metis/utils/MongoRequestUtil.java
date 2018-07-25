package eu.europeana.metis.utils;

import com.mongodb.MongoSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-07-24
 */
public final class MongoRequestUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoRequestUtil.class);
  private static final int MAX_RETRIES = 10;
  private static final int SLEEP_TIMEOUT = 500;

  private MongoRequestUtil() {
  }

  /**
   * Retries a request to Mongo in case an exception with root cause message "Connection reset"
   * occurred.
   *
   * @param supplierWithMongoSocketException the respective supplier encapsulating the Mongo
   * request
   * @return the expected object as a result of the mongo request
   */
  public static <R> R retryableMongoRequest(
      SupplierWithMongoSocketException<R> supplierWithMongoSocketException) {
    int retryCounter = 0;

    R result = null;
    boolean success = false;
    do {
      try {
        result = supplierWithMongoSocketException.get();
        success = true;
      } catch (MongoSocketException e) {
        retryCounter++;
        //Re-throw if it's not a Connection reset error or max retries exceeded.
        if (!getCause(e).getMessage().contains("Connection reset") || retryCounter > MAX_RETRIES) {
          throw e;
        }
        LOGGER
            .warn(String.format("Request to Mongo has failed! Retrying in %sms", SLEEP_TIMEOUT), e);
        try {
          Thread.sleep(SLEEP_TIMEOUT);
        } catch (InterruptedException ex) {
          LOGGER.warn("Thread was interrupted while waiting for retry.", ex);
          Thread.currentThread().interrupt();
          return null;
        }
      }
    } while (!success);
    return result;
  }

  private static Throwable getCause(Throwable e) {
    Throwable cause;
    Throwable result = e;

    while (null != (cause = result.getCause()) && (result != cause)) {
      result = cause;
    }
    return result;
  }

  /**
   * It's a {@link java.util.function.Supplier} functional interface that throws a {@link
   * MongoSocketException}.
   * <p>This supplier is to be used for requests to Mongo, and was created to avoid the
   * issues that are faced with the Application and Mongo in Cloud Foundry where the underlying
   * system would throw a {@link java.net.SocketException}</p>
   *
   * @param <R> the object that should be returned.
   */
  @FunctionalInterface
  public interface SupplierWithMongoSocketException<R> {

    /**
     * Contains supplier functionality to get an object with the possibility that it throws a {@link
     * MongoSocketException}
     *
     * @return the supplied object
     * @throws MongoSocketException the exception while requested the object from Mongo
     */
    R get() throws MongoSocketException;
  }
}
