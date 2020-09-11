package eu.europeana.metis.authentication.user;

import java.util.Date;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Tzanakis
 * @since 2020-09-11
 */
class MetisUserTest {

  @Test
  void testEmptyMetisUserConstructor() {
    final MetisUser metisUser = new MetisUser();

    Assertions.assertNull(metisUser.getUserId());
    Assertions.assertNull(metisUser.getEmail());
    Assertions.assertNull(metisUser.getFirstName());
    Assertions.assertNull(metisUser.getLastName());
    Assertions.assertNull(metisUser.getOrganizationId());
    Assertions.assertNull(metisUser.getOrganizationName());
    Assertions.assertNull(metisUser.getAccountRole());
    Assertions.assertNull(metisUser.getCountry());
    Assertions.assertFalse(metisUser.isNetworkMember());
    Assertions.assertFalse(metisUser.isMetisUserFlag());
    Assertions.assertNull(metisUser.getCreatedDate());
    Assertions.assertNull(metisUser.getUpdatedDate());
    Assertions.assertNull(metisUser.getMetisUserAccessToken());
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

    Assertions.assertEquals("UserId", metisUser.getUserId());
    Assertions.assertEquals(email, metisUser.getEmail());
    Assertions.assertEquals("FirstName", metisUser.getFirstName());
    Assertions.assertEquals("LastName", metisUser.getLastName());
    Assertions.assertEquals("OrganizationId", metisUser.getOrganizationId());
    Assertions.assertEquals("OrganizationName", metisUser.getOrganizationName());
    Assertions.assertEquals(AccountRole.EUROPEANA_DATA_OFFICER, metisUser.getAccountRole());
    Assertions.assertEquals("Country", metisUser.getCountry());
    Assertions.assertTrue(metisUser.isNetworkMember());
    Assertions.assertTrue(metisUser.isMetisUserFlag());
    Assertions.assertEquals(createdDate, metisUser.getCreatedDate());
    Assertions.assertEquals(updatedDate, metisUser.getUpdatedDate());

    Assertions.assertEquals(email, metisUser.getMetisUserAccessToken().getEmail());
    Assertions.assertEquals(accessToken.getAccessToken(),
        metisUser.getMetisUserAccessToken().getAccessToken());
    Assertions.assertEquals(accessToken.getTimestamp(),
        metisUser.getMetisUserAccessToken().getTimestamp());

  }
}