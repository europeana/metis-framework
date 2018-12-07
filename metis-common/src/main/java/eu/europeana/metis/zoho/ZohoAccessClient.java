package eu.europeana.metis.zoho;

import com.zoho.crm.library.api.response.BulkAPIResponse;
import com.zoho.crm.library.crud.ZCRMModule;
import com.zoho.crm.library.crud.ZCRMRecord;
import com.zoho.crm.library.crud.ZCRMTrashRecord;
import com.zoho.crm.library.exception.ZCRMException;
import com.zoho.crm.library.setup.restclient.ZCRMRestClient;
import com.zoho.oauth.client.ZohoOAuthClient;
import com.zoho.oauth.common.ZohoOAuthException;
import eu.europeana.metis.exception.BadContentException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that contains methods related to communication with the Zoho service.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-12-06
 */
public class ZohoAccessClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZohoAccessClient.class);
  private static final int ITEMS_PER_PAGE = 200;
  private final ZCRMModule zcrmModuleContacts;
  private final ZCRMModule zcrmModuleAccounts;

  /**
   * Constructor with the grant token provided as a parameter.
   * <p>
   * It will try to initialize the connection with the Zoho service. Uses the grant token for the
   * initial setup to generate the access and refresh tokens. If the grant token was used once
   * before already then the exception that would normally the initialization will throw, will be
   * caught instead and logged. The process will continue and it will try to use the already
   * generated access/refresh tokens.
   * </p>
   *
   * @param grantToken the grant token key to be used
   * @throws Exception if an exception occurred during initialization
   */
  public ZohoAccessClient(String grantToken) throws Exception {
    ZCRMRestClient.initialize();
    ZohoOAuthClient cli = ZohoOAuthClient.getInstance();
    try {
      cli.generateAccessToken(grantToken);
    } catch (ZohoOAuthException e) {
      LOGGER
          .warn(
              "THIS EXCEPTION IS PROBABLY NORMAL IF THE GRANT TOKEN HAS BEEN SUCCESSFULLY USED ONCE ALREADY!"
                  + "Exception when generating access token. Grant tokens can be used only once, "
                  + "so when the access and refresh tokens are generated, the grant token becomes obsolete on subsequent deployments",
              e);
    }

    zcrmModuleContacts = ZCRMModule.getInstance(ZohoConstants.CONTACTS_MODULE_NAME);
    zcrmModuleAccounts = ZCRMModule.getInstance(ZohoConstants.ACCOUNTS_MODULE_NAME);
  }

  /**
   * Get a contact by using an email.
   *
   * @param email the email to search for
   * @return the contact corresponding to the email
   * @throws BadContentException if an exception occurred during searching
   */
  public ZCRMRecord getZcrmRecordContactByEmail(String email) throws BadContentException {
    final BulkAPIResponse bulkAPIResponseContacts;
    try {
      bulkAPIResponseContacts = zcrmModuleContacts.searchByEmail(email);
    } catch (ZCRMException e) {
      throw new BadContentException("Zoho search by email threw an exception", e);
    }
    final List<ZCRMRecord> zcrmRecords = (List<ZCRMRecord>) bulkAPIResponseContacts.getData();
    return zcrmRecords.isEmpty() ? null : zcrmRecords.get(0);
  }


  /**
   * Get a contact by using an organization email.
   *
   * @param organizationName the organization name to search for
   * @return the organization corresponding to the organization name
   * @throws BadContentException if an exception occurred during searching
   */
  public ZCRMRecord getZcrmRecordOrganizationByName(String organizationName)
      throws BadContentException {
    final BulkAPIResponse bulkAPIResponseAccounts;
    try {
      bulkAPIResponseAccounts = zcrmModuleAccounts
          .searchByCriteria(
              String.format(ZohoConstants.ZOHO_OPERATION_FORMAT_STRING,
                  ZohoConstants.ACCOUNT_NAME_FIELD,
                  ZohoConstants.EQUALS_OPERATION, organizationName));
    } catch (ZCRMException e) {
      throw new BadContentException(
          "Zoho search organization by organization name threw an exception", e);
    }
    final List<ZCRMRecord> zcrmRecords = (List<ZCRMRecord>) bulkAPIResponseAccounts.getData();
    return zcrmRecords.isEmpty() ? null : zcrmRecords.get(0);
  }

  /**
   * Get a contact by using an organization id.
   *
   * @param organizationId the organization id to search for
   * @return the organization corresponding to the organization id
   * @throws BadContentException if an exception occurred during searching
   */
  public ZCRMRecord getZcrmRecordOrganizationById(String organizationId)
      throws BadContentException {
    final BulkAPIResponse bulkAPIResponseAccounts;
    try {
      bulkAPIResponseAccounts = zcrmModuleAccounts
          .searchByCriteria(
              String.format(ZohoConstants.ZOHO_OPERATION_FORMAT_STRING, ZohoConstants.ID_FIELD,
                  ZohoConstants.EQUALS_OPERATION,
                  organizationId));
    } catch (ZCRMException e) {
      throw new BadContentException(
          "Zoho search organization by organization id threw an exception", e);
    }
    final List<ZCRMRecord> zcrmRecords = (List<ZCRMRecord>) bulkAPIResponseAccounts.getData();
    return zcrmRecords.isEmpty() ? null : zcrmRecords.get(0);
  }

  /**
   * Get deleted organization items paged.
   *
   * @param startPage The number of the item from which the paging should start. First item is at
   * number 1. Uses default number of items per page.
   * @return the list of deleted Zoho Organizations
   * @throws BadContentException if an error occurred during accessing Zoho
   */
  public List<ZCRMTrashRecord> getZCRMTrashRecordDeletedOrganizations(int startPage)
      throws BadContentException {

    if (startPage < 1) {
      throw new BadContentException(
          "Invalid start page index. Index must be >= 1",
          new IllegalArgumentException("start page: " + startPage));
    }
    final BulkAPIResponse bulkAPIResponseDeletedRecords;
    try {
      bulkAPIResponseDeletedRecords = zcrmModuleAccounts
          .getDeletedRecords(startPage, ITEMS_PER_PAGE);
    } catch (ZCRMException e) {
      throw new BadContentException("Cannot get deleted organization list from: "
          + startPage, e);
    }
    return (List<ZCRMTrashRecord>) bulkAPIResponseDeletedRecords.getData();
  }

  /**
   * Get organization items paged, filtering by modifiedDate date.
   *
   * @param start first index starts with 1
   * @param rows the number of entries to be returned, Zoho will have an upper limit
   * @param modifiedDate the date of last modification to check
   * @return the list of Zoho Organizations
   * @throws BadContentException if an error occurred during accessing Zoho
   */
  public List<ZCRMRecord> getZcrmRecordOrganizations(int start, int rows, Date modifiedDate)
      throws BadContentException {
    return getZcrmRecordOrganizations(start, rows, modifiedDate, null);
  }

  /**
   * Get organization items paged, filtering by modifiedDate date and searchCriteria.
   *
   * @param start first index starts with 1
   * @param rows the number of entries to be returned, Zoho will have an upper limit
   * @param modifiedDate the date of last modification to check
   * @param searchCriteria the searchCriteria to apply during the Zoho search
   * @return the list of Zoho Organizations
   * @throws BadContentException if an error occurred during accessing Zoho
   */
  public List<ZCRMRecord> getZcrmRecordOrganizations(int start, int rows, Date modifiedDate,
      Map<String, String> searchCriteria) throws BadContentException {

    if (start < 1) {
      throw new BadContentException(
          "Invalid start index. Index must be >= 1",
          new IllegalArgumentException("start: " + start));
    }

    String modifiedDateString = null;
    if (modifiedDate != null) {
      modifiedDateString = ZohoConstants.ZOHO_DATE_FORMATTER.format(modifiedDate);
    }

    final BulkAPIResponse bulkAPIResponse;
    try {
      if (searchCriteria == null || searchCriteria.isEmpty()) {//No searchCriteria available
        bulkAPIResponse = zcrmModuleAccounts
            .getRecords(null, null, null, start, rows, modifiedDateString, null, false);
      } else {
        bulkAPIResponse = zcrmModuleAccounts
            .searchByCriteria(createZohoCriteriaString(searchCriteria, modifiedDateString), start,
                rows);
      }
    } catch (ZCRMException e) {
      throw new BadContentException(
          "Cannot get organization list from: " + start + " rows :" + rows, e);
    }

    return (List<ZCRMRecord>) bulkAPIResponse.getData();
  }

  /**
   * Using the search criteria provided and the modifiedDate if available it will create the
   * criteria in the format that Zoho accepts. Result will be depicted as
   * "(field1:equals:valueA)OR(field1:equals:valueB)OR(field2:equals:valueC)" or "".
   *
   * @param searchCriteria the search criteria map provided, values can be comma separated per key
   * @param modifiedDateString the modified date that should be checked if any
   * @return the created criteria in the format Zoho accepts
   */
  private String createZohoCriteriaString(Map<String, String> searchCriteria,
      String modifiedDateString) {
    if (searchCriteria == null || searchCriteria.isEmpty()) {
      searchCriteria = new HashMap<>();
    }
    if (StringUtils.isNotBlank(modifiedDateString)) {
      searchCriteria.put(ZohoConstants.LAST_ACTIVITY_TIME_FIELD, modifiedDateString);
    }

    return searchCriteria.entrySet().stream().map(entry ->
        Arrays.stream(entry.getValue().split(ZohoConstants.DELIMITER_COMMA))
            .map(value -> String
                .format(ZohoConstants.ZOHO_OPERATION_FORMAT_STRING, entry.getKey(),
                    ZohoConstants.EQUALS_OPERATION, value.trim())).collect(
            Collectors.joining(ZohoConstants.OR))).collect(Collectors.joining(ZohoConstants.OR));
  }
}
