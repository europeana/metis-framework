package eu.europeana.enrichment.service.dao;

import java.io.IOException;
import java.net.URI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.europeana.metis.authentication.dao.ZohoApi2Fields;
import eu.europeana.metis.authentication.dao.ZohoApiFields;
import eu.europeana.metis.exception.BadContentException;
import eu.europeana.metis.exception.GenericMetisException;

/**
 * @author GrafR
 * @since 2018-04-20
 */
public class ZohoV2AccessDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZohoV2AccessDao.class);

  private final String zohoBaseUrl;
  private final String zohoAuthenticationToken;

  /**
   * Constructor with required fields that will be used to access the Zoho API version 2 service.
   *
   * @param zohoBaseUrl the remote url endpoint
   * @param zohoAuthenticationToken the remote authentication token required to access its REST API
   */
  public ZohoV2AccessDao(String zohoBaseUrl, String zohoAuthenticationToken) {
    this.zohoBaseUrl = zohoBaseUrl;
    this.zohoAuthenticationToken = zohoAuthenticationToken;
  }
  
  /**
   * Retrieve deleted organizations using getRecords query, start and end index. 
   * The organizations are pre-ordered by modified time ascending
   * <p>
   * It will try to fetch the organizations from the external CRM. This method returns a list of
   * organizations in json format.
   * </p>
   * 
   * Example query:
   * https://www.zohoapis.com/crm/v2/Accounts/deleted?page=1
   *
   * @param startPage The page number to start search from this index
   * @return the list of the deleted organizations
   * @throws GenericMetisException which can be one of:
   *         <ul>
   *         <li>{@link BadContentException} if any other problem occurred while constructing the
   *         user, like an organization did not have a role defined or the response cannot be
   *         converted to {@link JsonNode}</li>
   *         </ul>
   * @throws IOException
   */
  public JsonNode getDeletedOrganizations(int startPage) throws GenericMetisException {

    String contactsSearchUrl = String.format("%s/%s/%s", zohoBaseUrl, 
        ZohoApiFields.ACCOUNTS_MODULE_STRING, ZohoApi2Fields.DELETED_STRING);
    UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(contactsSearchUrl)
        .queryParam(ZohoApi2Fields.PAGE_STRING, startPage);

    HttpHeaders headers = new HttpHeaders();
    headers.add(HttpHeaders.AUTHORIZATION, zohoAuthenticationToken);
    HttpEntity<?> entity = new HttpEntity<>(headers);
    
    RestTemplate restTemplate = new RestTemplate();
    URI uri = builder.build().encode().toUri();
    LOGGER.trace("{}", uri);

    HttpEntity<String> response = restTemplate.exchange(
        uri, 
        HttpMethod.GET, 
        entity, 
        String.class);

    String organisationsResponse = response.getBody();
    LOGGER.debug(organisationsResponse);
    ObjectMapper mapper = new ObjectMapper();
    try {
      return mapper.readTree(organisationsResponse);
    } catch (IOException e) {
      throw new GenericMetisException("Cannot parse zoho response: " + organisationsResponse, e);
    }
  }
  
}
