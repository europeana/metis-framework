package eu.europeana.metis.zoho;

import com.zoho.api.authenticator.OAuthToken;
import com.zoho.api.authenticator.OAuthToken.TokenType;
import com.zoho.api.authenticator.Token;
import com.zoho.api.authenticator.store.TokenStore;
import com.zoho.crm.api.Initializer;
import com.zoho.crm.api.ParameterMap;
import com.zoho.crm.api.SDKConfig;
import com.zoho.crm.api.UserSignature;
import com.zoho.crm.api.dc.DataCenter.Environment;
import com.zoho.crm.api.dc.USDataCenter;
import com.zoho.crm.api.exception.SDKException;
import com.zoho.crm.api.record.Record;
import com.zoho.crm.api.record.RecordOperations;
import com.zoho.crm.api.record.RecordOperations.SearchRecordsParam;
import com.zoho.crm.api.record.ResponseHandler;
import com.zoho.crm.api.record.ResponseWrapper;
import com.zoho.crm.api.util.APIResponse;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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

  public ZohoAccessClient(TokenStore tokenStore, String zohoEmail, String clientId,
      String clientSecret, String grandToken, String redirectUrl) throws SDKException {
    UserSignature userSignature = new UserSignature(zohoEmail);
    Token token = new OAuthToken(clientId, clientSecret, grandToken, TokenType.GRANT, redirectUrl);
    SDKConfig sdkConfig = new SDKConfig.Builder().setAutoRefreshFields(false)
        .setPickListValidation(true).build();
    Environment environment = USDataCenter.PRODUCTION;
    String resourcePath = "/tmp";
    try {
      Initializer
          .initialize(userSignature, environment, token, tokenStore, sdkConfig, resourcePath);
      //Make a call to zoho so that the grant token will generate the first pair of access/refresh tokens
      this.getZohoRecordContactByEmail("");
    } catch (SDKException e) {
      LOGGER.warn("Exception during initialize", e);
    }
  }
  /**
   * Get a contact by using an email.
   *
   * @param email the email to search for
   * @return the contact corresponding to the email
   * @throws SDKException if an exception occurred during searching
   */
  public Optional<Record> getZohoRecordContactByEmail(String email) throws SDKException {
    RecordOperations recordOperations = new RecordOperations();
    ParameterMap paramInstance = new ParameterMap();
    paramInstance.add(SearchRecordsParam.EMAIL, email);
    APIResponse<ResponseHandler> response = recordOperations
        .searchRecords(ZohoConstants.CONTACTS_MODULE_NAME, paramInstance);
    return getZohoRecord(response);
  }

  /**
   * Get a contact by using an organization email.
   *
   * @param organizationName the organization name to search for
   * @return the organization corresponding to the organization name
   * @throws SDKException if an exception occurred during searching
   */
  public Optional<Record> getZohoRecordOrganizationByName(String organizationName)
      throws SDKException {
    RecordOperations recordOperations = new RecordOperations();
    ParameterMap paramInstance = new ParameterMap();
    paramInstance.add(SearchRecordsParam.CRITERIA, String
            .format(ZohoConstants.ZOHO_OPERATION_FORMAT_STRING, ZohoConstants.ACCOUNT_NAME_FIELD,
                ZohoConstants.EQUALS_OPERATION, organizationName));

        APIResponse < ResponseHandler > response = recordOperations
            .searchRecords(ZohoConstants.ACCOUNTS_MODULE_NAME, paramInstance);
    return getZohoRecord(response);
  }

  private Optional<Record> getZohoRecord(APIResponse<ResponseHandler> response) {
    if (response != null) {
      if (response.isExpected()) {
        //Get the object from response
        ResponseHandler responseHandler = response.getObject();
        if (responseHandler instanceof ResponseWrapper) {
          ResponseWrapper responseWrapper = (ResponseWrapper) responseHandler;
          List<Record> records = responseWrapper.getData();
          return records.stream().findFirst();
        }
      }
    }
    return Optional.empty();
  }
  //
  //  /**
  //   * Get a contact by using an organization id.
  //   *
  //   * @param organizationId the organization id to search for
  //   * @return the organization corresponding to the organization id
  //   * @throws ZohoException if an exception occurred during searching
  //   */
  //  public Optional<ZCRMRecord> getZcrmRecordOrganizationById(String organizationId)
  //      throws ZohoException {
  //    final BulkAPIResponse bulkAPIResponseAccounts;
  //    try {
  //      bulkAPIResponseAccounts = zcrmModuleAccounts.searchByCriteria(String
  //          .format(ZohoConstants.ZOHO_OPERATION_FORMAT_STRING, ZohoConstants.ID_FIELD,
  //              ZohoConstants.EQUALS_OPERATION, organizationId));
  //    } catch (ZCRMException e) {
  //      throw new ZohoException("Zoho search organization by organization id threw an exception", e);
  //    }
  //    final List<ZCRMRecord> zcrmRecords = castItemsToType(bulkAPIResponseAccounts.getData(),
  //        ZCRMRecord.class);
  //    return zcrmRecords.stream().findFirst();
  //  }
  //
  //  /**
  //   * Get deleted organization items paged.
  //   *
  //   * @param startPage The number of the item from which the paging should start. First item is at
  //   * number 1. Uses default number of items per page.
  //   * @return the list of deleted Zoho Organizations
  //   * @throws ZohoException if an error occurred during accessing Zoho
  //   */
  //  public List<ZCRMTrashRecord> getZCRMTrashRecordDeletedOrganizations(int startPage)
  //      throws ZohoException {
  //
  //    if (startPage < 1) {
  //      throw new ZohoException("Invalid start page index. Index must be >= 1",
  //          new IllegalArgumentException("start page: " + startPage));
  //    }
  //    final BulkAPIResponse bulkAPIResponseDeletedRecords;
  //    try {
  //      bulkAPIResponseDeletedRecords = zcrmModuleAccounts
  //          .getDeletedRecords(startPage, ITEMS_PER_PAGE);
  //    } catch (ZCRMException e) {
  //      throw new ZohoException("Cannot get deleted organization list from: " + startPage, e);
  //    }
  //    return castItemsToType(bulkAPIResponseDeletedRecords.getData(), ZCRMTrashRecord.class);
  //  }
  //
  //  /**
  //   * Get organization items paged, filtering by modifiedDate date.
  //   *
  //   * @param start first index starts with 1
  //   * @param rows the number of entries to be returned, Zoho will have an upper limit
  //   * @param modifiedDate the date of last modification to check
  //   * @return the list of Zoho Organizations
  //   * @throws ZohoException if an error occurred during accessing Zoho
  //   */
  //  public List<ZCRMRecord> getZcrmRecordOrganizations(int start, int rows, Date modifiedDate)
  //      throws ZohoException {
  //    return getZcrmRecordOrganizations(start, rows, modifiedDate, null, null);
  //  }
  //
  //  /**
  //   * Get organization items paged, filtering by modifiedDate date and searchCriteria.
  //   *
  //   * @param page first index starts with 1
  //   * @param pageSize the number of entries to be returned, Zoho will have an upper limit.
  //   * @param modifiedDate the date of last modification to check
  //   * @param searchCriteria the searchCriteria to apply during the Zoho search
  //   * @param criteriaOperator the criteriaOperator used for each parameter, can be one of {@link
  //   * ZohoConstants#EQUALS_OPERATION},{@link ZohoConstants#STARTS_WITH_OPERATION}. If not provided or
  //   * wrong value, it will default to {@link ZohoConstants#EQUALS_OPERATION}.
  //   * @return the list of Zoho Organizations
  //   * @throws ZohoException if an error occurred during accessing Zoho
  //   */
  //  public List<ZCRMRecord> getZcrmRecordOrganizations(int page, int pageSize, Date modifiedDate,
  //      Map<String, String> searchCriteria, String criteriaOperator) throws ZohoException {
  //
  //    if (page < 1 || pageSize < 1) {
  //      throw new ZohoException("Invalid page or pageSize index. Index must be >= 1",
  //          new IllegalArgumentException(
  //              String.format("Provided page: %s, and pageSize: %s", page, pageSize)));
  //    }
  //    int start = ((page - 1) * pageSize) + 1;
  //
  //    String modifiedDateString = null;
  //    if (Objects.nonNull(modifiedDate)) {
  //      modifiedDateString = ZohoConstants.ZOHO_DATE_FORMATTER.format(modifiedDate);
  //    }
  //
  //    final BulkAPIResponse bulkAPIResponse;
  //    try {
  //      if (isNullOrEmpty(searchCriteria)) {//No searchCriteria available
  //        bulkAPIResponse = zcrmModuleAccounts
  //            .getRecords(null, null, null, start, pageSize, modifiedDateString, null, Boolean.FALSE);
  //      } else {
  //        bulkAPIResponse = zcrmModuleAccounts
  //            .searchByCriteria(createZohoCriteriaString(searchCriteria, criteriaOperator), start,
  //                pageSize);
  //      }
  //    } catch (ZCRMException e) {
  //      throw new ZohoException("Cannot get organization list from: " + start + " rows :" + pageSize,
  //          e);
  //    }
  //
  //    return castItemsToType(bulkAPIResponse.getData(), ZCRMRecord.class);
  //  }

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
    if (isNullOrEmpty(searchCriteria)) {
      searchCriteria = new HashMap<>();
    }

    if (Objects.isNull(criteriaOperator) || (
        !ZohoConstants.EQUALS_OPERATION.equals(criteriaOperator)
            && !ZohoConstants.STARTS_WITH_OPERATION.equals(criteriaOperator))) {
      criteriaOperator = ZohoConstants.EQUALS_OPERATION;
    }

    String finalCriteriaOperator = criteriaOperator;
    return searchCriteria.entrySet().stream().map(
        entry -> Arrays.stream(entry.getValue().split(ZohoConstants.DELIMITER_COMMA)).map(
            value -> String.format(ZohoConstants.ZOHO_OPERATION_FORMAT_STRING, entry.getKey(),
                finalCriteriaOperator, value.trim())).collect(Collectors.joining(ZohoConstants.OR)))
        .collect(Collectors.joining(ZohoConstants.OR));
  }

  //  private static <T extends ZCRMEntity> List<T> castItemsToType(
  //      Collection<? extends ZCRMEntity> zcrmEntities, Class<T> classType) {
  //    return zcrmEntities.stream().filter(classType::isInstance).map(classType::cast)
  //        .collect(Collectors.toList());
  //  }

  /**
   * Check map for nullity or emptiness
   *
   * @param m the map
   * @return true if null or empty, false otherwise
   */
  public static boolean isNullOrEmpty(final Map<?, ?> m) {
    return m == null || m.isEmpty();
  }
}
