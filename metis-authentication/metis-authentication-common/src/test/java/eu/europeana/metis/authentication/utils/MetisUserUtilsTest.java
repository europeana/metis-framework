package eu.europeana.metis.authentication.utils;

import static eu.europeana.metis.authentication.utils.MetisUserUtils.DEFAULT_COUNTRY;
import static eu.europeana.metis.authentication.utils.MetisUserUtils.DEFAULT_LAST_NAME;
import static eu.europeana.metis.authentication.utils.MetisUserUtils.DEFAULT_NAME;
import static eu.europeana.metis.authentication.utils.MetisUserUtils.EUROPEANA_ORGANIZATION_ID;
import static eu.europeana.metis.authentication.utils.MetisUserUtils.EUROPEANA_ORGANIZATION_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUser;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class MetisUserUtilsTest {

  @Test
  void checkFieldsAndPopulateMetisUser() {
    MetisUser metisUser = MetisUserUtils.checkFieldsAndPopulateMetisUser("test@email.com");

    assertEquals(DEFAULT_NAME, metisUser.getFirstName());
    assertEquals(DEFAULT_LAST_NAME, metisUser.getLastName());
    assertTrue(0 < Long.valueOf(metisUser.getUserId()));
    assertEquals("test@email.com", metisUser.getEmail());
    assertTrue(Instant.now()
                      .atZone(ZoneId.systemDefault())
                      .toLocalDate().equals(metisUser.getCreatedDate().toInstant()
                                                     .atZone(ZoneId.systemDefault())
                                                     .toLocalDate()));
    assertTrue(Instant.now()
                      .atZone(ZoneId.systemDefault())
                      .toLocalDate().equals(metisUser.getUpdatedDate().toInstant()
                                                     .atZone(ZoneId.systemDefault())
                                                     .toLocalDate()));
    assertEquals(DEFAULT_COUNTRY, metisUser.getCountry());
    assertTrue(metisUser.isNetworkMember());
    assertTrue(metisUser.isMetisUserFlag());
    assertEquals(AccountRole.EUROPEANA_DATA_OFFICER, metisUser.getAccountRole());
    assertEquals(EUROPEANA_ORGANIZATION_ID, metisUser.getOrganizationId());
    assertEquals(EUROPEANA_ORGANIZATION_NAME, metisUser.getOrganizationName());
  }
}
