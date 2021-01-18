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
  void testEmptyMetisUserConstructor() {
    final MetisUser metisUser = new MetisUser();

    assertNull(metisUser.getUserId());
    assertNull(metisUser.getEmail());
    assertNull(metisUser.getFirstName());
    assertNull(metisUser.getLastName());
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
  void testMetisUserConstructor() {
    final String email = "example@email.com";
    final Date timestampDate = new Date();
    final MetisUserAccessToken accessToken = new MetisUserAccessToken(email, "accessToken",
        timestampDate);

    final MetisUserModel metisUserModel = new MetisUserModel();
    final Date createdDate = new Date();
    final Date updatedDate = new Date();
    metisUserModel.setUserId("UserId");
    metisUserModel.setEmail(email);
    metisUserModel.setFirstName("FirstName");
    metisUserModel.setLastName("LastName");
    metisUserModel.setOrganizationId("OrganizationId");
    metisUserModel.setOrganizationName("OrganizationName");
    metisUserModel.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    metisUserModel.setCountry("Country");
    metisUserModel.setNetworkMember(true);
    metisUserModel.setMetisUserFlag(true);
    metisUserModel.setCreatedDate(createdDate);
    metisUserModel.setUpdatedDate(updatedDate);
    metisUserModel.setMetisUserAccessToken(accessToken);
    final MetisUser metisUser = new MetisUser(metisUserModel);

    assertEquals("UserId", metisUser.getUserId());
    assertEquals(email, metisUser.getEmail());
    assertEquals("FirstName", metisUser.getFirstName());
    assertEquals("LastName", metisUser.getLastName());
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