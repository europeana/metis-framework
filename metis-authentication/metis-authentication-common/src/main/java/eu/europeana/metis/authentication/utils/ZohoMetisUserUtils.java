package eu.europeana.metis.authentication.utils;

import static eu.europeana.metis.zoho.ZohoUtils.stringFieldSupplier;

import com.zoho.crm.api.record.Record;
import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserView;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.zoho.ZohoConstants;
import java.sql.Date;
import java.util.List;
import org.springframework.util.CollectionUtils;

/**
 * Class that contains utility methods for interaction between Zoho Contacts and {@link MetisUserView}
 * objects.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-12-06
 */
public final class ZohoMetisUserUtils {

  private ZohoMetisUserUtils() {
  }

  /**
   * Checks fields from an incoming {@link Record} object(Contact Zoho) and returns a new user with the relevant fields
   * populated.
   *
   * @param zohoRecord the object coming from Zoho
   * @return the metis user with its fields populated
   * @throws BadContentException if a problem occurs during parsing of the fields
   */
  public static MetisUser checkZohoFieldsAndPopulateMetisUser(Record zohoRecord)
      throws BadContentException {

    final MetisUser metisUser = new MetisUser();

    metisUser.setUserId(Long.toString(zohoRecord.getId()));
    metisUser.setFirstName(stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.FIRST_NAME_FIELD)));
    metisUser.setLastName(stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.LAST_NAME_FIELD)));
    metisUser.setEmail(stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.EMAIL_FIELD)));
    metisUser.setCreatedDate(
        zohoRecord.getCreatedTime() == null ? null : Date.from(zohoRecord.getCreatedTime().toInstant()));
    metisUser.setUpdatedDate(
        zohoRecord.getModifiedTime() == null ? null : Date.from(zohoRecord.getModifiedTime().toInstant()));
    metisUser.setCountry(stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.USER_COUNTRY_FIELD)));
    final List<String> participationLevel = (List<String>) (zohoRecord.getKeyValue(ZohoConstants.PARTICIPATION_LEVEL_FIELD));
    if (!CollectionUtils.isEmpty(participationLevel) && participationLevel
        .contains("Network Association Member")) {
      metisUser.setNetworkMember(true);
    }
    if (zohoRecord.getKeyValue(ZohoConstants.METIS_USER_FIELD) != null) {
      metisUser.setMetisUserFlag((Boolean) zohoRecord.getKeyValue(ZohoConstants.METIS_USER_FIELD));
    }

    metisUser.setAccountRole(AccountRole.getAccountRoleFromEnumName(
        stringFieldSupplier(zohoRecord.getKeyValue(ZohoConstants.ACCOUNT_ROLE_FIELD))));
    if (metisUser.getAccountRole() == AccountRole.METIS_ADMIN) {
      throw new BadContentException("Account Role in Zoho is not valid");
    }
    final Record accountName = (Record) zohoRecord.getKeyValue(ZohoConstants.ACCOUNT_NAME_FIELD);
    metisUser.setOrganizationId(Long.toString(accountName.getId()));
    metisUser.setOrganizationName(stringFieldSupplier(accountName.getKeyValue(ZohoConstants.NAME_FIELD)));

    return metisUser;
  }
}
