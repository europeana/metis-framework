package eu.europeana.metis.authentication.utils;

import com.zoho.crm.library.crud.ZCRMRecord;
import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.zoho.ZohoConstants;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import org.springframework.util.CollectionUtils;

/**
 * Class that contains utility methods for interaction between Zoho Contacts and {@link MetisUser}
 * objects.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-12-06
 */
public final class ZohoMetisUserUtils {

  private ZohoMetisUserUtils() {
  }

  /**
   * Checks fields from an incoming {@link ZCRMRecord} object(Contact Zoho) and returns a new user
   * with the relevant fields populated.
   *
   * @param zcrmRecord the object coming from Zoho
   * @return the metis user with its fields populated
   * @throws BadContentException if a problem occurs during parsing of the fields
   */
  public static MetisUser checkZohoFieldsAndPopulateMetisUser(ZCRMRecord zcrmRecord)
      throws BadContentException {
    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX");
    HashMap<String, Object> zohoFields = zcrmRecord.getData();

    final MetisUser metisUser = new MetisUser();

    metisUser.setUserId(Long.toString(zcrmRecord.getEntityId()));
    metisUser.setFirstName((String) zohoFields.get(ZohoConstants.FIRST_NAME_FIELD));
    metisUser.setLastName((String) zohoFields.get(ZohoConstants.LAST_NAME_FIELD));
    metisUser.setEmail((String) zohoFields.get(ZohoConstants.EMAIL_FIELD));
    try {
      metisUser.setCreatedDate(zcrmRecord.getCreatedTime() == null ? null
          : dateFormat.parse(zcrmRecord.getCreatedTime()));
      metisUser.setUpdatedDate(zcrmRecord.getModifiedTime() == null ? null
          : dateFormat.parse(zcrmRecord.getModifiedTime()));
    } catch (ParseException ex) {
      throw new BadContentException("Created or updated date could not be parsed.");
    }
    metisUser.setCountry((String) zohoFields.get(ZohoConstants.USER_COUNTRY_FIELD));
    final List<String> participationLevel = (List<String>) (zohoFields
        .get(ZohoConstants.PARTICIPATION_LEVEL_FIELD));
    if (!CollectionUtils.isEmpty(participationLevel) && participationLevel
        .contains("Network Association Member")) {
      metisUser.setNetworkMember(true);
    }
    if (zohoFields.get(ZohoConstants.METIS_USER_FIELD) != null) {
      metisUser.setMetisUserFlag((Boolean) zohoFields.get(ZohoConstants.METIS_USER_FIELD));
    }

    metisUser.setAccountRole(AccountRole
        .getAccountRoleFromEnumName((String) zohoFields.get(ZohoConstants.ACCOUNT_ROLE_FIELD)));
    if (metisUser.getAccountRole() == AccountRole.METIS_ADMIN) {
      throw new BadContentException("Account Role in Zoho is not valid");
    }
    final ZCRMRecord accountName = (ZCRMRecord) zohoFields
        .get(ZohoConstants.ACCOUNT_NAME_FIELD);
    metisUser.setOrganizationId(Long.toString(accountName.getEntityId()));
    metisUser.setOrganizationName(accountName.getLookupLabel());

    return metisUser;
  }
}
