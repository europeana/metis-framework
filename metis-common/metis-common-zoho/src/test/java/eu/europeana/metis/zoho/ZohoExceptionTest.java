package eu.europeana.metis.zoho;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link ZohoException}
 *
 * @author Jorge Ortiz
 * @since 02-02-2022
 */
class ZohoExceptionTest {

  private UtilsThrower utilsThrower;

  @BeforeEach
  void setup() {
    utilsThrower = new UtilsThrower();
  }

  @Test
  void testZohoException() {
    ZohoException actualException = assertThrows(ZohoException.class, () -> utilsThrower.throwZohoException());
    assertEquals("Zoho content", actualException.getMessage());
  }

  @Test
  void testBadContentExceptionThrowable() {
    ZohoException actualException = assertThrows(ZohoException.class, () -> utilsThrower.throwZohoExceptionThrowable());
    assertEquals("Zoho content throwable", actualException.getMessage());
    assertEquals("Cause of Zoho exception", actualException.getCause().getMessage());
  }

  private class UtilsThrower {

    public void throwZohoException() throws ZohoException {
      throw new ZohoException("Zoho content");
    }

    public void throwZohoExceptionThrowable() throws ZohoException {
      throw new ZohoException("Zoho content throwable", new Throwable("Cause of Zoho exception"));
    }
  }
}