package eu.europeana.metis.utils;

import com.mongodb.MongoSecurityException;
import com.mongodb.MongoSocketException;
import java.net.SocketException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.ResourceAccessException;

/**
 * A utilities class used to encapsulate methods that throw exceptions {@link RuntimeException} or
 * {@link Exception} and should follow retries logic based on specific caused exceptions.
 *
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
   */
  public static <R> R retryableExternalRequestConnectionReset(Supplier<R> supplier) {
    return retryableExternalRequestForRuntimeExceptions(supplier,
        getSocketExceptionConnectionReset(), -1, -1);
  }

  /**
   * Retries a request to an external service like a database. This method is meant to be called
   * when request throws a {@link Exception} that contains a cause of one of the keys in the
   * specified Map and matches the String message provided as the value of the key, if any. If no
   * message specified, then the matching will only be checked on the type of the exception. Default
   * values for maximum retries {@link #MAX_RETRIES} and period between retries {@link
   * #SLEEP_TIMEOUT} will be used.
   *
   * @param supplierThrowingException the respective supplierThrowingException encapsulating the
   * external request
   * @param exceptionStringMap the map that contains all the type of exceptions to match with a
   * message to check, if any. If message is null or empty, all messages will match
   * @return the expected object as a result of the external request
   * @throws Exception any exception that the supplier could throw
   */
  public static <R> R retryableExternalRequest(
      SupplierThrowingException<R> supplierThrowingException,
      Map<Class<?>, String> exceptionStringMap)
      throws Exception {
    return retryableExternalRequest(supplierThrowingException, exceptionStringMap, -1, -1);
  }

  /**
   * Retries a request to an external service like a database. This method is meant to be called
   * when request throws a {@link Exception} that contains a cause of one of the keys in the
   * specified Map and matches the String message provided as the value of the key, if any. If no
   * message specified, then the matching will only be checked on the type of the exception.
   *
   * @param supplierThrowingException the respective supplierThrowingException encapsulating the
   * external request
   * @param exceptionStringMap the map that contains all the type of exceptions to match with a
   * message to check, if any. If message is null or empty, all messages will match
   * @param maxRetries the maximum amount of retries to perform, if set to -1, the default value
   * will be set
   * @param periodBetweenRetriesInMillis the amount of period spend sleeping in between two retries,
   * if set to -1, the default value will be set
   * @return the expected object as a result of the external request
   * @throws Exception any exception that the supplier could throw
   */
  public static <R> R retryableExternalRequest(
      SupplierThrowingException<R> supplierThrowingException,
      Map<Class<?>, String> exceptionStringMap, int maxRetries,
      int periodBetweenRetriesInMillis) throws Exception {
    maxRetries =
        maxRetries < 0 ? MAX_RETRIES : maxRetries; //If not specified, set default value of retries
    periodBetweenRetriesInMillis =
        periodBetweenRetriesInMillis < 0 ? SLEEP_TIMEOUT : periodBetweenRetriesInMillis;
    AtomicInteger retriesCounter = new AtomicInteger(0);

    do {
      try {
        return supplierThrowingException.get();
      } catch (Exception e) {
        doWhenExceptionCaught(e, exceptionStringMap, retriesCounter, maxRetries,
            periodBetweenRetriesInMillis);
      }
    } while (true);
  }

  /**
   * Retries a request to an external service like a database. This method is meant to be called
   * when request throws a {@link RuntimeException} that contains a cause of one of the keys in the
   * specified Map and matches the String message provided as the value of the key, if any. If no
   * message specified, then the matching will only be checked on the type of the exception.
   *
   * @param supplier the respective supplier encapsulating the external request
   * @param runtimeExceptionStringMap the map that contains all the type of exceptions to match with
   * a message to check, if any. If message is null or empty, all messages will match
   * @param maxRetries the maximum amount of retries to perform, if set to -1, the default value
   * will be set
   * @param periodBetweenRetriesInMillis the amount of period spend sleeping in between two retries,
   * if set to -1, the default value will be set
   * @return the expected object as a result of the external request
   */
  public static <R> R retryableExternalRequestForRuntimeExceptions(
      Supplier<R> supplier, Map<Class<?>, String> runtimeExceptionStringMap, int maxRetries,
      int periodBetweenRetriesInMillis) {
    maxRetries =
        maxRetries < 0 ? MAX_RETRIES : maxRetries; //If not specified, set default value of retries
    periodBetweenRetriesInMillis =
        periodBetweenRetriesInMillis < 0 ? SLEEP_TIMEOUT : periodBetweenRetriesInMillis;
    AtomicInteger retriesCounter = new AtomicInteger(0);

    do {
      try {
        return supplier.get();
      } catch (RuntimeException e) {
        try {
          doWhenExceptionCaught(e, runtimeExceptionStringMap, retriesCounter, maxRetries,
              periodBetweenRetriesInMillis);
        } catch (Exception exception) {
          throw (RuntimeException) exception; //It's supposed to be a RuntimeException
        }
      }
    } while (true);
  }

  private static <R> R doWhenExceptionCaught(Exception e,
      Map<Class<?>, String> runtimeExceptionStringMap,
      AtomicInteger retriesCounter, int maxRetries, int periodBetweenRetriesInMillis)
      throws Exception {
    retriesCounter.incrementAndGet();
    //Re-throw if it's not a Connection reset error or max retries exceeded.
    final boolean causeMatches = doesExceptionCauseMatchAnyOfProvidedExceptions(runtimeExceptionStringMap, e);
    //Rethrow the exception if more than maxRetries occurred or the cause doesn't match any expected causes.
    if (retriesCounter.get() > maxRetries || !causeMatches) {
      throw e;
    }

    if (LOGGER.isWarnEnabled()) {
      LOGGER.warn(String.format("External request has failed! Retrying in %sms",
          periodBetweenRetriesInMillis), e);
    }
    try {
      Thread.sleep(periodBetweenRetriesInMillis);
    } catch (InterruptedException ex) {
      LOGGER.warn("Thread was interrupted while waiting for retry.", ex);
      Thread.currentThread().interrupt();
      return null;
    }
    return null;
  }

  /**
   * Creates a singleton Map that contains the type {@link SocketException} as key and the
   * "Connection reset" as value
   *
   * @return the created map
   */
  public static Map<Class<?>, String> getSocketExceptionConnectionReset() {
    return Collections.singletonMap(SocketException.class, "Connection reset");
  }

  /**
   * Checks if an exception cause, matches any of the exception rules in the provided exception
   * map.
   *
   * @param runtimeExceptionStringMap the map that contains all the type of exceptions to match with
   * a message to check, if any. If message is null or empty, all messages will match
   * @param e the exception to check the cause and try the matching
   * @return true if there is a match, otherwise false
   */
  public static boolean doesExceptionCauseMatchAnyOfProvidedExceptions(
      Map<Class<?>, String> runtimeExceptionStringMap, Exception e) {
    Throwable cause = getCause(e);
    return runtimeExceptionStringMap.entrySet().stream()
        .anyMatch(
            entry -> entry.getKey().isInstance(cause) && (StringUtils.isBlank(entry.getValue())
                || cause.getMessage().toLowerCase(Locale.US)
                .contains(entry.getValue().toLowerCase(Locale.US)))
        );
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
   * A Supplier that throws {@link Exception}
   *
   * @param <T> the result of the supplier
   */
  @FunctionalInterface
  public interface SupplierThrowingException<T> {

    /**
     * Gets a result.
     *
     * @return a result
     * @throws Exception any exception that the supplier could throw
     */
    T get() throws Exception;
  }
}
