package eu.europeana.metis.network;

import static eu.europeana.metis.network.ExternalRequestUtil.UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.ServiceUnavailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.HttpServerErrorException;

/**
 * Unit test for {@link ExternalRequestUtil}
 *
 * @author Jorge Ortiz
 * @since 01-02-2022
 */
class ExternalRequestUtilTest {

  @BeforeEach
  void setup() {
    UtilsThrower.setRetryCount(0);
  }

  @Test
  void retryableExternalRequest() throws Exception {
    final int maxRetries = (int) ReflectionTestUtils.getField(ExternalRequestUtil.class, "MAX_RETRIES");

    final Integer actualTest = ExternalRequestUtil.retryableExternalRequest(
        () -> UtilsThrower.throwGenericException(maxRetries));

    assertEquals(maxRetries, actualTest);
  }

  @Test
  void testRetryableExternalRequestWithMap() {
    final int maxRetries = (int) ReflectionTestUtils.getField(ExternalRequestUtil.class, "MAX_RETRIES");

    final Integer actualTest = ExternalRequestUtil.retryableExternalRequest(
        () -> UtilsThrower.throwRuntimeException(maxRetries),
        UNMODIFIABLE_MAP_WITH_TEST_EXCEPTIONS);

    assertEquals(maxRetries, actualTest);
  }

  @Test
  void testRetryableExternalRequestThrowsExceptionOutOfSpecifiedMap() {
    assertThrows(RuntimeException.class, () -> {
      ExternalRequestUtil.retryableExternalRequest(
          () -> {
            throw new RuntimeException(new ClassNotFoundException("Class pointer test exception"));
          },
          UNMODIFIABLE_MAP_WITH_TEST_EXCEPTIONS);
    });
  }

  @Test
  void testRetryableExternalRequestThrowsException() {
    assertThrows(RuntimeException.class, () -> {
      ExternalRequestUtil.retryableExternalRequest(
          () -> {
            throw new RuntimeException(new ClassNotFoundException("Class pointer test exception"));
          },
          null);
    });
  }

  @Test
  void testRetryableExternalRequestWithMapRetriesAndPeriod() {
    final int maxRetries = 5;
    final Integer actualTest = ExternalRequestUtil.retryableExternalRequest(
        () -> UtilsThrower.throwRuntimeException(maxRetries),
        UNMODIFIABLE_MAP_WITH_TEST_EXCEPTIONS, maxRetries, 50);

    assertEquals(maxRetries, actualTest);
  }

  @Test
  void retryableExternalRequestForNetworkExceptions() {
    final int maxRetries = (int) ReflectionTestUtils.getField(ExternalRequestUtil.class, "MAX_RETRIES");

    final Integer actualTest = ExternalRequestUtil.retryableExternalRequestForNetworkExceptions(
        () -> UtilsThrower.throwRuntimeNetworkException(maxRetries));

    assertEquals(maxRetries, actualTest);
  }


  @Test
  void retryableExternalRequestForNetworkExceptionsThrowing() throws Exception {
    final int maxRetries = (int) ReflectionTestUtils.getField(ExternalRequestUtil.class, "MAX_RETRIES");

    final Integer actualTest = ExternalRequestUtil.retryableExternalRequestForNetworkExceptionsThrowing(
        () -> UtilsThrower.throwNetworkException(maxRetries));

    assertEquals(maxRetries, actualTest);
  }

  @Test
  void retryableExternalRequestForRuntimeExceptions() {
    final int maxRetries = 5;
    final Integer actualTest = ExternalRequestUtil.retryableExternalRequestForRuntimeExceptions(
        () -> UtilsThrower.throwRuntimeException(maxRetries),
        UNMODIFIABLE_MAP_WITH_TEST_EXCEPTIONS, maxRetries, 100);

    assertEquals(maxRetries, actualTest);
  }

  @Test
  void getSocketExceptionConnectionReset() {
    final Map<Class<?>, String> expectedMap = Collections.singletonMap(SocketException.class, "Connection reset");

    Map<Class<?>, String> actualMap = ExternalRequestUtil.getSocketExceptionConnectionReset();

    assertEquals(expectedMap, actualMap);
  }

  private static Stream<Arguments> providedMapException() {
    return Stream.of(
        Arguments.of(UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS, new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT), true),
        Arguments.of(UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS, new UnknownHostException(), true),
        Arguments.of(UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS, new SocketTimeoutException(), true),
        Arguments.of(UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS, new SocketException(), true),
        Arguments.of(UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS, new ServiceUnavailableException(), true),
        Arguments.of(UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS, new NotFoundException(), true),
        Arguments.of(UNMODIFIABLE_MAP_WITH_NETWORK_EXCEPTIONS, new IOException(), false)
    );
  }

  @ParameterizedTest
  @MethodSource("providedMapException")
  void doesExceptionCauseMatchAnyOfProvidedExceptions(Map<Class<?>, String> exceptionMap, Exception exception,
      Boolean expectedMatch) {
    final Boolean isActualMatch = ExternalRequestUtil.doesExceptionCauseMatchAnyOfProvidedExceptions(exceptionMap, exception);

    assertEquals(expectedMatch, isActualMatch);
  }

  private static Stream<Map> providedMaps() {
    return Stream.of(new HashMap<String, Integer>(), null);
  }

  @ParameterizedTest
  @MethodSource("providedMaps")
  void isNullOrEmpty(Map<?, ?> testMap) {
    assertTrue(ExternalRequestUtil.isNullOrEmpty(testMap));
  }

  @Test
  void getRootCause() {
    final Throwable expectedThrowable = new Throwable("Root cause of content");
    final Exception exception = new Exception(expectedThrowable);

    final Throwable actualThrowable = ExternalRequestUtil.getRootCause(exception);
    assertEquals(expectedThrowable, actualThrowable);
  }

  /**
   * Helper class to throw exceptions and test code
   */
  private static class UtilsThrower {

    private static Integer retryCount = 0;

    private UtilsThrower() {
    }

    public static void setRetryCount(Integer retryCount) {
      UtilsThrower.retryCount = retryCount;
    }

    public static Integer throwRuntimeException(Integer maxRetries) throws RuntimeException {
      if (retryCount < maxRetries) {
        retryCount++;
        switch ((retryCount % 5) + 1) {
          case 1:
            throw new RuntimeException(new IOException("IO test exception"));
          case 2:
            throw new RuntimeException(new IllegalArgumentException("Illegal argument test exception"));
          case 3:
            throw new RuntimeException(new ArithmeticException("Arithmetic test exception"));
          case 4:
            throw new RuntimeException(new ArrayIndexOutOfBoundsException("Array test exception"));
          case 5:
            throw new RuntimeException(new NullPointerException("Null pointer test exception"));
        }
        return maxRetries;
      } else {
        return retryCount;
      }
    }

    public static Integer throwNetworkException(Integer maxRetries) throws Exception {
      if (retryCount < maxRetries) {
        retryCount++;
        switch ((retryCount % 6) + 1) {
          case 1:
            throw new UnknownHostException("Unknown host test exception");
          case 2:
            throw new HttpServerErrorException(HttpStatus.BAD_GATEWAY, "test exception");
          case 3:
            throw new SocketTimeoutException("Socket timeout test exception");
          case 4:
            throw new SocketException("Socket exception test exception");
          case 5:
            throw new ServiceUnavailableException("Service unavailable test exception");
          case 6:
            throw new NotFoundException("Not found test exception");
        }
        return maxRetries;
      } else {
        return retryCount;
      }
    }

    public static Integer throwRuntimeNetworkException(Integer maxRetries) throws RuntimeException {
      try {
        return throwNetworkException(maxRetries);
      } catch (Exception exceptionCause) {
        throw new RuntimeException("Runtime", exceptionCause);
      }
    }

    public static Integer throwGenericException(Integer maxRetries) throws Exception {
      if (retryCount < maxRetries) {
        retryCount++;
        throw new Exception("Unit test exception");
      } else {
        return retryCount;
      }
    }
  }

  private static final Map<Class<?>, String> UNMODIFIABLE_MAP_WITH_TEST_EXCEPTIONS;

  static {
    final Map<Class<?>, String> exceptionMap = new ConcurrentHashMap<>();
    exceptionMap.put(IOException.class, "");
    exceptionMap.put(IllegalArgumentException.class, "");
    exceptionMap.put(ArithmeticException.class, "");
    exceptionMap.put(ArrayIndexOutOfBoundsException.class, "");
    exceptionMap.put(NullPointerException.class, "");
    UNMODIFIABLE_MAP_WITH_TEST_EXCEPTIONS = Collections.unmodifiableMap(exceptionMap);
  }
}