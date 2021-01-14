package eu.europeana.metis.authentication.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.Date;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Tzanakis
 * @since 2020-09-11
 */
class MetisUserAccessTokenTest {

  @Test
  void testEmptyMetisUserAccessTokenConstructor() {
    final MetisUserAccessToken metisUserAccessToken = new MetisUserAccessToken();

    assertNull(metisUserAccessToken.getEmail());
    assertNull(metisUserAccessToken.getAccessToken());
    assertNull(metisUserAccessToken.getTimestamp());
  }

  @Test
  void testMetisUserAccessTokenConstructor() {
    final Date timestampDate = new Date();
    final MetisUserAccessToken metisUserAccessToken = new MetisUserAccessToken("example@email.com",
        "AccessToken", timestampDate);

    assertEquals("example@email.com", metisUserAccessToken.getEmail());
    assertEquals("AccessToken", metisUserAccessToken.getAccessToken());
    assertEquals(timestampDate, metisUserAccessToken.getTimestamp());
  }

  @Test
  void testMetisUserAccessTokenSetters() {
    final Date timestampDate = new Date();
    final MetisUserAccessToken metisUserAccessToken = new MetisUserAccessToken();

    metisUserAccessToken.setEmail("example@email.com");
    metisUserAccessToken.setAccessToken("AccessToken");
    metisUserAccessToken.setTimestamp(timestampDate);
    assertEquals("example@email.com", metisUserAccessToken.getEmail());
    assertEquals("AccessToken", metisUserAccessToken.getAccessToken());
    assertEquals(timestampDate, metisUserAccessToken.getTimestamp());
  }

}