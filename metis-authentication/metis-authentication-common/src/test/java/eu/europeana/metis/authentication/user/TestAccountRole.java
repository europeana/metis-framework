package eu.europeana.metis.authentication.user;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.metis.exception.BadContentException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-11-03
 */
class TestAccountRole {

  @Test
  void accountRoleCreationFromStringValue() throws BadContentException {
    AccountRole metisAdmin = AccountRole.getAccountRoleFromEnumName("METIS_ADMIN");
    assertEquals(AccountRole.METIS_ADMIN, metisAdmin);
  }

  @Test
  void accountRoleCreationFromStringValueFails() {
    Assertions.assertThrows(BadContentException.class,
        () -> AccountRole.getAccountRoleFromEnumName("METIS_AD"));
  }
}
