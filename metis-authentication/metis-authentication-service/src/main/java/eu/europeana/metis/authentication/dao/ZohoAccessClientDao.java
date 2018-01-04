package eu.europeana.metis.authentication.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.util.Iterator;
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

  private String zohoBaseUrl;
  private String zohoAuthenticationToken;

  public ZohoAccessClientDao(String zohoBaseUrl, String zohoAuthenticationToken) {
    this.zohoBaseUrl = zohoBaseUrl;
    this.zohoAuthenticationToken = zohoAuthenticationToken;
  }

  public JsonNode getUserByEmail(String email) throws IOException {
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
    JsonNode jsonContactResponse = mapper.readTree(contactResponse);
    if (jsonContactResponse.get(RESPONSE_STRING).get(RESULT_STRING) == null) {
      return null;
    }
    return jsonContactResponse.get(RESPONSE_STRING).get(RESULT_STRING).get(CONTACTS_MODULE_STRING)
        .get(ROW_STRING).get(FIELDS_LABEL);
  }

  public JsonNode getOrganizationByOrganizationName(String organizationName) throws IOException {
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
    JsonNode jsonContactResponse = mapper.readTree(contactResponse);
    return findExactMatchOfOrganization(jsonContactResponse, organizationName);
  }

  private JsonNode findExactMatchOfOrganization(JsonNode jsonOrgizationsResponse,
      String organizationName) {
    if (jsonOrgizationsResponse.get(RESPONSE_STRING).get(RESULT_STRING) == null) {
      return null;
    }
    JsonNode organizationJsonNode = jsonOrgizationsResponse.get(RESPONSE_STRING).get(RESULT_STRING)
        .get(ACCOUNTS_MODULE_STRING).get(ROW_STRING).get(FIELDS_LABEL);
    if (organizationJsonNode == null) {
      return findOrganizationFromListOfJsonNodes(jsonOrgizationsResponse, organizationName);
    }
    return organizationJsonNode;
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
        if (val.textValue().equals(ORGANIZATION_NAME_FIELD) && content.textValue()
            .equals(organizationName)) {
          return nextOrganizationJsonNode;
        }
      }
    }
    return null;
  }

}
