package eu.europeana.metis.authentication.user;

import static org.junit.jupiter.api.Assertions.*;

import eu.europeana.metis.exception.BadContentException;
import org.junit.jupiter.api.Test;

class AccountRoleTest {

  @Test
  void testAccountRoleValues() throws BadContentException {
    //Check null
    assertThrows(BadContentException.class, () -> AccountRole.getAccountRoleFromEnumName(null));

    //Check non existent value
    assertThrows(BadContentException.class, () -> AccountRole.getAccountRoleFromEnumName("invalid"));

    //Check all values are resolvable
    assertEquals(AccountRole.METIS_ADMIN, AccountRole.getAccountRoleFromEnumName("METIS_ADMIN"));
    assertEquals(AccountRole.EUROPEANA_DATA_OFFICER, AccountRole.getAccountRoleFromEnumName("EUROPEANA_DATA_OFFICER"));
    assertEquals(AccountRole.PROVIDER_VIEWER, AccountRole.getAccountRoleFromEnumName("PROVIDER_VIEWER"));

    //Any character case should also work
    assertEquals(AccountRole.METIS_ADMIN, AccountRole.getAccountRoleFromEnumName("metis_ADMIN"));
    assertEquals(AccountRole.EUROPEANA_DATA_OFFICER, AccountRole.getAccountRoleFromEnumName(
        "eurOpeana_data_Officer"));
    assertEquals(AccountRole.PROVIDER_VIEWER, AccountRole.getAccountRoleFromEnumName(
        "Provider_Viewer"));
  }
}