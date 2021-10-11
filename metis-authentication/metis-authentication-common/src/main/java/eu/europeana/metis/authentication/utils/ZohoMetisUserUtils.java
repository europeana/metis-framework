package eu.europeana.metis.authentication.utils;

import static eu.europeana.metis.zoho.ZohoUtils.stringFieldSupplier;

import com.zoho.crm.api.record.Record;
import eu.europeana.metis.authentication.user.AccountRole;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.authentication.user.MetisUserModel;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.zoho.ZohoConstants;
import java.sql.Date;
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
   * Checks fields from an incoming {@link Record} object(Contact Zoho) and returns a new user
   * with the relevant fields populated.
   *
   * @param record the object coming from Zoho
   * @return the metis user with its fields populated
   * @throws BadContentException if a problem occurs during parsing of the fields
   */
  public static MetisUserModel checkZohoFieldsAndPopulateMetisUser(Record record)
      throws BadContentException {

    final MetisUserModel metisUser = new MetisUserModel();

    metisUser.setUserId(Long.toString(record.getId()));
    metisUser.setFirstName(stringFieldSupplier(record.getKeyValue(ZohoConstants.FIRST_NAME_FIELD)));
    metisUser.setLastName(stringFieldSupplier(record.getKeyValue(ZohoConstants.LAST_NAME_FIELD)));
    metisUser.setEmail(stringFieldSupplier(record.getKeyValue(ZohoConstants.EMAIL_FIELD)));
    metisUser.setCreatedDate(
        record.getCreatedTime() == null ? null : Date.from(record.getCreatedTime().toInstant()));
    metisUser.setUpdatedDate(
        record.getModifiedTime() == null ? null : Date.from(record.getModifiedTime().toInstant()));
    metisUser.setCountry(stringFieldSupplier(record.getKeyValue(ZohoConstants.USER_COUNTRY_FIELD)));
    final List<String> participationLevel = (List<String>) (record.getKeyValue(ZohoConstants.PARTICIPATION_LEVEL_FIELD));
    if (!CollectionUtils.isEmpty(participationLevel) && participationLevel
        .contains("Network Association Member")) {
      metisUser.setNetworkMember(true);
    }
    if (record.getKeyValue(ZohoConstants.METIS_USER_FIELD) != null) {
      metisUser.setMetisUserFlag((Boolean) record.getKeyValue(ZohoConstants.METIS_USER_FIELD));
    }

    metisUser.setAccountRole(AccountRole.getAccountRoleFromEnumName(
        stringFieldSupplier(record.getKeyValue(ZohoConstants.ACCOUNT_ROLE_FIELD))));
    if (metisUser.getAccountRole() == AccountRole.METIS_ADMIN) {
      throw new BadContentException("Account Role in Zoho is not valid");
    }
    final Record accountName = (Record) record.getKeyValue(ZohoConstants.ACCOUNT_NAME_FIELD);
    metisUser.setOrganizationId(Long.toString(accountName.getId()));
    metisUser.setOrganizationName(stringFieldSupplier(accountName.getKeyValue(ZohoConstants.NAME_FIELD)));

    return metisUser;
  }
}
