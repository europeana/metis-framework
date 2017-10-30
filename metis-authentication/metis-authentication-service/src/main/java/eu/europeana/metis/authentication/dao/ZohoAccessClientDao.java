package eu.europeana.metis.authentication.dao;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
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


  private String zohoBaseUrl;
  private String zohoAuthenticationToken;

  public ZohoAccessClientDao(String zohoBaseUrl, String zohoAuthenticationToken) {
    this.zohoBaseUrl = zohoBaseUrl;
    this.zohoAuthenticationToken = zohoAuthenticationToken;
  }

  public JsonNode getUserByEmail(String email) throws IOException {
    String contactsSearchUrl = String
        .format("%s/%s/%s/%s", zohoBaseUrl, "json", CONTACTS_MODULE_STRING, SEARCH_RECORDS_STRING);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl)
        .queryParam(AUTHENTICATION_TOKEN_STRING, zohoAuthenticationToken)
        .queryParam(SCOPE_STRING, "crmapi")
        .queryParam(CRITERIA_STRING, String.format("(Email:%s)", email));

    RestTemplate restTemplate = new RestTemplate();
    String contactResponse = restTemplate
        .getForObject(builder.build().encode().toUri(), String.class);
    LOGGER.info(contactResponse);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonContactResponse = mapper.readTree(contactResponse);
    if (jsonContactResponse.get("response").get("result") == null)
      return null;
    return jsonContactResponse.get("response").get("result").get(CONTACTS_MODULE_STRING).get("row").get("FL");
  }

  public JsonNode getOrganizationByOrganizationName(String organizationName) throws IOException {
    String contactsSearchUrl = String
        .format("%s/%s/%s/%s", zohoBaseUrl, "json", ACCOUNTS_MODULE_STRING, SEARCH_RECORDS_STRING);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl)
        .queryParam(AUTHENTICATION_TOKEN_STRING, zohoAuthenticationToken)
        .queryParam(SCOPE_STRING, "crmapi")
        .queryParam(CRITERIA_STRING, String.format("(Account Name:%s)", organizationName));

    RestTemplate restTemplate = new RestTemplate();
    String contactResponse = restTemplate
        .getForObject(builder.build().encode().toUri(), String.class);
    LOGGER.info(contactResponse);
    ObjectMapper mapper = new ObjectMapper();
    JsonNode jsonContactResponse = mapper.readTree(contactResponse);
    if (jsonContactResponse.get("response").get("result") == null)
      return null;
    return jsonContactResponse.get("response").get("result").get(ACCOUNTS_MODULE_STRING).get("row").get("FL");
  }

}
