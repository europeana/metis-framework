package eu.europeana.metis.network;

import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.function.Supplier;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ServiceUnavailableException;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.HttpServerErrorException;

/**
 * A utilities class used to encapsulate methods that throw exceptions {@link RuntimeException} or
 * {@link Exception} and should follow retries logic based on specific caused exceptions.
 *
 */
public final class ExternalRequestUtil {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalRequestUtil.class);
  private static final int MAX_RETRIES = 30;
  private static final int SLEEP_TIMEOUT = 1000;

  public static final Map<Class<?>, String> UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS;

  static {
    final Map<Class<?>, String> retryableExceptionMap = new ConcurrentHashMap<>();
    //Usually when a bad gateway exception occurs
    retryableExceptionMap.put(HttpServerErrorException.class, "");
    //Usually when the dns resolution fails
    retryableExceptionMap.put(UnknownHostException.class, "");
    //Usually when the server does not respond in time
    retryableExceptionMap.put(SocketTimeoutException.class, "");
    //Usually when the base url is not reachable
    retryableExceptionMap.put(SocketException.class, "");
    //Usually when the container service unavailable
    retryableExceptionMap.put(ServiceUnavailableException.class, "");
    //Usually when the endpoint in the container is not available
    retryableExceptionMap.put(NotFoundException.class, "");
    UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS = Collections.unmodifiableMap(retryableExceptionMap);
  }

  private ExternalRequestUtil() {
  }

  /**
   * Retries a request to an external service like a database. This method is meant to be called
   * when request throws an {@link Exception}. Default values for maximum retries {@link
   * #MAX_RETRIES} and period between retries {@link #SLEEP_TIMEOUT} will be used.
   *
   * @param supplierThrowingException the respective supplierThrowingException encapsulating the
   * external request
   * @return the expected object as a result of the external request
   * @throws E any exception that the supplier could throw
   */
  public static <R, E extends Exception> R retryableExternalRequest(
      SupplierThrowingException<R, E> supplierThrowingException) throws E {
    return retryableExternalRequest(supplierThrowingException, null, -1, -1);
  }

  /**
   * Retries a request to an external service like a database. This method is meant to be called
   * when request throws an {@link Exception} that contains a cause of one of the keys in the
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
   * @throws E any exception that the supplier could throw
   */
  public static <R, E extends Exception> R retryableExternalRequest(
      SupplierThrowingException<R, E> supplierThrowingException,
      Map<Class<?>, String> exceptionStringMap) throws E {
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
   * @throws E any exception that the supplier could throw
   * @param <R> the return type
   * @param <E> the exception type
   */
  public static <R, E extends Exception> R retryableExternalRequest(
      SupplierThrowingException<R, E> supplierThrowingException,
      Map<Class<?>, String> exceptionStringMap, int maxRetries, int periodBetweenRetriesInMillis)
      throws E {
    maxRetries =
        maxRetries < 0 ? MAX_RETRIES : maxRetries; //If not specified, set default value of retries
    periodBetweenRetriesInMillis =
        periodBetweenRetriesInMillis < 0 ? SLEEP_TIMEOUT : periodBetweenRetriesInMillis;
    AtomicInteger retriesCounter = new AtomicInteger(0);

    do {
      try {
        return supplierThrowingException.get();
      } catch (RuntimeException e) {
        doWhenExceptionCaught(e, exceptionStringMap, retriesCounter, maxRetries,
                periodBetweenRetriesInMillis);
      } catch (@SuppressWarnings("java:S2221") Exception e) {
        final E castException = (E) e; // Exception must be of given type
        doWhenExceptionCaught(castException, exceptionStringMap, retriesCounter, maxRetries,
                periodBetweenRetriesInMillis);
      }
    } while (true);

  }

  /**
   * Retries a request to an external service like a database. This method is meant to be called
   * when request throws a {@link RuntimeException} that contains a cause of one of the keys in the
   * {@link #UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS} Map. Default values for maximum retries
   * {@link * #MAX_RETRIES} and period between retries {@link #SLEEP_TIMEOUT} will be used.
   *
   * @param supplier the respective supplier encapsulating the external request a message to check,
   * if any. If message is null or empty, all messages will match
   * @return the expected object as a result of the external request
   * @param <R> the return type
   */
  public static <R> R retryableExternalRequestForNetworkExceptions(Supplier<R> supplier) {
    return retryableExternalRequestForRuntimeExceptions(supplier::get,
        UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS, -1, -1);
  }

  /**
   * Retries a request to an external service like a database. This method is meant to be called
   * when request throws a {@link Exception} that contains a cause of one of the keys in the
   * {@link #UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS} Map. Default values for maximum retries
   * {@link * #MAX_RETRIES} and period between retries {@link #SLEEP_TIMEOUT} will be used.
   *
   * @param supplier the respective supplier encapsulating the external request a message to check,
   * if any. If message is null or empty, all messages will match
   * @return the expected object as a result of the external request
   * @param <R> the return type
   * @param <E> the exception type
   * @throws E any exception that the supplier could throw
   */
  public static <R, E extends Exception> R retryableExternalRequestForNetworkExceptionsThrowing(SupplierThrowingException<R, E> supplier) throws E{
    return retryableExternalRequestForRuntimeExceptions(supplier,
        UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS, -1, -1);
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
   * @param <R> the return type
   * @param <E> the exception type
   * @throws E any exception that the supplier could throw
   */
  public static <R, E extends Exception> R retryableExternalRequestForRuntimeExceptions(SupplierThrowingException<R, E> supplier,
      Map<Class<?>, String> runtimeExceptionStringMap, int maxRetries,
      int periodBetweenRetriesInMillis) throws E{
    return retryableExternalRequest(supplier, runtimeExceptionStringMap, maxRetries,
            periodBetweenRetriesInMillis);
  }

  private static <E extends Exception> void doWhenExceptionCaught(E e,
          Map<Class<?>, String> exceptionStringMap,
          AtomicInteger retriesCounter, int maxRetries, int periodBetweenRetriesInMillis) throws E {
    retriesCounter.incrementAndGet();
    //Check if exception matches any exception from the map that is provided
    if(!isNullOrEmpty(exceptionStringMap)) {
      final boolean causeMatches = doesExceptionCauseMatchAnyOfProvidedExceptions(exceptionStringMap,
          e);
      //Rethrow the exception if more than maxRetries occurred or the cause doesn't match any expected causes.
      if (retriesCounter.get() > maxRetries || !causeMatches) {
        throw e;
      }
    } else {
      if (retriesCounter.get() > maxRetries) {
        throw e;
      }
    }

    if (LOGGER.isWarnEnabled()) {
      LOGGER.warn(String
              .format("External request has failed! Retrying in %sms", periodBetweenRetriesInMillis),
          e);
    }
    try {
      Thread.sleep(periodBetweenRetriesInMillis);
    } catch (InterruptedException ex) {
      LOGGER.warn("Thread was interrupted while waiting for retry.", ex);
      Thread.currentThread().interrupt();
    }
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
   * @param exceptionStringMap the map that contains all the type of exceptions to match with a
   * message to check, if any. If message is null or empty, any messages will match
   * @param e the exception to check the cause and try the matching
   * @return true if there is a match, otherwise false
   */
  public static boolean doesExceptionCauseMatchAnyOfProvidedExceptions(
      Map<Class<?>, String> exceptionStringMap, Exception e) {
    Throwable cause = getRootCause(e);
    final Predicate<Entry<Class<?>, String>> sameInstanceAndMessageMatches = entry ->
        entry.getKey().isInstance(cause) && (StringUtils.isBlank(entry.getValue()) || cause
            .getMessage().toLowerCase(Locale.US).contains(entry.getValue().toLowerCase(Locale.US)));

    boolean foundMatch = false;
    if (!isNullOrEmpty(exceptionStringMap)) {
      foundMatch = exceptionStringMap.entrySet().stream().anyMatch(sameInstanceAndMessageMatches);
    }
    return foundMatch;
  }

  /**
   * Check map for nullity or emptiness
   *
   * @param m the map
   * @return true if null or empty, false otherwise
   */
  public static boolean isNullOrEmpty(final Map<?, ?> m) {
    return m == null || m.isEmpty();
  }

  /**
   * Get root cause of a provided throwable
   * @param e the throwable
   * @return the root cause of the exception
   */
  public static Throwable getRootCause(Throwable e) {
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
   * @param <E> the exception type
   */
  @FunctionalInterface
  public interface SupplierThrowingException<T, E extends Exception> {

    /**
     * Gets a result.
     *
     * @return a result
     * @throws E any exception that the supplier could throw
     */
    T get() throws E;
  }
}
