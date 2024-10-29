package eu.europeana.metis.authentication.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.metis.exception.BadContentException;
import org.junit.jupiter.api.Test;

class TestAccountRole {

  @Test
  void accountRoleCreationFromStringValue() throws BadContentException {
    AccountRole metisAdmin = AccountRole.getAccountRoleFromEnumName("METIS_ADMIN");
    assertEquals(AccountRole.METIS_ADMIN, metisAdmin);
  }

  @Test
  void accountRoleCreationFromStringValueFails() {
    assertThrows(BadContentException.class,
        () -> AccountRole.getAccountRoleFromEnumName("METIS_AD"));
  }
}
