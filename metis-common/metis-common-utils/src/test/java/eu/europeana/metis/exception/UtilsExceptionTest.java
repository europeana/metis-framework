package eu.europeana.metis.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Unit test for
 * {@link BadContentException}
 * {@link ExternalTaskException}
 * {@link GenericMetisException}
 * {@link NoUserFoundException}
 * {@link UserAlreadyExistsException}
 * {@link UserUnauthorizedException}
 * {@link StructuredExceptionWrapper}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class UtilsExceptionTest {

  @Test
  void testBadContentException() {
    BadContentException actualException = assertThrows(BadContentException.class, UtilsThrower::throwBadContentException);
    assertEquals("Bad content", actualException.getMessage());
  }

  @Test
  void testBadContentExceptionThrowable() {
    BadContentException actualException = assertThrows(BadContentException.class,
        UtilsThrower::throwBadContentExceptionThrowable);
    assertEquals("Bad content throwable", actualException.getMessage());
    assertEquals("Cause of bad content", actualException.getCause().getMessage());
  }

  @Test
  void testExternalTaskException() {
    ExternalTaskException actualException = assertThrows(ExternalTaskException.class, UtilsThrower::throwExternalTaskException);
    assertEquals("External Task", actualException.getMessage());
  }

  @Test
  void testExternalTaskExceptionThrowable() {
    ExternalTaskException actualException = assertThrows(ExternalTaskException.class,
        UtilsThrower::throwExternalTaskExceptionThrowable);
    assertEquals("External Task throwable", actualException.getMessage());
    assertEquals("Cause of External Task", actualException.getCause().getMessage());
  }

  @Test
  void testGenericMetisException() {
    GenericMetisException actualException = assertThrows(GenericMetisException.class, UtilsThrower::throwGenericMetisException);
    assertEquals("Generic metis", actualException.getMessage());
  }

  @Test
  void testGenericMetisExceptionThrowable() {
    GenericMetisException actualException = assertThrows(GenericMetisException.class,
        UtilsThrower::throwGenericMetisExceptionThrowable);
    assertEquals("Generic metis throwable", actualException.getMessage());
    assertEquals("Cause of generic metis", actualException.getCause().getMessage());
  }

  @Test
  void testNoUserFoundException() {
    NoUserFoundException actualException = assertThrows(NoUserFoundException.class, UtilsThrower::throwNoUserFoundException);
    assertEquals("No user found", actualException.getMessage());
  }

  @Test
  void testUserAlreadyExistsException() {
    UserAlreadyExistsException actualException = assertThrows(UserAlreadyExistsException.class,
        UtilsThrower::throwUserAlreadyExistsException);
    assertEquals("User already exists", actualException.getMessage());
  }

  @Test
  void testUserUnauthorizedException() {
    UserUnauthorizedException actualException = assertThrows(UserUnauthorizedException.class,
        UtilsThrower::throwUserUnauthorizedException);
    assertEquals("User unauthorized to perform an action", actualException.getMessage());
  }

  @Test
  void testUserUnauthorizedExceptionThrowable() {
    UserUnauthorizedException actualException = assertThrows(UserUnauthorizedException.class,
        UtilsThrower::throwUserUnauthorizedExceptionThrowable);
    assertEquals("User unauthorized to perform an action", actualException.getMessage());
    assertEquals("Cause of unauthorized", actualException.getCause().getMessage());
  }

  @Test
  void testStructuredExceptionWrapper() {
    StructuredExceptionWrapper structuredExceptionWrapper = new StructuredExceptionWrapper("Error Message");

    assertEquals("Error Message", structuredExceptionWrapper.getErrorMessage());
  }

  @Test
  void testStructuredExceptionWrapperSetter() {
    StructuredExceptionWrapper structuredExceptionWrapper = new StructuredExceptionWrapper();
    structuredExceptionWrapper.setErrorMessage("Error Message");

    assertEquals("Error Message", structuredExceptionWrapper.getErrorMessage());
  }

  /**
   * Helper class for {@link UtilsExceptionTest}
   */
  private static class UtilsThrower {

    public static void throwBadContentException() throws BadContentException {
      throw new BadContentException("Bad content");
    }

    public static void throwBadContentExceptionThrowable() throws BadContentException {
      throw new BadContentException("Bad content throwable", new Throwable("Cause of bad content"));
    }

    public static void throwExternalTaskException() throws ExternalTaskException {
      throw new ExternalTaskException("External Task");
    }

    public static void throwExternalTaskExceptionThrowable() throws ExternalTaskException {
      throw new ExternalTaskException("External Task throwable", new Throwable("Cause of External Task"));
    }

    public static void throwGenericMetisException() throws GenericMetisException {
      throw new GenericMetisException("Generic metis");
    }

    public static void throwGenericMetisExceptionThrowable() throws GenericMetisException {
      throw new GenericMetisException("Generic metis throwable", new Throwable("Cause of generic metis"));
    }

    public static void throwNoUserFoundException() throws NoUserFoundException {
      throw new NoUserFoundException("No user found");
    }

    public static void throwUserAlreadyExistsException() throws UserAlreadyExistsException {
      throw new UserAlreadyExistsException("User already exists");
    }

    public static void throwUserUnauthorizedException() throws UserUnauthorizedException {
      throw new UserUnauthorizedException("User unauthorized to perform an action");
    }

    public static void throwUserUnauthorizedExceptionThrowable() throws UserUnauthorizedException {
      throw new UserUnauthorizedException("User unauthorized to perform an action", new Throwable("Cause of unauthorized"));
    }
  }
}