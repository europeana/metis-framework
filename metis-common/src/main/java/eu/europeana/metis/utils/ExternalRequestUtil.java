package eu.europeana.metis.utils;

import com.mongodb.MongoSecurityException;
import com.mongodb.MongoSocketException;
import java.net.SocketException;
import java.util.Collections;
import java.util.Map;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-07-24
 */
public final class ExternalRequestUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalRequestUtil.class);
  private static final int MAX_RETRIES = 10;
  private static final int SLEEP_TIMEOUT = 500;

  private ExternalRequestUtil() {
  }

  /**
   * Retries a request to an external service like a database. This method is meant to be called
   * when request throws a {@link RuntimeException} that contains a {@link SocketException} cause
   * with message "Connection reset". This method was intentionally implemented for the above
   * described issue that is caused in the Bluemix Cloud Foundry environment. Some examples are:
   * <ul>
   * <li>{@link MongoSocketException}</li> From a Mongo request
   * <li>{@link MongoSecurityException}</li> From a Mongo request
   * <li>{@link ResourceAccessException}</li> From an HTTP request
   * </ul>
   *
   * @param supplier the respective supplier encapsulating the external request
   * @return the expected object as a result of the external request
   * @deprecated As of 01-10-2018. Use the one of the following method {@link #retryableExternalRequestConnectionReset(Supplier)}
   * or the more controlled methods {@link #retryableExternalRequest(Supplier, Map, int, int)},
   * {@link #retryableExternalRequest(Supplier, Map)} instead by providing the Map containing the
   * required class types and String message of the caused exception
   */
  @Deprecated
  public static <R> R retryableExternalRequest(Supplier<R> supplier) {
    return retryableExternalRequestConnectionReset(supplier);
  }

  /**
   * Retries a request to an external service like a database. This method is meant to be called
   * when request throws a {@link RuntimeException} that contains a {@link SocketException} cause
   * with message "Connection reset". This method was intentionally implemented for the above
   * described issue that is caused in the Bluemix Cloud Foundry environment. Some examples are:
   * <ul>
   * <li>{@link MongoSocketException}</li> From a Mongo request
   * <li>{@link MongoSecurityException}</li> From a Mongo request
   * <li>{@link ResourceAccessException}</li> From an HTTP request
   * </ul>
   *
   * @param supplier the respective supplier encapsulating the external request
   * @return the expected object as a result of the external request
   */
  public static <R> R retryableExternalRequestConnectionReset(Supplier<R> supplier) {
    return retryableExternalRequest(supplier, getSocketExceptionConnectionReset(), -1, -1);
  }

  /**
   * Retries a request to an external service like a database. This method is meant to be called
   * when request throws a {@link RuntimeException} that contains a cause of one of the keys in the
   * specified Map and matches the String message provided as the value of the key, if any. If no
   * message specified, then the matching will only be checked on the type of the exception. Default
   * values for maximum retries {@link #MAX_RETRIES} and period between retries {@link
   * #SLEEP_TIMEOUT} will be used.
   *
   * @param supplier the respective supplier encapsulating the external request
   * @param runtimeExceptionStringMap the map that contains all the type of exceptions to match with
   * a message to check, if any
   * @return the expected object as a result of the external request
   */
  public static <R, T extends Exception> R retryableExternalRequest(
      Supplier<R> supplier, Map<Class<T>, String> runtimeExceptionStringMap) {
    return retryableExternalRequest(supplier, runtimeExceptionStringMap, -1, -1);
  }

  /**
   * Retries a request to an external service like a database. This method is meant to be called
   * when request throws a {@link RuntimeException} that contains a cause of one of the keys in the
   * specified Map and matches the String message provided as the value of the key, if any. If no
   * message specified, then the matching will only be checked on the type of the exception.
   *
   * @param supplier the respective supplier encapsulating the external request
   * @param runtimeExceptionStringMap the map that contains all the type of exceptions to match with
   * a message to check, if any
   * @param maxRetries the maximum amount of retries to perform, if set to -1, the default value
   * will be set
   * @param periodBetweenRetriesInMillis the amount of period spend sleeping in between two retries,
   * if set to -1, the default value will be set
   * @return the expected object as a result of the external request
   */
  public static <R, T extends Exception> R retryableExternalRequest(
      Supplier<R> supplier, Map<Class<T>, String> runtimeExceptionStringMap, int maxRetries,
      int periodBetweenRetriesInMillis) {
    maxRetries =
        maxRetries < 0 ? MAX_RETRIES : maxRetries; //If not specified, set default value of retries
    periodBetweenRetriesInMillis =
        periodBetweenRetriesInMillis < 0 ? SLEEP_TIMEOUT : periodBetweenRetriesInMillis;
    int retryCounter = 0;

    do {
      try {
        return supplier.get();
      } catch (RuntimeException e) {
        retryCounter++;
        //Re-throw if it's not a Connection reset error or max retries exceeded.
        Throwable cause = getCause(e);
        final boolean causeMatches = runtimeExceptionStringMap.entrySet().stream()
            .anyMatch(
                entry -> entry.getKey().isInstance(cause) && (entry.getValue() == null || cause
                    .getMessage().contains(entry.getValue()))
            );
        //Rethrow the exception if more than maxRetries occurred or the cause doesn't match any expected causes.
        if (retryCounter > maxRetries || !causeMatches) {
          throw e;
        }

        LOGGER
            .warn(String.format("External request has failed! Retrying in %sms",
                periodBetweenRetriesInMillis), e);
        try {
          Thread.sleep(periodBetweenRetriesInMillis);
        } catch (InterruptedException ex) {
          LOGGER.warn("Thread was interrupted while waiting for retry.", ex);
          Thread.currentThread().interrupt();
          return null;
        }
      }
    } while (true);
  }

  /**
   * Creates a singleton Map that contains the type {@link SocketException} as key and the
   * "Connection reset" as value
   * @return the created map
   */
  public static Map<Class<SocketException>, String> getSocketExceptionConnectionReset() {
    return Collections.singletonMap(SocketException.class, "Connection reset");
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
