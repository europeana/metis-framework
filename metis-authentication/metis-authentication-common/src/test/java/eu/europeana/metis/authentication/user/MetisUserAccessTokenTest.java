package eu.europeana.metis.authentication.user;

import java.util.Date;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Tzanakis
 * @since 2020-09-11
 */
class MetisUserAccessTokenTest {

  @Test
  void testEmptyMetisUserAccessTokenConstructor() {
    final MetisUserAccessToken metisUserAccessToken = new MetisUserAccessToken();

    Assertions.assertNull(metisUserAccessToken.getEmail());
    Assertions.assertNull(metisUserAccessToken.getAccessToken());
    Assertions.assertNull(metisUserAccessToken.getTimestamp());
  }

  @Test
  void testMetisUserAccessTokenConstructor() {
    final Date timestampDate = new Date();
    final MetisUserAccessToken metisUserAccessToken = new MetisUserAccessToken("example@email.com",
        "AccessToken", timestampDate);

    Assertions.assertEquals("example@email.com", metisUserAccessToken.getEmail());
    Assertions.assertEquals("AccessToken", metisUserAccessToken.getAccessToken());
    Assertions.assertEquals(timestampDate, metisUserAccessToken.getTimestamp());
  }

  @Test
  void testMetisUserAccessTokenSetters() {
    final Date timestampDate = new Date();
    final MetisUserAccessToken metisUserAccessToken = new MetisUserAccessToken();

    metisUserAccessToken.setEmail("example@email.com");
    metisUserAccessToken.setAccessToken("AccessToken");
    metisUserAccessToken.setTimestamp(timestampDate);
    Assertions.assertEquals("example@email.com", metisUserAccessToken.getEmail());
    Assertions.assertEquals("AccessToken", metisUserAccessToken.getAccessToken());
    Assertions.assertEquals(timestampDate, metisUserAccessToken.getTimestamp());
  }

}