package eu.europeana.metis.authentication.utils;

import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserView;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

/**
 * Class that contains utility methods for interaction between Zoho Contacts and {@link MetisUserView} objects.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018 -12-06
 */
public final class MetisUserUtils {

  public static final String EUROPEANA_ORGANIZATION_ID = "1482250000001617026";
  public static final String EUROPEANA_ORGANIZATION_NAME = "Europeana Foundation";
  public static final String DEFAULT_NAME = "firstName";
  public static final String DEFAULT_LAST_NAME = "lastName";
  public static final String DEFAULT_COUNTRY = "Netherlands";

  private MetisUserUtils() {
  }

  /**
   * Check zoho fields and populate metis user.
   *
   * @param email the email
   * @return the metis user
   */
  public static MetisUser checkFieldsAndPopulateMetisUser(String email) {

    final MetisUser metisUser = new MetisUser();
    final long genId = UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE;
    metisUser.setUserId(Long.toString(genId));
    metisUser.setFirstName(DEFAULT_NAME);
    metisUser.setLastName(DEFAULT_LAST_NAME);
    metisUser.setEmail(email);
    metisUser.setCreatedDate(Date.from(Instant.now()));
    metisUser.setUpdatedDate(Date.from(Instant.now()));
    metisUser.setCountry(DEFAULT_COUNTRY);
    metisUser.setNetworkMember(true);
    metisUser.setMetisUserFlag(true);
    metisUser.setAccountRole(AccountRole.EUROPEANA_DATA_OFFICER);
    metisUser.setOrganizationId(EUROPEANA_ORGANIZATION_ID);
    metisUser.setOrganizationName(EUROPEANA_ORGANIZATION_NAME);

    return metisUser;
  }
}
