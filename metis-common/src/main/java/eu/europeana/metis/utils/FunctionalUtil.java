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
  private static final int MAX_RETRIES = 10;
  private static final int SLEEP_TIMEOUT = 500;

  private FunctionalUtil() {
  }

  public static <R> R retryableMongoCall(
      SupplierWithMongoSocketException<R> supplierWithMongoSocketException) {
    int retryCounter = 0;

    R result = null;
    boolean success = false;
    do {
      try {
        result = supplierWithMongoSocketException.get();
        success = true;
      } catch (MongoSocketException e) {
        LOGGER.warn(String.format("Call to Mongo has failed! Retrying in %sms", SLEEP_TIMEOUT), e);
        retryCounter++;
        if (retryCounter > MAX_RETRIES) {
          throw e; //rethrow if all retries failed.
        }
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

  @FunctionalInterface
  public interface SupplierWithMongoSocketException<R> {

    R get() throws MongoSocketException;
  }

}
