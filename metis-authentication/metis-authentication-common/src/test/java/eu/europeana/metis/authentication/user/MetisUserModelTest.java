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
class MetisUserModelTest {

  @Test
  void testEmptyMetisUserModelConstructor() {
    final MetisUserModel metisUserModel = new MetisUserModel();

    assertNull(metisUserModel.getUserId());
    assertNull(metisUserModel.getEmail());
    assertNull(metisUserModel.getFirstName());
    assertNull(metisUserModel.getLastName());
    assertNull(metisUserModel.getPassword());
    assertNull(metisUserModel.getOrganizationId());
    assertNull(metisUserModel.getOrganizationName());
    assertNull(metisUserModel.getAccountRole());
    assertNull(metisUserModel.getCountry());
    assertFalse(metisUserModel.isNetworkMember());
    assertFalse(metisUserModel.isMetisUserFlag());
    assertNull(metisUserModel.getCreatedDate());
    assertNull(metisUserModel.getUpdatedDate());
    assertNull(metisUserModel.getMetisUserAccessToken());
  }

  @Test
  void testMetisUserModelConstructor() {
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
    metisUserModel.setPassword("Password");
    metisUserModel.setOrganizationId("OrganizationId");
    metisUserModel.setOrganizationName("OrganizationName");
    metisUserModel.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    metisUserModel.setCountry("Country");
    metisUserModel.setNetworkMember(true);
    metisUserModel.setMetisUserFlag(true);
    metisUserModel.setCreatedDate(createdDate);
    metisUserModel.setUpdatedDate(updatedDate);
    metisUserModel.setMetisUserAccessToken(accessToken);

    assertEquals("UserId", metisUserModel.getUserId());
    assertEquals(email, metisUserModel.getEmail());
    assertEquals("FirstName", metisUserModel.getFirstName());
    assertEquals("LastName", metisUserModel.getLastName());
    assertEquals("Password", metisUserModel.getPassword());
    assertEquals("OrganizationId", metisUserModel.getOrganizationId());
    assertEquals("OrganizationName", metisUserModel.getOrganizationName());
    assertEquals(AccountRole.EUROPEANA_DATA_OFFICER, metisUserModel.getAccountRole());
    assertEquals("Country", metisUserModel.getCountry());
    assertTrue(metisUserModel.isNetworkMember());
    assertTrue(metisUserModel.isMetisUserFlag());
    assertEquals(createdDate, metisUserModel.getCreatedDate());
    assertEquals(updatedDate, metisUserModel.getUpdatedDate());

    assertEquals(email, metisUserModel.getMetisUserAccessToken().getEmail());
    assertEquals(accessToken.getAccessToken(),
        metisUserModel.getMetisUserAccessToken().getAccessToken());
    assertEquals(accessToken.getTimestamp(),
        metisUserModel.getMetisUserAccessToken().getTimestamp());

  }
}