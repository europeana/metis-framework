package eu.europeana.metis.authentication.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.common.model.OrganizationRole;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;
import java.io.IOException;
import java.util.Iterator;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
public class ZohoAccessClientDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZohoAccessClientDao.class);

  private static final String CONTACTS_MODULE_STRING = "Contacts";
  private static final String ACCOUNTS_MODULE_STRING = "Accounts";
  private static final String SEARCH_RECORDS_STRING = "searchRecords";
  private static final String AUTHENTICATION_TOKEN_STRING = "authtoken";
  private static final String SCOPE_STRING = "scope";
  private static final String CRITERIA_STRING = "criteria";
  private static final String RESPONSE_STRING = "response";
  private static final String RESULT_STRING = "result";
  private static final String ROW_STRING = "row";
  private static final String JSON_STRING = "json";
  private static final String CRMAPI_STRING = "crmapi";
  private static final String ORGANIZATION_NAME_FIELD = "Account Name";
  private static final String EMAIL_FIELD = "Email";
  private static final String VALUE_LABEL = "val";
  private static final String CONTENT_LABEL = "content";
  private static final String FIELDS_LABEL = "FL";

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
   * <ul>
   * <li> {@link BadContentException} if any other problem occurred while constructing the user, if the
   * response cannot be converted to {@link JsonNode} </li>
   * </ul>
   */
  public JsonNode getUserByEmail(String email) throws GenericMetisException {
    String contactsSearchUrl = String
        .format("%s/%s/%s/%s", zohoBaseUrl, JSON_STRING, CONTACTS_MODULE_STRING,
            SEARCH_RECORDS_STRING);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl)
        .queryParam(AUTHENTICATION_TOKEN_STRING, zohoAuthenticationToken)
        .queryParam(SCOPE_STRING, CRMAPI_STRING)
        .queryParam(CRITERIA_STRING, String.format("(%s:%s)", EMAIL_FIELD, email));

    RestTemplate restTemplate = new RestTemplate();
    String contactResponse = restTemplate
        .getForObject(builder.build().encode().toUri(), String.class);
    LOGGER.info(contactResponse);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonContactResponse = null;
    try {
      jsonContactResponse = mapper.readTree(contactResponse);
    } catch (IOException e) {
      throw new BadContentException(
          String.format("Cannot retrieve user with email %s, from Zoho", email), e);
    }
    if (jsonContactResponse.get(RESPONSE_STRING).get(RESULT_STRING) == null) {
      return null;
    }
    return jsonContactResponse.get(RESPONSE_STRING).get(RESULT_STRING).get(CONTACTS_MODULE_STRING)
        .get(ROW_STRING).get(FIELDS_LABEL);
  }


  /**
   * Using an organizationName find its corresponding organizationId. <p>It will try to fetch the
   * organization from the external CRM. The external CRM does NOT check for an exact match, so it
   * is possible that instead of a singe organization it will return a list of organization in json
   * format. The exact match will be checked in memory and the correct organizationId will be
   * returned</p>
   *
   * @param organizationName to search for
   * @return the String representation of the organizationId
   * @throws GenericMetisException which can be one of:
   * <ul>
   * <li>{@link BadContentException} if any other problem occurred while constructing the user, like an
   * organization did not have a role defined or the response cannot be converted to {@link JsonNode}</li>
   * </ul>
   */
  public String getOrganizationIdByOrganizationName(String organizationName)
      throws GenericMetisException {
    String contactsSearchUrl = String
        .format("%s/%s/%s/%s", zohoBaseUrl, JSON_STRING, ACCOUNTS_MODULE_STRING,
            SEARCH_RECORDS_STRING);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl)
        .queryParam(AUTHENTICATION_TOKEN_STRING, zohoAuthenticationToken)
        .queryParam(SCOPE_STRING, CRMAPI_STRING)
        .queryParam(CRITERIA_STRING,
            String.format("(%s:%s)", ORGANIZATION_NAME_FIELD, organizationName));

    RestTemplate restTemplate = new RestTemplate();
    String contactResponse = restTemplate
        .getForObject(builder.build().encode().toUri(), String.class);
    LOGGER.info(contactResponse);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonContactResponse;
    try {
      jsonContactResponse = mapper.readTree(contactResponse);
    } catch (IOException e) {
      throw new BadContentException(
          String.format("Cannot retrieve organization with orgnaization name %s, from Zoho",
              organizationName), e);
    }
    return checkOrganizationRoleAndGetOrganizationIdFromJsonNode(
        findExactMatchOfOrganization(jsonContactResponse,
            organizationName));
  }

  private JsonNode findExactMatchOfOrganization(JsonNode jsonOrgizationsResponse,
      String organizationName) {
    if (jsonOrgizationsResponse.get(RESPONSE_STRING).get(RESULT_STRING) == null) {
      return null;
    }
    if (jsonOrgizationsResponse.get(RESPONSE_STRING).get(RESULT_STRING)
        .get(ACCOUNTS_MODULE_STRING).get(ROW_STRING).isArray()) {
      return findOrganizationFromListOfJsonNodes(jsonOrgizationsResponse, organizationName);
    }
    return jsonOrgizationsResponse.get(RESPONSE_STRING).get(RESULT_STRING)
        .get(ACCOUNTS_MODULE_STRING).get(ROW_STRING).get(FIELDS_LABEL);
  }

  private JsonNode findOrganizationFromListOfJsonNodes(JsonNode jsonOrgizationsResponse,
      String organizationName) {
    Iterator<JsonNode> organizationJsonNodes = jsonOrgizationsResponse.get(RESPONSE_STRING)
        .get(RESULT_STRING).get(ACCOUNTS_MODULE_STRING).get(ROW_STRING).elements();
    if (organizationJsonNodes == null || !organizationJsonNodes.hasNext()) {
      return null;
    }
    while (organizationJsonNodes.hasNext()) {
      JsonNode nextOrganizationJsonNode = organizationJsonNodes.next().get(FIELDS_LABEL);
      Iterator<JsonNode> organizationFields = nextOrganizationJsonNode.elements();
      while (organizationFields.hasNext()) {
        JsonNode organizationField = organizationFields.next();
        JsonNode val = organizationField.get(VALUE_LABEL);
        JsonNode content = organizationField.get(CONTENT_LABEL);
        if (StringUtils.equals(val.textValue(), ORGANIZATION_NAME_FIELD) && StringUtils
            .equals(content.textValue(), organizationName)) {
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
        JsonNode val = next.get(VALUE_LABEL);
        JsonNode content = next.get(CONTENT_LABEL);
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

}
