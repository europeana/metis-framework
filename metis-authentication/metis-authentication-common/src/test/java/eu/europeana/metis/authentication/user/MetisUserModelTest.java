package eu.europeana.metis.authentication.user;

import java.util.Date;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Tzanakis
 * @since 2020-09-11
 */
class MetisUserModelTest {

  @Test
  void testEmptyMetisUserModelConstructor() {
    final MetisUserModel metisUserModel = new MetisUserModel();

    Assertions.assertNull(metisUserModel.getUserId());
    Assertions.assertNull(metisUserModel.getEmail());
    Assertions.assertNull(metisUserModel.getFirstName());
    Assertions.assertNull(metisUserModel.getLastName());
    Assertions.assertNull(metisUserModel.getPassword());
    Assertions.assertNull(metisUserModel.getOrganizationId());
    Assertions.assertNull(metisUserModel.getOrganizationName());
    Assertions.assertNull(metisUserModel.getAccountRole());
    Assertions.assertNull(metisUserModel.getCountry());
    Assertions.assertFalse(metisUserModel.isNetworkMember());
    Assertions.assertFalse(metisUserModel.isMetisUserFlag());
    Assertions.assertNull(metisUserModel.getCreatedDate());
    Assertions.assertNull(metisUserModel.getUpdatedDate());
    Assertions.assertNull(metisUserModel.getMetisUserAccessToken());
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

    Assertions.assertEquals("UserId", metisUserModel.getUserId());
    Assertions.assertEquals(email, metisUserModel.getEmail());
    Assertions.assertEquals("FirstName", metisUserModel.getFirstName());
    Assertions.assertEquals("LastName", metisUserModel.getLastName());
    Assertions.assertEquals("Password", metisUserModel.getPassword());
    Assertions.assertEquals("OrganizationId", metisUserModel.getOrganizationId());
    Assertions.assertEquals("OrganizationName", metisUserModel.getOrganizationName());
    Assertions.assertEquals(AccountRole.EUROPEANA_DATA_OFFICER, metisUserModel.getAccountRole());
    Assertions.assertEquals("Country", metisUserModel.getCountry());
    Assertions.assertTrue(metisUserModel.isNetworkMember());
    Assertions.assertTrue(metisUserModel.isMetisUserFlag());
    Assertions.assertEquals(createdDate, metisUserModel.getCreatedDate());
    Assertions.assertEquals(updatedDate, metisUserModel.getUpdatedDate());

    Assertions.assertEquals(email, metisUserModel.getMetisUserAccessToken().getEmail());
    Assertions.assertEquals(accessToken.getAccessToken(),
        metisUserModel.getMetisUserAccessToken().getAccessToken());
    Assertions.assertEquals(accessToken.getTimestamp(),
        metisUserModel.getMetisUserAccessToken().getTimestamp());

  }
}