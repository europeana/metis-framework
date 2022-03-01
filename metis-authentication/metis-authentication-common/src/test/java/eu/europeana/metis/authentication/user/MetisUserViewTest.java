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
class MetisUserViewTest {

  @Test
  void testEmptyMetisUserConstructor() {
    final MetisUserView metisUserView = new MetisUserView();

    assertNull(metisUserView.getUserId());
    assertNull(metisUserView.getEmail());
    assertNull(metisUserView.getFirstName());
    assertNull(metisUserView.getLastName());
    assertNull(metisUserView.getOrganizationId());
    assertNull(metisUserView.getOrganizationName());
    assertNull(metisUserView.getAccountRole());
    assertNull(metisUserView.getCountry());
    assertFalse(metisUserView.isNetworkMember());
    assertFalse(metisUserView.isMetisUserFlag());
    assertNull(metisUserView.getCreatedDate());
    assertNull(metisUserView.getUpdatedDate());
    assertNull(metisUserView.getMetisUserAccessToken());
  }

  @Test
  void testMetisUserConstructor() {
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
    metisUser.setOrganizationId("OrganizationId");
    metisUser.setOrganizationName("OrganizationName");
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    metisUser.setCountry("Country");
    metisUser.setNetworkMember(true);
    metisUser.setMetisUserFlag(true);
    metisUser.setCreatedDate(createdDate);
    metisUser.setUpdatedDate(updatedDate);
    metisUser.setMetisUserAccessToken(accessToken);
    final MetisUserView metisUserView = new MetisUserView(metisUser);

    assertEquals("UserId", metisUserView.getUserId());
    assertEquals(email, metisUserView.getEmail());
    assertEquals("FirstName", metisUserView.getFirstName());
    assertEquals("LastName", metisUserView.getLastName());
    assertEquals("OrganizationId", metisUserView.getOrganizationId());
    assertEquals("OrganizationName", metisUserView.getOrganizationName());
    assertEquals(AccountRole.EUROPEANA_DATA_OFFICER, metisUserView.getAccountRole());
    assertEquals("Country", metisUserView.getCountry());
    assertTrue(metisUserView.isNetworkMember());
    assertTrue(metisUserView.isMetisUserFlag());
    assertEquals(createdDate, metisUserView.getCreatedDate());
    assertEquals(updatedDate, metisUserView.getUpdatedDate());

    assertEquals(email, metisUserView.getMetisUserAccessToken().getEmail());
    assertEquals(accessToken.getAccessToken(),
        metisUserView.getMetisUserAccessToken().getAccessToken());
    assertEquals(accessToken.getTimestamp(),
        metisUserView.getMetisUserAccessToken().getTimestamp());

  }
}