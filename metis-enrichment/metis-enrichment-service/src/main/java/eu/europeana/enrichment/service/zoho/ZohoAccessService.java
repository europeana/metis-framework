package eu.europeana.enrichment.service.zoho;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.JsonNode;
import eu.europeana.corelib.definitions.edm.entity.Address;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.AddressImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.api.external.model.zoho.ZohoOrganization;
import eu.europeana.enrichment.service.exception.ZohoAccessException;
import eu.europeana.enrichment.service.zoho.model.ZohoOrganizationAdapter;
import eu.europeana.metis.authentication.dao.ZohoAccessClientDao;
import eu.europeana.metis.authentication.dao.ZohoApiFields;
import eu.europeana.metis.exception.GenericMetisException;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
@Service
public class ZohoAccessService {

  private static final String URL_ORGANIZATION_PREFFIX = "http://data.europeana.eu/organization/";
  private static final String UNDEFINED_LANGUAGE_KEY = "def";
  private final ZohoAccessClientDao zohoAccessClientDao;

  
  /**
   * Constructor of class with required parameters
   *
   * @param zohoAccessClientDao {@link ZohoAccessClientDao}
   */
  @Autowired
  public ZohoAccessService(ZohoAccessClientDao zohoAccessClientDao) {
    this.zohoAccessClientDao = zohoAccessClientDao;
  }

  /**
   * This method retrieves OrganizationImpl object from Zoho organization
   * record.
   *
   * @return representation of the Zoho organization record in OrganizatinImpl
   * format
   */
  protected ZohoOrganization getOrganization(String organizationId)
      throws ZohoAccessException {

    JsonNode jsonRecordsResponse;
    try {
      jsonRecordsResponse = zohoAccessClientDao
          .getOrganizationById(organizationId);
    } catch (GenericMetisException e) {
      throw new ZohoAccessException(
          "Cannot get organization by id: " + organizationId, e);
    }
    JsonNode accountsNode = findRecordsByType(jsonRecordsResponse,
        ZohoApiFields.ACCOUNTS_MODULE_STRING);
    JsonNode jsonRecord = accountsNode
        .findValue(ZohoApiFields.FIELDS_LABEL);

    return new ZohoOrganizationAdapter(jsonRecord);
  }

  /**
   * This method retrieves a list of Zoho records in {@link JsonNode} format.
   *
   * @param type the type of the objects to be retrieved
   * @return {@link JsonNode} representation of the organization
   */
  JsonNode findRecordsByType(JsonNode jsonLeadsResponse, String type) {
    if (jsonLeadsResponse.get(ZohoApiFields.RESPONSE_STRING)
        .get(ZohoApiFields.RESULT_STRING) == null) {
      return null;
    }
    return jsonLeadsResponse.get(ZohoApiFields.RESPONSE_STRING)
        .get(ZohoApiFields.RESULT_STRING).get(type)
        .get(ZohoApiFields.ROW_STRING);
  }

  /**
   * Converter for {@link ZohoOrganization} to {@link Organization}
   * @param zohoOrganization the Zoho Organization object to convert
   * @return the converted Organization object
   */
  public Organization toEdmOrganization(ZohoOrganization zohoOrganization) {

    OrganizationImpl org = new OrganizationImpl();

    org.setAbout(URL_ORGANIZATION_PREFFIX + zohoOrganization.getZohoId());
    org.setDcIdentifier(createMapOfStringList(UNDEFINED_LANGUAGE_KEY, zohoOrganization.getZohoId()));
    String isoLanguage = toIsoLanguage(zohoOrganization.getLanguageForOrganizationName());
    org.setPrefLabel(createMapOfStringList(isoLanguage, zohoOrganization.getOrganizationName()));
    org.setAltLabel(createLanguageMapOfStringList(zohoOrganization.getAlternativeLanguage(),
        zohoOrganization.getAlternativeOrganizationName()));
    org.setEdmAcronym(createLanguageMapOfStringList(zohoOrganization.getLangAcronym(),
        zohoOrganization.getAcronym()));
    org.setFoafLogo(zohoOrganization.getLogo());
    org.setFoafHomepage(zohoOrganization.getWebsite());

    if (zohoOrganization.getRole() != null) {
      String[] role = zohoOrganization.getRole().split(";");
      org.setEdmEuropeanaRole(createLanguageMapOfStringList(
          Locale.ENGLISH.getLanguage(), Arrays.asList(role)));
    }
    org.setEdmOrganizationDomain(
        createMap(Locale.ENGLISH.getLanguage(), zohoOrganization.getDomain()));
    org.setEdmOrganizationSector(
        createMap(Locale.ENGLISH.getLanguage(), zohoOrganization.getSector()));
    org.setEdmOrganizationScope(
        createMap(Locale.ENGLISH.getLanguage(), zohoOrganization.getScope()));
    org.setEdmGeorgraphicLevel(createMap(Locale.ENGLISH.getLanguage(),
        zohoOrganization.getGeographicLevel()));
    String organizationCountry = toEdmCountry(zohoOrganization.getOrganizationCountry());
    org.setEdmCountry(
        createMap(Locale.ENGLISH.getLanguage(), organizationCountry));

    if (zohoOrganization.getSameAs() != null && !zohoOrganization.getSameAs().isEmpty()) {
      org.setOwlSameAs(zohoOrganization.getSameAs().toArray(new String[]{}));
    }

    // address
    Address address = new AddressImpl();
    address.setAbout(org.getAbout() + "#address");
    address.setVcardStreetAddress(zohoOrganization.getStreet());
    address.setVcardLocality(zohoOrganization.getCity());
    address.setVcardCountryName(zohoOrganization.getCountry());
    address.setVcardPostalCode(zohoOrganization.getZipCode());
    address.setVcardPostOfficeBox(zohoOrganization.getPostBox());
    org.setAddress(address);

    return org;
  }

  String toEdmCountry(String organizationCountry) {
    if (StringUtils.isBlank(organizationCountry)) {
      return null;
    }

    String isoCode = null;
    int commaSeparatorPos = organizationCountry.indexOf(',');
    //TODO: remove the support for FR(France), when Zoho data is updated and consistent.
    int bracketSeparatorPos = organizationCountry.indexOf('(');

    if (commaSeparatorPos > 0) {
      // example: FR(France)
      isoCode = organizationCountry.substring(commaSeparatorPos + 1).trim();
    } else if (bracketSeparatorPos > 0) {
      // example: France, FR
      isoCode = organizationCountry.substring(0, bracketSeparatorPos).trim();
    }

    return isoCode;
  }

  String toIsoLanguage(String language) {
    if (StringUtils.isBlank(language)) {
      return UNDEFINED_LANGUAGE_KEY;
    }

    return language.substring(0, 2).toLowerCase(Locale.US);
  }

  /**
   * Get organizations paged and lastModified date.
   *
   * @param start first index starts with 1
   * @param rows the number of entries to be returned
   * @param lastModified the date of last modification to check
   * @return the list of Zoho Organizations
   * @throws ZohoAccessException if an error occurred during accessing Zoho
   */
  public List<ZohoOrganization> getOrganizations(int start, int rows,
      Date lastModified) throws ZohoAccessException {
    return getOrganizations(start, rows, lastModified, null);
  }

  /**
   * Get organizations paged, lastModified date and searchCriteria.
   *
   * @param start first index starts with 1
   * @param rows the number of entries to be returned
   * @param lastModified the date of last modification to check
   * @param searchCriteria the searchCriteria to apply during the Zoho search
   * @return the list of Zoho Organizations
   * @throws ZohoAccessException if an error occurred during accessing Zoho
   */
  public List<ZohoOrganization> getOrganizations(int start, int rows,
      Date lastModified, Map<String, String> searchCriteria)
      throws ZohoAccessException {

    if (start < 1) {
      throw new ZohoAccessException(
          "Invalid start index. Index must be >= 1",
          new IllegalArgumentException("start: " + start));
    }

    JsonNode jsonRecordsResponse;
    String lastModifiedTime = null;
    if (lastModified != null) {
      lastModifiedTime = getDateFormatter().format(lastModified);
    }

    try {
      if (searchCriteria != null && !searchCriteria.isEmpty()) {
        jsonRecordsResponse = zohoAccessClientDao.searchOrganizations(
            start, rows, lastModifiedTime, searchCriteria);
      } else {
        jsonRecordsResponse = zohoAccessClientDao
            .getOrganizations(start, rows, lastModifiedTime);
      }
    } catch (GenericMetisException e) {
      throw new ZohoAccessException("Cannot get organization list from: "
          + start + " rows :" + rows, e);
    }
    return getOrganizationsListFromJsonNode(jsonRecordsResponse);
  }

  /**
   * This method retrieves map of records.
   *
   * @return map representation of the records
   */
  protected List<ZohoOrganization> getOrganizationsListFromJsonNode(
      JsonNode jsonRecordsResponse) throws ZohoAccessException {

    JsonNode accountsNode = findRecordsByType(jsonRecordsResponse,
        ZohoApiFields.ACCOUNTS_MODULE_STRING);
    List<ZohoOrganization> res = new ArrayList<>();
    if (accountsNode == null) {
      return res;
    }

    // one result in the response
    boolean oneResult = accountsNode.get(0) == null;
    if (oneResult) {
      addAccountToOrgList(accountsNode, res);
      return res;
    }

    // a list of results in the response
    for (JsonNode accountNode : accountsNode) {
      addAccountToOrgList(accountNode, res);
    }

    return res;
  }

  private void addAccountToOrgList(JsonNode accountNode,
      List<ZohoOrganization> res) throws ZohoAccessException {
    JsonNode organizationNode = accountNode.get(ZohoApiFields.FIELDS_LABEL);
    ZohoOrganization zoa = new ZohoOrganizationAdapter(organizationNode);
    res.add(zoa);
  }

  /**
   * Create OrganizationImpl map from value
   *
   * @param key The field name
   * @param value The value
   * @return map of strings and lists
   */
  Map<String, List<String>> createMapOfStringList(String key, String value) {
    if (value == null) {
      return null;
    }
    List<String> valueList = createList(value);
    return createMapOfStringList(key, valueList);
  }

  Map<String, String> createMap(String key, String value) {
    if (value == null) {
      return null;
    }
    Map<String, String> resMap = new HashMap<>();
    resMap.put(key, value);
    return resMap;
  }

  List<String> createList(String value) {
    if (value == null) {
      return null;
    }

    return Collections.singletonList(value);
  }

  /**
   * Create OrganizationImpl map from list of values
   *
   * @param key The field name
   * @param value The list of values
   * @return map of strings and lists
   */
  Map<String, List<String>> createMapOfStringList(String key,
      List<String> value) {
    Map<String, List<String>> resMap = new HashMap<>();
    resMap.put(key, value);
    return resMap;
  }

  Map<String, List<String>> createLanguageMapOfStringList(
      List<String> languages, List<String> values) {
    if (languages == null) {
      return null;
    }

    Map<String, List<String>> resMap = new HashMap<>();
    for (int i = 0; i < languages.size(); i++) {
      resMap.put(toIsoLanguage(languages.get(i)),
          createList(values.get(i)));
    }
    return resMap;
  }

  Map<String, List<String>> createLanguageMapOfStringList(String language,
      String value) {

    if (value == null) {
      return null;
    }

    Map<String, List<String>> resMap = new HashMap<>();
    resMap.put(toIsoLanguage(language), createList(value));
    return resMap;
  }

  Map<String, List<String>> createLanguageMapOfStringList(String language,
      List<String> value) {

    if (value == null) {
      return null;
    }

    Map<String, List<String>> resMap = new HashMap<>();
    resMap.put(toIsoLanguage(language), value);
    return resMap;
  }

  public FastDateFormat getDateFormatter() {
    return ZohoApiFields.getZohoTimeFormatter();
  }
}
