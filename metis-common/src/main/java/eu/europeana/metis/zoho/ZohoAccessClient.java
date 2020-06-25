package eu.europeana.metis.zoho;

import com.zoho.crm.library.api.response.BulkAPIResponse;
import com.zoho.crm.library.common.ZCRMEntity;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;

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
   * initial setup to generate the access and refresh tokens. If the grant token was already used
   * once before, then the exception that the initialization will throw, will be caught instead and
   * logged. The process will continue and it will try to use the already generated access/refresh
   * tokens from the persistence storage.
   * </p>
   *
   * @param grantToken the grant token key to be used. Can be null
   * @param zcrmConfigurations the key value map of zoho configuration
   * @throws ZohoOAuthException if initialization of {@link ZCRMRestClient} failed.
   */
  public ZohoAccessClient(@Nullable String grantToken, HashMap<String, String> zcrmConfigurations)
      throws ZohoOAuthException {
    try {
      ZCRMRestClient.initialize(zcrmConfigurations);
    } catch (Exception ex) {
      LOGGER.error("ZCRMRestClient failure!");
      throw new ZohoOAuthException(ex);
    }
    //Try the grand token if available
    if (StringUtils.isNotBlank(grantToken)) {
      ZohoOAuthClient cli = ZohoOAuthClient.getInstance();
      try {
        cli.generateAccessToken(grantToken);
      } catch (ZohoOAuthException e) {
        LOGGER.warn(
            "THIS EXCEPTION IS PROBABLY NORMAL. THAT CAN HAPPEN IF THE GRANT TOKEN HAS BEEN SUCCESSFULLY USED ONCE ALREADY!"
                + "Exception when generating access token. Grant tokens can be used only once, "
                + "so when the access and refresh tokens are generated, the grant token becomes obsolete on subsequent deployments",
            e);
      }
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
  public Optional<ZCRMRecord> getZcrmRecordContactByEmail(String email) throws BadContentException {
    final BulkAPIResponse bulkAPIResponseContacts;
    try {
      bulkAPIResponseContacts = zcrmModuleContacts.searchByEmail(email);
    } catch (ZCRMException e) {
      throw new BadContentException("Zoho search by email threw an exception", e);
    }
    final List<ZCRMRecord> zcrmRecords = castItemsToType(bulkAPIResponseContacts.getData(),
        ZCRMRecord.class);
    return zcrmRecords.stream().findFirst();
  }

  /**
   * Get a contact by using an organization email.
   *
   * @param organizationName the organization name to search for
   * @return the organization corresponding to the organization name
   * @throws BadContentException if an exception occurred during searching
   */
  public Optional<ZCRMRecord> getZcrmRecordOrganizationByName(String organizationName)
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
    final List<ZCRMRecord> zcrmRecords = castItemsToType(bulkAPIResponseAccounts.getData(),
        ZCRMRecord.class);
    return zcrmRecords.stream().findFirst();
  }

  /**
   * Get a contact by using an organization id.
   *
   * @param organizationId the organization id to search for
   * @return the organization corresponding to the organization id
   * @throws BadContentException if an exception occurred during searching
   */
  public Optional<ZCRMRecord> getZcrmRecordOrganizationById(String organizationId)
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
    final List<ZCRMRecord> zcrmRecords = castItemsToType(bulkAPIResponseAccounts.getData(),
        ZCRMRecord.class);
    return zcrmRecords.stream().findFirst();
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
    return castItemsToType(bulkAPIResponseDeletedRecords.getData(), ZCRMTrashRecord.class);
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
    return getZcrmRecordOrganizations(start, rows, modifiedDate, null, null);
  }

  /**
   * Get organization items paged, filtering by modifiedDate date and searchCriteria.
   *
   * @param page first index starts with 1
   * @param pageSize the number of entries to be returned, Zoho will have an upper limit.
   * @param modifiedDate the date of last modification to check
   * @param searchCriteria the searchCriteria to apply during the Zoho search
   * @param criteriaOperator the criteriaOperator used for each parameter, can be one of {@link
   * ZohoConstants#EQUALS_OPERATION},{@link ZohoConstants#STARTS_WITH_OPERATION}. If not provided or
   * wrong value, it will default to {@link ZohoConstants#EQUALS_OPERATION}.
   * @return the list of Zoho Organizations
   * @throws BadContentException if an error occurred during accessing Zoho
   */
  public List<ZCRMRecord> getZcrmRecordOrganizations(int page, int pageSize, Date modifiedDate,
      Map<String, String> searchCriteria, String criteriaOperator) throws BadContentException {

    if (page < 1 || pageSize < 1) {
      throw new BadContentException(
          "Invalid page or pageSize index. Index must be >= 1",
          new IllegalArgumentException(
              String.format("Provided page: %s, and pageSize: %s", page, pageSize)));
    }
    int start = ((page - 1) * pageSize) + 1;

    String modifiedDateString = null;
    if (Objects.nonNull(modifiedDate)) {
      modifiedDateString = ZohoConstants.ZOHO_DATE_FORMATTER.format(modifiedDate);
    }

    final BulkAPIResponse bulkAPIResponse;
    try {
      if (CollectionUtils.isEmpty(searchCriteria)) {//No searchCriteria available
        bulkAPIResponse = zcrmModuleAccounts
            .getRecords(null, null, null, start, pageSize, modifiedDateString, null, Boolean.FALSE);
      } else {
        bulkAPIResponse = zcrmModuleAccounts
            .searchByCriteria(createZohoCriteriaString(searchCriteria, criteriaOperator), start,
                pageSize);
      }
    } catch (ZCRMException e) {
      throw new BadContentException(
          "Cannot get organization list from: " + start + " rows :" + pageSize, e);
    }

    return castItemsToType(bulkAPIResponse.getData(), ZCRMRecord.class);
  }

  /**
   * Using the search criteria provided and the modifiedDate if available it will create the
   * criteria in the format that Zoho accepts. Result will be depicted as
   * "(field1:equals:valueA)OR(field1:equals:valueB)OR(field2:equals:valueC)" or "".
   *
   * @param searchCriteria the search criteria map provided, values can be comma separated per key
   * @param criteriaOperator the criteriaOperator used for each parameter, can be one of {@link
   * ZohoConstants#EQUALS_OPERATION},{@link ZohoConstants#STARTS_WITH_OPERATION}. If not provided or
   * wrong value, it will default to {@link ZohoConstants#EQUALS_OPERATION}.
   * @return the created criteria in the format Zoho accepts
   */
  private String createZohoCriteriaString(Map<String, String> searchCriteria,
      String criteriaOperator) {
    if (CollectionUtils.isEmpty(searchCriteria)) {
      searchCriteria = new HashMap<>();
    }

    if (Objects.isNull(criteriaOperator) || (!ZohoConstants.EQUALS_OPERATION.equals(criteriaOperator)
        && !ZohoConstants.STARTS_WITH_OPERATION.equals(criteriaOperator))) {
      criteriaOperator = ZohoConstants.EQUALS_OPERATION;
    }

    String finalCriteriaOperator = criteriaOperator;
    return searchCriteria.entrySet().stream().map(entry ->
        Arrays.stream(entry.getValue().split(ZohoConstants.DELIMITER_COMMA))
            .map(value -> String
                .format(ZohoConstants.ZOHO_OPERATION_FORMAT_STRING, entry.getKey(),
                    finalCriteriaOperator, value.trim())).collect(
            Collectors.joining(ZohoConstants.OR))).collect(Collectors.joining(ZohoConstants.OR));
  }

  public static <T extends ZCRMEntity> List<T> castItemsToType(List<? extends ZCRMEntity> zcrmEntities,
      Class<T> classType) {
    return zcrmEntities.stream().filter(classType::isInstance).map(classType::cast)
        .collect(Collectors.toList());
  }
}
