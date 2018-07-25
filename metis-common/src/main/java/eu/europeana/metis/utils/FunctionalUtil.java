package eu.europeana.metis.utils;

import com.mongodb.MongoSocketException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-07-24
 */
public final class FunctionalUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(FunctionalUtil.class);

  private FunctionalUtil() {
  }

  public static <R> R retryableMongoCall(
      SupplierWithMongoSocketException<R> supplierWithMongoSocketException) {
    int maxRetries = 10;
    int retryCounter = 0;
    int sleepTimeout = 500;

    R result = null;
    boolean success = false;
    do {
      try {
        result = supplierWithMongoSocketException.get();
        success = true;
      } catch (MongoSocketException e) {
        LOGGER.warn(String.format("Call to Mongo has failed! Retrying in %sms", sleepTimeout), e);
        retryCounter++;
        if (retryCounter > maxRetries) {
          throw e; //rethrow if all retries failed.
        }
        try {
          Thread.sleep(sleepTimeout);
        } catch (InterruptedException ex) {
          LOGGER.warn("Thread was interrupted while waiting for retry.", ex);
          Thread.currentThread().interrupt();
          return null;
        }
      }
    } while (!success);
    return result;
  }

  @FunctionalInterface
  public interface SupplierWithMongoSocketException<R> {

    R get() throws MongoSocketException;
  }

}
