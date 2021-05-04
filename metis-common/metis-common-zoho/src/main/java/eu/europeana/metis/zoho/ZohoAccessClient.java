package eu.europeana.metis.zoho;

import com.zoho.api.authenticator.OAuthToken;
import com.zoho.api.authenticator.OAuthToken.TokenType;
import com.zoho.api.authenticator.Token;
import com.zoho.api.authenticator.store.TokenStore;
import com.zoho.crm.api.HeaderMap;
import com.zoho.crm.api.Initializer;
import com.zoho.crm.api.ParameterMap;
import com.zoho.crm.api.SDKConfig;
import com.zoho.crm.api.UserSignature;
import com.zoho.crm.api.dc.DataCenter.Environment;
import com.zoho.crm.api.dc.USDataCenter;
import com.zoho.crm.api.exception.SDKException;
import com.zoho.crm.api.record.DeletedRecord;
import com.zoho.crm.api.record.DeletedRecordsHandler;
import com.zoho.crm.api.record.DeletedRecordsWrapper;
import com.zoho.crm.api.record.Record;
import com.zoho.crm.api.record.RecordOperations;
import com.zoho.crm.api.record.RecordOperations.GetDeletedRecordsParam;
import com.zoho.crm.api.record.RecordOperations.GetRecordsHeader;
import com.zoho.crm.api.record.RecordOperations.GetRecordsParam;
import com.zoho.crm.api.record.RecordOperations.SearchRecordsParam;
import com.zoho.crm.api.record.ResponseHandler;
import com.zoho.crm.api.record.ResponseWrapper;
import com.zoho.crm.api.util.APIResponse;
import java.time.OffsetDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class that contains methods related to communication with the Zoho service.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-12-06
 */
public final class ZohoAccessClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZohoAccessClient.class);
  private static final int ITEMS_PER_PAGE = 200;

  /**
   * Constructor with all parameters.
   *
   * @param tokenStore the tocken store to be used
   * @param zohoEmail the zoho email
   * @param clientId the zoho client id
   * @param clientSecret the zoho client secret
   * @param grandToken the zoho initial grant token
   * @param redirectUrl the registered zoho redirect url
   * @throws ZohoException if something when wrong during zoho initialization
   */
  public ZohoAccessClient(TokenStore tokenStore, String zohoEmail, String clientId,
      String clientSecret, String grandToken, String redirectUrl) throws ZohoException {
    try {
      UserSignature userSignature = new UserSignature(zohoEmail);
      Token token = new OAuthToken(clientId, clientSecret, grandToken, TokenType.GRANT,
          redirectUrl);
      SDKConfig sdkConfig = new SDKConfig.Builder().setAutoRefreshFields(false)
          .setPickListValidation(true).build();
      Environment environment = USDataCenter.PRODUCTION;
      String resourcePath = SystemUtils.getUserHome().getAbsolutePath();
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
   * @throws ZohoException if an exception occurred during searching
   */
  public Optional<Record> getZohoRecordContactByEmail(String email) throws ZohoException {
    try {
      RecordOperations recordOperations = new RecordOperations();
      ParameterMap paramInstance = new ParameterMap();
      paramInstance.add(SearchRecordsParam.EMAIL, email);
      APIResponse<ResponseHandler> response = recordOperations
          .searchRecords(ZohoConstants.CONTACTS_MODULE_NAME, paramInstance);
      return getZohoRecord(response);
    } catch (SDKException e) {
      throw new ZohoException("Zoho search by email threw an exception", e);
    }
  }

  /**
   * Get a contact by using an organization email.
   *
   * @param organizationName the organization name to search for
   * @return the organization corresponding to the organization name
   * @throws ZohoException if an exception occurred during searching
   */
  public Optional<Record> getZohoRecordOrganizationByName(String organizationName)
      throws ZohoException {
    try {
      RecordOperations recordOperations = new RecordOperations();
      ParameterMap paramInstance = new ParameterMap();
      paramInstance.add(SearchRecordsParam.CRITERIA, String
          .format(ZohoConstants.ZOHO_OPERATION_FORMAT_STRING, ZohoConstants.ACCOUNT_NAME_FIELD,
              ZohoConstants.EQUALS_OPERATION, organizationName));

      APIResponse<ResponseHandler> response = recordOperations
          .searchRecords(ZohoConstants.ACCOUNTS_MODULE_NAME, paramInstance);
      return getZohoRecord(response);
    } catch (SDKException e) {
      throw new ZohoException("Zoho search organization by organization name threw an exception",
          e);
    }
  }

  private Optional<Record> getZohoRecord(APIResponse<ResponseHandler> response) {
    return getZohoRecords(response).stream().findFirst();
  }

  private List<Record> getZohoRecords(APIResponse<ResponseHandler> response) {
    if (response != null && response.isExpected()) {
      //Get the object from response
      ResponseHandler responseHandler = response.getObject();
      if (responseHandler instanceof ResponseWrapper) {
        ResponseWrapper responseWrapper = (ResponseWrapper) responseHandler;
        return responseWrapper.getData();
      }
    }
    return Collections.emptyList();
  }

  private List<DeletedRecord> getZohoDeletedRecords(APIResponse<DeletedRecordsHandler> response) {
    if (response != null && response.isExpected()) {
      //Get the object from response
      DeletedRecordsHandler deletedRecordsHandler = response.getObject();
      if (deletedRecordsHandler instanceof DeletedRecordsWrapper) {
        DeletedRecordsWrapper deletedRecordsWrapper = (DeletedRecordsWrapper) deletedRecordsHandler;
        return deletedRecordsWrapper.getData();
      }
    }
    return Collections.emptyList();
  }

  /**
   * Get a contact by using an organization id.
   *
   * @param organizationId the organization id to search for
   * @return the organization corresponding to the organization id
   * @throws ZohoException if an exception occurred during searching
   */
  public Optional<Record> getZohoRecordOrganizationById(String organizationId)
      throws ZohoException {
    try {
      RecordOperations recordOperations = new RecordOperations();
      ParameterMap paramInstance = new ParameterMap();
      paramInstance.add(SearchRecordsParam.CRITERIA, String
          .format(ZohoConstants.ZOHO_OPERATION_FORMAT_STRING, ZohoConstants.ID_FIELD,
              ZohoConstants.EQUALS_OPERATION, organizationId));

      APIResponse<ResponseHandler> response = recordOperations
          .searchRecords(ZohoConstants.ACCOUNTS_MODULE_NAME, paramInstance);
      return getZohoRecord(response);
    } catch (SDKException e) {
      throw new ZohoException("Zoho search organization by organization id threw an exception", e);
    }
  }

  /**
   * Get deleted organization items paged.
   *
   * @param startPage The number of the item from which the paging should start. First item is at
   * number 1. Uses default number of items per page.
   * @return the list of deleted Zoho Organizations
   * @throws ZohoException if an error occurred during accessing Zoho
   */
  public List<DeletedRecord> getZohoDeletedRecordOrganizations(int startPage) throws ZohoException {
    if (startPage < 1) {
      throw new ZohoException("Invalid start page index. Index must be >= 1",
          new IllegalArgumentException("start page: " + startPage));
    }
    try {
      RecordOperations recordOperations = new RecordOperations();
      ParameterMap paramInstance = new ParameterMap();
      paramInstance.add(GetDeletedRecordsParam.TYPE, "permanent");//all, recycle, permanent
      paramInstance.add(GetDeletedRecordsParam.PAGE, 1);
      paramInstance.add(GetDeletedRecordsParam.PER_PAGE, ITEMS_PER_PAGE);
      APIResponse<DeletedRecordsHandler> response = recordOperations
          .getDeletedRecords(ZohoConstants.ACCOUNTS_MODULE_NAME, paramInstance, new HeaderMap());
      return getZohoDeletedRecords(response);
    } catch (SDKException e) {
      throw new ZohoException("Cannot get deleted organization list from: " + startPage, e);
    }
  }

  /**
   * Get organization items paged, filtering by modifiedDate date.
   *
   * @param start first index starts with 1
   * @param rows the number of entries to be returned, Zoho will have an upper limit
   * @param modifiedDate the date of last modification to check
   * @return the list of Zoho Organizations
   * @throws ZohoException if an error occurred during accessing Zoho
   */
  public List<Record> getZcrmRecordOrganizations(int start, int rows, OffsetDateTime modifiedDate)
      throws ZohoException {
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
   * @throws ZohoException if an error occurred during accessing Zoho
   */
  public List<Record> getZcrmRecordOrganizations(int page, int pageSize,
      OffsetDateTime modifiedDate, Map<String, String> searchCriteria, String criteriaOperator)
      throws ZohoException {

    if (page < 1 || pageSize < 1) {
      throw new ZohoException("Invalid page or pageSize index. Index must be >= 1",
          new IllegalArgumentException(
              String.format("Provided page: %s, and pageSize: %s", page, pageSize)));
    }
    int start = ((page - 1) * pageSize) + 1;

    try {
      APIResponse<ResponseHandler> response;
      RecordOperations recordOperations = new RecordOperations();
      ParameterMap paramInstance = new ParameterMap();
      if (isNullOrEmpty(searchCriteria)) {//No searchCriteria available
        paramInstance.add(GetRecordsParam.PAGE, start);
        paramInstance.add(GetRecordsParam.PER_PAGE, pageSize);
        HeaderMap headerInstance = new HeaderMap();
        headerInstance.add(GetRecordsHeader.IF_MODIFIED_SINCE, modifiedDate);
        response = recordOperations
            .getRecords(ZohoConstants.ACCOUNTS_MODULE_NAME, paramInstance, headerInstance);

      } else {
        paramInstance.add(SearchRecordsParam.PAGE, start);
        paramInstance.add(SearchRecordsParam.PER_PAGE, pageSize);
        paramInstance.add(SearchRecordsParam.CRITERIA,
            createZohoCriteriaString(searchCriteria, criteriaOperator));
        response = recordOperations
            .searchRecords(ZohoConstants.ACCOUNTS_MODULE_NAME, paramInstance);
      }
      return getZohoRecords(response);
    } catch (SDKException e) {
      throw new ZohoException("Cannot get organization list from: " + start + " rows :" + pageSize,
          e);
    }
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
