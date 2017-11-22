package eu.europeana.metis.authentication.user;

import static org.junit.Assert.assertEquals;

import eu.europeana.metis.exception.BadContentException;
import org.junit.Test;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-11-03
 */
public class TestAccountRole {

  @Test
  public void accountRoleCreationFromStringValue() throws BadContentException {
    AccountRole metisAdmin = AccountRole.getAccountRoleFromEnumName("METIS_ADMIN");
    assertEquals(AccountRole.METIS_ADMIN, metisAdmin);
  }

  @Test(expected = BadContentException.class)
  public void accountRoleCreationFromStringValueFails() throws BadContentException {
    AccountRole wrongAccountRole = AccountRole.getAccountRoleFromEnumName("METIS_AD");
    assertEquals(null, wrongAccountRole);
  }

}
