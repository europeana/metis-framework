package eu.europeana.metis.authentication.dao;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.common.model.OrganizationRole;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
public class ZohoAccessClientDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZohoAccessClientDao.class);

  private final String zohoBaseUrl;
  private final String zohoAuthenticationToken;

  /**
   * Constructor with required fields that will be used to access the Zoho service.
   *
   * @param zohoBaseUrl the remote url endpoint
   * @param zohoAuthenticationToken the remote authentication token required to access its REST API
   */
  public ZohoAccessClientDao(String zohoBaseUrl, String zohoAuthenticationToken) {
    this.zohoBaseUrl = zohoBaseUrl;
    this.zohoAuthenticationToken = zohoAuthenticationToken;
  }

  /**
   * Retrieves a {@link JsonNode} containing user details from the remote CRM, using an email
   *
   * @param email the email to search for the user
   * @return {@link JsonNode}
   * @throws GenericMetisException which can be one of:
   *         <ul>
   *         <li>{@link BadContentException} if any other problem occurred while constructing the
   *         user, if the response cannot be converted to {@link JsonNode}</li>
   *         </ul>
   */
  public JsonNode getUserByEmail(String email) throws GenericMetisException {
    String contactsSearchUrl = String.format("%s/%s/%s/%s", zohoBaseUrl, ZohoApiFields.JSON_STRING,
        ZohoApiFields.CONTACTS_MODULE_STRING, ZohoApiFields.SEARCH_RECORDS_STRING);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl)
        .queryParam(ZohoApiFields.AUTHENTICATION_TOKEN_STRING, zohoAuthenticationToken)
        .queryParam(ZohoApiFields.SCOPE_STRING, ZohoApiFields.CRMAPI_STRING)
        .queryParam(ZohoApiFields.CRITERIA_STRING,
            String.format("(%s:%s)", ZohoApiFields.EMAIL_FIELD, email));

    RestTemplate restTemplate = new RestTemplate();
    String contactResponse =
        restTemplate.getForObject(builder.build().encode().toUri(), String.class);
    LOGGER.info(contactResponse);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonContactResponse = null;
    try {
      jsonContactResponse = mapper.readTree(contactResponse);
    } catch (IOException e) {
      throw new BadContentException(
          String.format("Cannot retrieve user with email %s, from Zoho", email), e);
    }
    if (jsonContactResponse.get(ZohoApiFields.RESPONSE_STRING)
        .get(ZohoApiFields.RESULT_STRING) == null) {
      return null;
    }
    return jsonContactResponse.get(ZohoApiFields.RESPONSE_STRING).get(ZohoApiFields.RESULT_STRING)
        .get(ZohoApiFields.CONTACTS_MODULE_STRING).get(ZohoApiFields.ROW_STRING)
        .get(ZohoApiFields.FIELDS_LABEL);
  }

  /**
   * Using an organizationName find its corresponding organizationId.
   * <p>
   * It will try to fetch the organization from the external CRM. The external CRM does NOT check
   * for an exact match, so it is possible that instead of a singe organization it will return a
   * list of organization in json format. The exact match will be checked in memory and the correct
   * organizationId will be returned
   * </p>
   *
   * @param organizationName to search for
   * @return the String representation of the organizationId
   * @throws GenericMetisException which can be one of:
   *         <ul>
   *         <li>{@link BadContentException} if any other problem occurred while constructing the
   *         user, like an organization did not have a role defined or the response cannot be
   *         converted to {@link JsonNode}</li>
   *         </ul>
   */
  public String getOrganizationIdByOrganizationName(String organizationName)
      throws GenericMetisException {
    String contactsSearchUrl = String.format("%s/%s/%s/%s", zohoBaseUrl, ZohoApiFields.JSON_STRING,
        ZohoApiFields.ACCOUNTS_MODULE_STRING, ZohoApiFields.SEARCH_RECORDS_STRING);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl)
        .queryParam(ZohoApiFields.AUTHENTICATION_TOKEN_STRING, zohoAuthenticationToken)
        .queryParam(ZohoApiFields.SCOPE_STRING, ZohoApiFields.CRMAPI_STRING)
        .queryParam(ZohoApiFields.CRITERIA_STRING,
            String.format("(%s:%s)", ZohoApiFields.ORGANIZATION_NAME_FIELD, organizationName));

    RestTemplate restTemplate = new RestTemplate();
    String contactResponse =
        restTemplate.getForObject(builder.build().encode().toUri(), String.class);
    LOGGER.info(contactResponse);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonContactResponse;
    try {
      jsonContactResponse = mapper.readTree(contactResponse);
    } catch (IOException e) {
      throw new BadContentException(
          String.format("Cannot retrieve organization with orgnaization name %s, from Zoho",
              organizationName),
          e);
    }
    return checkOrganizationRoleAndGetOrganizationIdFromJsonNode(
        findExactMatchOfOrganization(jsonContactResponse, organizationName));
  }

  private JsonNode findExactMatchOfOrganization(JsonNode jsonOrgizationsResponse,
      String organizationName) {
    if (jsonOrgizationsResponse.get(ZohoApiFields.RESPONSE_STRING)
        .get(ZohoApiFields.RESULT_STRING) == null) {
      return null;
    }
    if (jsonOrgizationsResponse.get(ZohoApiFields.RESPONSE_STRING).get(ZohoApiFields.RESULT_STRING)
        .get(ZohoApiFields.ACCOUNTS_MODULE_STRING).get(ZohoApiFields.ROW_STRING).isArray()) {
      return findOrganizationFromListOfJsonNodes(jsonOrgizationsResponse, organizationName);
    }
    return jsonOrgizationsResponse.get(ZohoApiFields.RESPONSE_STRING)
        .get(ZohoApiFields.RESULT_STRING).get(ZohoApiFields.ACCOUNTS_MODULE_STRING)
        .get(ZohoApiFields.ROW_STRING).get(ZohoApiFields.FIELDS_LABEL);
  }

  private JsonNode findOrganizationFromListOfJsonNodes(JsonNode jsonOrgizationsResponse,
      String organizationName) {
    Iterator<JsonNode> organizationJsonNodes =
        jsonOrgizationsResponse.get(ZohoApiFields.RESPONSE_STRING).get(ZohoApiFields.RESULT_STRING)
            .get(ZohoApiFields.ACCOUNTS_MODULE_STRING).get(ZohoApiFields.ROW_STRING).elements();
    if (organizationJsonNodes == null || !organizationJsonNodes.hasNext()) {
      return null;
    }
    while (organizationJsonNodes.hasNext()) {
      JsonNode nextOrganizationJsonNode =
          organizationJsonNodes.next().get(ZohoApiFields.FIELDS_LABEL);
      Iterator<JsonNode> organizationFields = nextOrganizationJsonNode.elements();
      while (organizationFields.hasNext()) {
        JsonNode organizationField = organizationFields.next();
        JsonNode val = organizationField.get(ZohoApiFields.VALUE_LABEL);
        JsonNode content = organizationField.get(ZohoApiFields.CONTENT_LABEL);
        if (StringUtils.equals(val.textValue(), ZohoApiFields.ORGANIZATION_NAME_FIELD)
            && StringUtils.equals(content.textValue(), organizationName)) {
          return nextOrganizationJsonNode;
        }
      }
    }
    return null;
  }

  private String checkOrganizationRoleAndGetOrganizationIdFromJsonNode(JsonNode jsonNode)
      throws BadContentException {
    String organizationId = null;
    if (jsonNode != null) {
      Iterator<JsonNode> elements = jsonNode.elements();
      OrganizationRole organizationRole = null;
      while (elements.hasNext()) {
        JsonNode next = elements.next();
        JsonNode val = next.get(ZohoApiFields.VALUE_LABEL);
        JsonNode content = next.get(ZohoApiFields.CONTENT_LABEL);
        switch (val.textValue()) {
          case "ACCOUNTID":
            organizationId = content.textValue();
            break;
          case "Organisation Role":
            organizationRole = OrganizationRole.getRoleFromName(content.textValue());
            break;
          default:
            break;
        }
      }
      if (organizationRole == null) {
        throw new BadContentException("Organization Role from Zoho is empty");
      }
    }
    return organizationId;
  }

  /**
   * This method adds filters to the Zoho query if values exist in properties. e.g.
   * &criteria=((Organisation Role:Data Provider)OR (Organisation Role:Provider)OR(Organisation
   * Role:Aggregator))
   * 
   * @param builder
   * @param searchCriteria
   */
  private void applyFilters(UriComponentsBuilder builder, Map<String, String> searchCriteria) {
    if (searchCriteria == null || searchCriteria.isEmpty())
      return;

    String[] filterCriteria;
    StringBuilder filterBuilder;

    for (Map.Entry<String, String> entry : searchCriteria.entrySet()) {
      filterBuilder = new StringBuilder();
      filterCriteria = entry.getValue().split(ZohoApiFields.DELIMITER_COMMA);

      for (String filter : filterCriteria) {
        filterBuilder.append(String.format("(%s:%s)", entry.getKey(), filter.trim()));
        filterBuilder.append(ZohoApiFields.OR);
      }

      // remove last OR
      filterBuilder.delete(filterBuilder.length() - ZohoApiFields.OR.length(), filterBuilder.length());
      builder.queryParam(ZohoApiFields.CRITERIA_STRING,
          String.format("(%s)", filterBuilder.toString()));

    }

  }

  /**
   * This method builds organization URI for passed ID.
   * @param organizationId
   * @return organization URI
   */
  public URI buildOrganizationUriById(String organizationId) {

    URI uri = null;
    String contactsSearchUrl = String.format("%s/%s/%s/%s", zohoBaseUrl, ZohoApiFields.JSON_STRING,
        ZohoApiFields.ACCOUNTS_MODULE_STRING, ZohoApiFields.GET_RECORDS_STRING);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl)
        .queryParam(ZohoApiFields.AUTHENTICATION_TOKEN_STRING, zohoAuthenticationToken)
        .queryParam(ZohoApiFields.SCOPE_STRING, ZohoApiFields.CRMAPI_STRING)
        .queryParam(ZohoApiFields.ID, organizationId);

    uri = builder.build().encode().toUri();
    LOGGER.trace("{}", uri);
    return uri;
  }
  
  /**
   * Retrieve Zoho organization by ID.
   * <p>
   * It will try to fetch the organization from the external Zoho CRM. This method returns an
   * organization in JSON format.
   * </p>
   * 
   * Example query:
   * https://crm.zoho.com/crm/private/json/Accounts/getRecords?authtoken=<token>&scope=crmapi&id=123
   *
   * @param organizationId The Zoho ID of organization
   * @return the Zoho organizations in JsonNode format
   * @throws GenericMetisException which can be one of:
   *         <ul>
   *         <li>{@link BadContentException} if any other problem occurred while constructing the
   *         user, like an organization did not have a role defined or the response cannot be
   *         converted to {@link JsonNode}</li>
   *         </ul>
   * @throws IOException
   */
  public JsonNode getOrganizationById(String organizationId) throws GenericMetisException {

    RestTemplate restTemplate = new RestTemplate();
    URI uri = buildOrganizationUriById(organizationId);

    String organisationsResponse = restTemplate.getForObject(uri, String.class);
    LOGGER.debug(organisationsResponse);

    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readTree(organisationsResponse);
    } catch (IOException e) {
      throw new GenericMetisException("Cannot parse zoho response: " + organisationsResponse, e);
    }
  }

  /**
   * Retrieve Zoho organization from file by given path.
   * <p>
   * It will try to fetch the organization from the given file. This method returns an
   * organization in JSON format.
   * </p>
   *
   * @param contentFile The zoho organization file
   * @return the Zoho organizations in JsonNode format
   * @throws GenericMetisException which can be one of:
   *         <ul>
   *         <li>{@link BadContentException} if any other problem occurred while constructing the
   *         user, like an organization did not have a role defined or the response cannot be
   *         converted to {@link JsonNode}</li>
   *         </ul>
   * @throws IOException
   */
  public JsonNode getOrganizationFromFile(File contentFile) throws GenericMetisException, IOException {

    String organisationsResponse = FileUtils.readFileToString(contentFile, "UTF-8");

    LOGGER.debug("Content of Zoho response file: {}", organisationsResponse);
    
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readTree(organisationsResponse);
    } catch (IOException e) {
      throw new GenericMetisException("Cannot parse zoho response: " + organisationsResponse, e);
    }
  }
    
  /**
   * Retrieve organizations using getRecords query, start and end index. 
   * The organizations are pre-ordered by modified time ascending
   * <p>
   * It will try to fetch the organizations from the external CRM. This method returns a list of
   * organizations in json format.
   * </p>
   * 
   * Example query:
   * https://crm.zoho.com/crm/private/json/Accounts/getRecords?authtoken=<token>&scope=crmapi&fromIndex=1
   * &toIndex=10
   *
   * @param start to start search from this index
   * @param rows to end search by this index
   * @param lastModifiedTime If specified, only records created or modified after the given time
   *        will be fetched. The value must be provided in yyyy-MM-dd HH:mm:ss format
   * @return the list of the organizations
   * @throws GenericMetisException which can be one of:
   *         <ul>
   *         <li>{@link BadContentException} if any other problem occurred while constructing the
   *         user, like an organization did not have a role defined or the response cannot be
   *         converted to {@link JsonNode}</li>
   *         </ul>
   * @throws IOException
   */
  public JsonNode getOrganizations(int start, int rows, String lastModifiedTime)
      throws GenericMetisException {

    String contactsSearchUrl = String.format("%s/%s/%s/%s", zohoBaseUrl, ZohoApiFields.JSON_STRING,
        ZohoApiFields.ACCOUNTS_MODULE_STRING, ZohoApiFields.GET_RECORDS_STRING);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl)
        .queryParam(ZohoApiFields.AUTHENTICATION_TOKEN_STRING, zohoAuthenticationToken)
        .queryParam(ZohoApiFields.SCOPE_STRING, ZohoApiFields.CRMAPI_STRING)
        .queryParam(ZohoApiFields.FROM_INDEX_STRING, start)
        .queryParam(ZohoApiFields.TO_INDEX_STRING, start + rows - 1)
        .queryParam(ZohoApiFields.SORT_COLUMN, ZohoApiFields.MODIFIED_TIME)
        .queryParam(ZohoApiFields.SORT_ORDER, ZohoApiFields.SORT_ORDER_ASC);

    if (!StringUtils.isBlank(lastModifiedTime))
      builder.queryParam(ZohoApiFields.LAST_MODIFIED_TIME, lastModifiedTime);

    RestTemplate restTemplate = new RestTemplate();
    URI uri = builder.build().encode().toUri();
    LOGGER.trace("{}", uri);

    String organisationsResponse = restTemplate.getForObject(uri, String.class);
    LOGGER.debug(organisationsResponse);
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readTree(organisationsResponse);
    } catch (IOException e) {
      throw new GenericMetisException("Cannot parse zoho response: " + organisationsResponse, e);
    }
  }

  /**
   * Retrieve organizations using searchRecords query, search criteria, start and end index. The
   * organizations are pre-ordered by modified time ascending
   * <p>
   * It will try to fetch the organizations from the external CRM. This method returns a list of
   * organizations in json format.
   * </p>
   * 
   * Example query:
   * https://crm.zoho.com/crm/private/json/Accounts/searchRecords?authtoken=<token>&scope=crmapi&fromIndex=1
   * &toIndex=5&sortColumnString=Modified%20Time&sortOrderString=asc
   * &criteria=(Organisation%20Role:Data%20Provider)
   *
   * @param start to start search from this index
   * @param rows to end search by this index
   * @param lastModifiedTime If specified, only records created or modified after the given time
   *        will be fetched. The value must be provided in yyyy-MM-dd HH:mm:ss format
   * @param searchCriteria The map with filter settings
   * @return the list of the organizations
   * @throws GenericMetisException which can be one of:
   *         <ul>
   *         <li>{@link BadContentException} if any other problem occurred while constructing the
   *         user, like an organization did not have a role defined or the response cannot be
   *         converted to {@link JsonNode}</li>
   *         </ul>
   * @throws IOException
   */
  public JsonNode searchOrganizations(int start, int rows, String lastModifiedTime,
      Map<String, String> searchCriteria) throws GenericMetisException {

    String contactsSearchUrl = String.format("%s/%s/%s/%s", zohoBaseUrl, ZohoApiFields.JSON_STRING,
        ZohoApiFields.ACCOUNTS_MODULE_STRING, ZohoApiFields.SEARCH_RECORDS_STRING);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl)
        .queryParam(ZohoApiFields.AUTHENTICATION_TOKEN_STRING, zohoAuthenticationToken)
        .queryParam(ZohoApiFields.SCOPE_STRING, ZohoApiFields.CRMAPI_STRING)
        .queryParam(ZohoApiFields.FROM_INDEX_STRING, start)
        .queryParam(ZohoApiFields.TO_INDEX_STRING, start + rows - 1)
        .queryParam(ZohoApiFields.SORT_COLUMN, ZohoApiFields.MODIFIED_TIME)
        .queryParam(ZohoApiFields.SORT_ORDER, ZohoApiFields.SORT_ORDER_ASC);

    if (!StringUtils.isBlank(lastModifiedTime))
      builder.queryParam(ZohoApiFields.LAST_MODIFIED_TIME, lastModifiedTime);

    applyFilters(builder, searchCriteria);

    RestTemplate restTemplate = new RestTemplate();
    URI uri = builder.build().encode().toUri();
    LOGGER.trace("{}", uri);

    String organisationsResponse = restTemplate.getForObject(uri, String.class);
    LOGGER.debug(organisationsResponse);
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readTree(organisationsResponse);
    } catch (IOException e) {
      throw new GenericMetisException("Cannot parse zoho response: " + organisationsResponse, e);
    }
  }

}
