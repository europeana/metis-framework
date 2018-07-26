package eu.europeana.metis.utils;

import com.mongodb.MongoSocketException;
import java.util.function.Supplier;
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
   * Retries a request to Mongo in case a {@link MongoSocketException} with root cause message
   * "Connection reset" occurred.
   *
   * @param supplier the respective supplier encapsulating the Mongo request
   * @return the expected object as a result of the mongo request
   */
  public static <R> R retryableMongoRequest(
      Supplier<R> supplier) {
    int retryCounter = 0;

    do {
      try {
        return supplier.get();
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
    } while (true);
  }

  private static Throwable getCause(Throwable e) {
    Throwable cause;
    Throwable result = e;

    while (null != (cause = result.getCause()) && (result != cause)) {
      result = cause;
    }
    return result;
  }
}
