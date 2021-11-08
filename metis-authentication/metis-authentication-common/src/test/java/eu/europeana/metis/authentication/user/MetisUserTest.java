package eu.europeana.metis.authentication.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Date;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Tzanakis
 * @since 2020-09-11
 */
class MetisUserTest {

  @Test
  void testEmptyMetisUserModelConstructor() {
    final MetisUser metisUser = new MetisUser();

    assertNull(metisUser.getUserId());
    assertNull(metisUser.getEmail());
    assertNull(metisUser.getFirstName());
    assertNull(metisUser.getLastName());
    assertNull(metisUser.getPassword());
    assertNull(metisUser.getOrganizationId());
    assertNull(metisUser.getOrganizationName());
    assertNull(metisUser.getAccountRole());
    assertNull(metisUser.getCountry());
    assertFalse(metisUser.isNetworkMember());
    assertFalse(metisUser.isMetisUserFlag());
    assertNull(metisUser.getCreatedDate());
    assertNull(metisUser.getUpdatedDate());
    assertNull(metisUser.getMetisUserAccessToken());
  }

  @Test
  void testMetisUserModelConstructor() {
    final String email = "example@email.com";
    final Date timestampDate = new Date();
    final MetisUserAccessToken accessToken = new MetisUserAccessToken(email, "accessToken",
        timestampDate);

    final MetisUser metisUser = new MetisUser();
    final Date createdDate = new Date();
    final Date updatedDate = new Date();
    metisUser.setUserId("UserId");
    metisUser.setEmail(email);
    metisUser.setFirstName("FirstName");
    metisUser.setLastName("LastName");
    metisUser.setPassword("Password");
    metisUser.setOrganizationId("OrganizationId");
    metisUser.setOrganizationName("OrganizationName");
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    metisUser.setCountry("Country");
    metisUser.setNetworkMember(true);
    metisUser.setMetisUserFlag(true);
    metisUser.setCreatedDate(createdDate);
    metisUser.setUpdatedDate(updatedDate);
    metisUser.setMetisUserAccessToken(accessToken);

    assertEquals("UserId", metisUser.getUserId());
    assertEquals(email, metisUser.getEmail());
    assertEquals("FirstName", metisUser.getFirstName());
    assertEquals("LastName", metisUser.getLastName());
    assertEquals("Password", metisUser.getPassword());
    assertEquals("OrganizationId", metisUser.getOrganizationId());
    assertEquals("OrganizationName", metisUser.getOrganizationName());
    assertEquals(AccountRole.EUROPEANA_DATA_OFFICER, metisUser.getAccountRole());
    assertEquals("Country", metisUser.getCountry());
    assertTrue(metisUser.isNetworkMember());
    assertTrue(metisUser.isMetisUserFlag());
    assertEquals(createdDate, metisUser.getCreatedDate());
    assertEquals(updatedDate, metisUser.getUpdatedDate());

    assertEquals(email, metisUser.getMetisUserAccessToken().getEmail());
    assertEquals(accessToken.getAccessToken(),
        metisUser.getMetisUserAccessToken().getAccessToken());
    assertEquals(accessToken.getTimestamp(),
        metisUser.getMetisUserAccessToken().getTimestamp());

  }
}