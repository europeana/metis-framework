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
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.time.FastDateFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-12-06
 */
public class ZohoAccessClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZohoAccessClient.class);
  private static final int ITEMS_PER_PAGE = 200;
  private static final FastDateFormat formatter = FastDateFormat
      .getInstance(ZohoConstants.ZOHO_TIME_FORMAT);
  private final ZCRMModule zcrmModule;
  private final ZCRMModule zcrmModuleAccounts;

  public ZohoAccessClient(String grantToken) throws Exception {
    ZCRMRestClient.initialize();
    ZohoOAuthClient cli = ZohoOAuthClient.getInstance();
    try {
      cli.generateAccessToken(grantToken);
    } catch (ZohoOAuthException ex) {
      LOGGER
          .warn("Exception when generating access token. Grant tokens can be used only once, "
              + "so when the access and refresh tokens are generated, the grant token becomes obsolete on subsequent deployments");
    }

    zcrmModule = ZCRMModule.getInstance(ZohoConstants.CONTACTS_MODULE);
    zcrmModuleAccounts = ZCRMModule.getInstance(ZohoConstants.ACCOUNTS_MODULE);
  }

  public ZCRMRecord getZcrmRecordContactByEmail(String email) throws BadContentException {
    final BulkAPIResponse bulkAPIResponseContacts;
    try {
      bulkAPIResponseContacts = zcrmModule.searchByEmail(email);
    } catch (ZCRMException e) {
      throw new BadContentException("Zoho search by email threw an exception", e);
    }
    final List<ZCRMRecord> zcrmRecords = (List<ZCRMRecord>) bulkAPIResponseContacts.getData();
    if (zcrmRecords.isEmpty()) {
      return null;
    }

    return zcrmRecords.get(0);
  }


  public ZCRMRecord getZcrmRecordOrganizationByName(String organizationName)
      throws BadContentException {
    final BulkAPIResponse bulkAPIResponseAccounts;
    try {
      bulkAPIResponseAccounts = zcrmModuleAccounts
          .searchByCriteria(
              String.format("(%s:%s:%s)", ZohoConstants.ACCOUNT_NAME_FIELD,
                  ZohoConstants.EQUALS_OPERATION, organizationName));
    } catch (ZCRMException e) {
      throw new BadContentException(
          "Zoho search organization by organization name threw an exception", e);
    }
    final List<ZCRMRecord> zcrmRecords = (List<ZCRMRecord>) bulkAPIResponseAccounts.getData();
    if (zcrmRecords.isEmpty()) {
      return null;
    }

    return zcrmRecords.get(0);
  }

  public ZCRMRecord getZcrmRecordOrganizationById(String organizationId)
      throws BadContentException {
    final BulkAPIResponse bulkAPIResponseAccounts;
    try {
      bulkAPIResponseAccounts = zcrmModuleAccounts
          .searchByCriteria(
              String.format("(%s:%s:%s)", ZohoConstants.ID_FIELD, ZohoConstants.EQUALS_OPERATION,
                  organizationId));
    } catch (ZCRMException e) {
      throw new BadContentException(
          "Zoho search organization by organization id threw an exception", e);
    }
    final List<ZCRMRecord> zcrmRecords = (List<ZCRMRecord>) bulkAPIResponseAccounts.getData();
    if (zcrmRecords.isEmpty()) {
      return null;
    }

    return zcrmRecords.get(0);
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
   * Get organization items paged, filtering by lastModified date.
   *
   * @param start first index starts with 1
   * @param rows the number of entries to be returned, Zoho will have an upper limit
   * @param lastModified the date of last modification to check
   * @return the list of Zoho Organizations
   * @throws BadContentException if an error occurred during accessing Zoho
   */
  public List<ZCRMRecord> getOrganizations(int start, int rows,
      Date lastModified) throws BadContentException {
    return getZcrmRecordOrganizations(start, rows, lastModified, null);
  }

  /**
   * Get organization items paged, filtering by lastModified date and searchCriteria.
   *
   * @param start first index starts with 1
   * @param rows the number of entries to be returned, Zoho will have an upper limit
   * @param lastModified the date of last modification to check
   * @param searchCriteria the searchCriteria to apply during the Zoho search
   * @return the list of Zoho Organizations
   * @throws BadContentException if an error occurred during accessing Zoho
   */
  public List<ZCRMRecord> getZcrmRecordOrganizations(int start, int rows,
      Date lastModified, Map<String, String> searchCriteria) throws BadContentException {

    if (start < 1) {
      throw new BadContentException(
          "Invalid start index. Index must be >= 1",
          new IllegalArgumentException("start: " + start));
    }

    String lastModifiedTime = null;
    if (lastModified != null) {
      lastModifiedTime = formatter.format(lastModified);
    }

    final BulkAPIResponse bulkAPIResponseRecordsOrganizations;
    try {
      if (searchCriteria == null || searchCriteria.isEmpty()) {//No searchCriteria available
        bulkAPIResponseRecordsOrganizations = zcrmModuleAccounts
            .getRecords(null, null, null, start, rows, lastModifiedTime, null, false);
      } else {
        // TODO: 5-12-18 If last modified is required here it should be part of the criteria and the method that creates the string below it should support it
        bulkAPIResponseRecordsOrganizations = zcrmModuleAccounts
            .searchByCriteria(createZohoCriteriaString(searchCriteria), start, rows);
      }
    } catch (ZCRMException e) {
      throw new BadContentException(
          "Cannot get organization list from: " + start + " rows :" + rows, e);
    }

    return (List<ZCRMRecord>) bulkAPIResponseRecordsOrganizations.getData();
  }

  // TODO: 5-12-18 This criteria creation is for equals only, for last modified there should be an extension here
  private String createZohoCriteriaString(Map<String, String> searchCriteria) {
    if (searchCriteria == null || searchCriteria.isEmpty()) {
      return null;
    }

    String[] filterCriteria;
    StringBuilder criteriaStringBuilder = new StringBuilder();

    for (Map.Entry<String, String> entry : searchCriteria.entrySet()) {
      criteriaStringBuilder = new StringBuilder();
      filterCriteria = entry.getValue().split(ZohoConstants.DELIMITER_COMMA);

      for (String filter : filterCriteria) {
        criteriaStringBuilder.append(String
            .format("(%s:%s:%s)", entry.getKey(), ZohoConstants.EQUALS_OPERATION, filter.trim()));
        criteriaStringBuilder.append(ZohoConstants.OR);
      }
    }

    // remove last OR
    criteriaStringBuilder.delete(criteriaStringBuilder.length() - ZohoConstants.OR.length(),
        criteriaStringBuilder.length());
    return criteriaStringBuilder.toString();

  }
}
