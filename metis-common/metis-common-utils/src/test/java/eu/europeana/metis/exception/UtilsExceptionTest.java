package eu.europeana.metis.exception;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UtilsExceptionTest {
  private UtilsThrower utilsThrower;
  @BeforeEach
  void setup() {
    utilsThrower = new UtilsThrower();
  }

  @Test
  void testBadContentException() {
   BadContentException actualException = assertThrows(BadContentException.class, ()-> utilsThrower.throwBadContentException());
   assertEquals("Bad content", actualException.getMessage());
  }

  @Test
  void testBadContentExceptionThrowable() {
    BadContentException actualException = assertThrows(BadContentException.class, ()-> utilsThrower.throwBadContentExceptionThrowable());
    assertEquals("Bad content throwable", actualException.getMessage());
    assertEquals( "Cause of bad content", actualException.getCause().getMessage());
  }

  @Test
  void testExternalTaskException() {
    ExternalTaskException actualException = assertThrows(ExternalTaskException.class, ()-> utilsThrower.throwExternalTaskException());
    assertEquals("External Task", actualException.getMessage());
  }

  @Test
  void testExternalTaskExceptionThrowable() {
    ExternalTaskException actualException = assertThrows(ExternalTaskException.class, ()-> utilsThrower.throwExternalTaskExceptionThrowable());
    assertEquals("External Task throwable", actualException.getMessage());
    assertEquals( "Cause of External Task", actualException.getCause().getMessage());
  }

  @Test
  void testGenericMetisException() {
    GenericMetisException actualException = assertThrows(GenericMetisException.class, ()-> utilsThrower.throwGenericMetisException());
    assertEquals("Generic metis", actualException.getMessage());
  }

  @Test
  void testGenericMetisExceptionThrowable() {
    GenericMetisException actualException = assertThrows(GenericMetisException.class, ()-> utilsThrower.throwGenericMetisExceptionThrowable());
    assertEquals("Generic metis throwable", actualException.getMessage());
    assertEquals( "Cause of generic metis", actualException.getCause().getMessage());
  }

  @Test
  void testNoUserFoundException() {
    NoUserFoundException actualException = assertThrows(NoUserFoundException.class, ()-> utilsThrower.throwNoUserFoundException());
    assertEquals("No user found", actualException.getMessage());
  }

  @Test
  void testUserAlreadyExistsException() {
    UserAlreadyExistsException actualException = assertThrows(UserAlreadyExistsException.class, ()-> utilsThrower.throwUserAlreadyExistsException());
    assertEquals("User already exists", actualException.getMessage());
  }

  @Test
  void testUserUnauthorizedException() {
    UserUnauthorizedException actualException = assertThrows(UserUnauthorizedException.class, ()-> utilsThrower.throwUserUnauthorizedException());
    assertEquals("User unauthorized to perform an action", actualException.getMessage());
  }

  @Test
  void testUserUnauthorizedExceptionThrowable() {
    UserUnauthorizedException actualException = assertThrows(UserUnauthorizedException.class, ()-> utilsThrower.throwUserUnauthorizedExceptionThrowable());
    assertEquals("User unauthorized to perform an action", actualException.getMessage());
    assertEquals( "Cause of unauthorized", actualException.getCause().getMessage());
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

  private class UtilsThrower {

    public void throwBadContentException() throws BadContentException {
      throw new BadContentException("Bad content");
    }

    public void throwBadContentExceptionThrowable() throws BadContentException {
      throw new BadContentException("Bad content throwable", new Throwable("Cause of bad content"));
    }

    public void throwExternalTaskException() throws ExternalTaskException {
      throw new ExternalTaskException("External Task");
    }

    public void throwExternalTaskExceptionThrowable() throws ExternalTaskException {
      throw new ExternalTaskException("External Task throwable", new Throwable("Cause of External Task"));
    }

    public void throwGenericMetisException() throws GenericMetisException {
      throw new GenericMetisException("Generic metis");
    }

    public void throwGenericMetisExceptionThrowable() throws GenericMetisException {
      throw new GenericMetisException("Generic metis throwable", new Throwable("Cause of generic metis"));
    }

    public void throwNoUserFoundException() throws NoUserFoundException {
      throw new NoUserFoundException("No user found");
    }

    public void throwUserAlreadyExistsException() throws UserAlreadyExistsException {
      throw new UserAlreadyExistsException("User already exists");
    }

    public void throwUserUnauthorizedException() throws UserUnauthorizedException {
      throw new UserUnauthorizedException("User unauthorized to perform an action");
    }

    public void throwUserUnauthorizedExceptionThrowable() throws UserUnauthorizedException {
      throw new UserUnauthorizedException("User unauthorized to perform an action", new Throwable("Cause of unauthorized"));
    }
  }
}