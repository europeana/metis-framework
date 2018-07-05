package eu.europeana.enrichment.service.zoho;

import java.io.File;
import java.io.IOException;
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
import eu.europeana.enrichment.service.EntityConverterUtils;
import eu.europeana.enrichment.service.dao.ZohoV2AccessDao;
import eu.europeana.enrichment.service.exception.ZohoAccessException;
import eu.europeana.enrichment.service.zoho.model.DeletedZohoOrganizationAdapter;
import eu.europeana.enrichment.service.zoho.model.ZohoOrganizationAdapter;
import eu.europeana.metis.authentication.dao.ZohoAccessClientDao;
import eu.europeana.metis.authentication.dao.ZohoApi2Fields;
import eu.europeana.metis.authentication.dao.ZohoApiFields;
import eu.europeana.metis.exception.GenericMetisException;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 * TODO JV currently this class seems to be only used by test code. Move it there?
 */
@Service
public class ZohoAccessService {

  public static final String URL_ORGANIZATION_PREFFIX = "http://data.europeana.eu/organization/";
  private static final String UNDEFINED_LANGUAGE_KEY = "def";
  private static final int LANGUAGE_CODE_LENGTH = 2;
  private final ZohoAccessClientDao zohoAccessClientDao;
  private final ZohoV2AccessDao zohoV2AccessDao;
  private EntityConverterUtils entityConverterUtils = new EntityConverterUtils();

  /**
   * Constructor of class with required parameters
   *
   * @param zohoAccessClientDao {@link ZohoAccessClientDao}
   * @param zohoV2AccessDao {@link ZohoV2AccessDao}
   */
  @Autowired
  public ZohoAccessService(ZohoAccessClientDao zohoAccessClientDao,
      ZohoV2AccessDao zohoV2AccessDao) {
    this.zohoAccessClientDao = zohoAccessClientDao;
    this.zohoV2AccessDao = zohoV2AccessDao;
  }

  /**
   * This method retrieves OrganizationImpl object from Zoho organization record.
   *
   * @param organizationId the organization id used to retrieve the organization information
   * @return representation of the Zoho organization record in OrganizatinImpl format
   * @throws ZohoAccessException if an error occurred during retrieval from Zoho
   */
  public ZohoOrganization getOrganization(String organizationId) throws ZohoAccessException {

    JsonNode jsonRecordsResponse;
    try {
      jsonRecordsResponse = zohoAccessClientDao.getOrganizationById(organizationId);
    } catch (GenericMetisException e) {
      throw new ZohoAccessException("Cannot get organization by id: " + organizationId, e);
    }
    JsonNode accountsNode =
        findRecordsByType(jsonRecordsResponse, ZohoApiFields.ACCOUNTS_MODULE_STRING);
    JsonNode jsonRecord = accountsNode.findValue(ZohoApiFields.FIELDS_LABEL);

    return new ZohoOrganizationAdapter(jsonRecord);
  }

  /**
   * This method retrieves OrganizationImpl object for Zoho organization record from given file.
   *
   * @param contentFile file that contains a Zoho Organization response
   * @return representation of the Zoho organization record in OrganizatinImpl format
   * @throws ZohoAccessException if an error occurred during retrieval from Zoho
   */
  public ZohoOrganization getOrganizationFromFile(File contentFile) throws ZohoAccessException {

    JsonNode jsonRecordsResponse;
    try {
      jsonRecordsResponse = zohoAccessClientDao.getOrganizationFromFile(contentFile);
    } catch (IOException e) {
      throw new ZohoAccessException("Cannot get organization from file. ", e);
    } catch (GenericMetisException e) {
      throw new ZohoAccessException("Cannot extract organization from file. ", e);
    }
    JsonNode accountsNode =
        findRecordsByType(jsonRecordsResponse, ZohoApiFields.ACCOUNTS_MODULE_STRING);
    JsonNode jsonRecord = accountsNode.findValue(ZohoApiFields.FIELDS_LABEL);

    return new ZohoOrganizationAdapter(jsonRecord);
  }

  /**
   * This method retrieves a list of Zoho records in {@link JsonNode} format.
   *
   * @param jsonLeadsResponse the parserd Zoho response
   * @param type the type of the objects to be retrieved
   * @return {@link JsonNode} representation of the organization
   */
  JsonNode findRecordsByType(JsonNode jsonLeadsResponse, String type) {
    if (jsonLeadsResponse.get(ZohoApiFields.RESPONSE_STRING)
        .get(ZohoApiFields.RESULT_STRING) == null) {
      return null;
    }
    return jsonLeadsResponse.get(ZohoApiFields.RESPONSE_STRING).get(ZohoApiFields.RESULT_STRING)
        .get(type).get(ZohoApiFields.ROW_STRING);
  }


  /**
   * Converter for {@link ZohoOrganization} to {@link Organization}
   *
   * @param zohoOrganization the Zoho Organization object to convert
   * @return the converted Organization object
   */
  public Organization toEdmOrganization(ZohoOrganization zohoOrganization) {

    OrganizationImpl org = new OrganizationImpl();

    org.setAbout(URL_ORGANIZATION_PREFFIX + zohoOrganization.getZohoId());
    org.setDcIdentifier(getEntityConverterUtils().createMapWithLists(UNDEFINED_LANGUAGE_KEY,
        zohoOrganization.getZohoId()));
    String isoLanguage = toIsoLanguage(zohoOrganization.getLanguageForOrganizationName());
    org.setPrefLabel(getEntityConverterUtils().createMapWithLists(isoLanguage,
        zohoOrganization.getOrganizationName()));
    org.setAltLabel(getEntityConverterUtils().createMapWithLists(
        zohoOrganization.getAlternativeLanguage(),
        zohoOrganization.getAlternativeOrganizationName()));
    org.setEdmAcronym(getEntityConverterUtils().createLanguageMapOfStringList(
        zohoOrganization.getLangAcronym(), zohoOrganization.getAcronym()));
    org.setFoafLogo(zohoOrganization.getLogo());
    org.setFoafHomepage(zohoOrganization.getWebsite());

    if (zohoOrganization.getRole() != null) {
      String[] role = zohoOrganization.getRole().split(";");
      org.setEdmEuropeanaRole(getEntityConverterUtils()
          .createLanguageMapOfStringList(Locale.ENGLISH.getLanguage(), Arrays.asList(role)));
    }
    org.setEdmOrganizationDomain(getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(),
        zohoOrganization.getDomain()));
    org.setEdmOrganizationSector(getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(),
        zohoOrganization.getSector()));
    org.setEdmOrganizationScope(getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(),
        zohoOrganization.getScope()));
    org.setEdmGeorgraphicLevel(getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(),
        zohoOrganization.getGeographicLevel()));
    String organizationCountry = toEdmCountry(zohoOrganization.getOrganizationCountry());
    org.setEdmCountry(
        getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(), organizationCountry));

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
    // TODO: remove the support for FR(France), when Zoho data is updated and consistent.
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

    return language.substring(0, LANGUAGE_CODE_LENGTH).toLowerCase(Locale.US);
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
      if (searchCriteria == null || searchCriteria.isEmpty()) {//No searchCriteria available
        jsonRecordsResponse = zohoAccessClientDao
            .getOrganizations(start, rows, lastModifiedTime);
      } else {
        jsonRecordsResponse = zohoAccessClientDao.searchOrganizations(
            start, rows, lastModifiedTime, searchCriteria);
      }
    } catch (GenericMetisException e) {
      throw new ZohoAccessException("Cannot get organization list from: "
          + start + " rows :" + rows, e);
    }
    return getOrganizationsListFromJsonNode(jsonRecordsResponse);
  }

  /**
   * Get deleted organizations paged.
   *
   * @param startPage The number of start page first index starts with 1. Zoho response
   * has maximal 200 items
   * @return the list of deleted Zoho Organizations
   * @throws ZohoAccessException if an error occurred during accessing Zoho
   */
  public List<String> getDeletedOrganizations(int startPage)
      throws ZohoAccessException {

    if (startPage < 1) {
      throw new ZohoAccessException(
          "Invalid start page index. Index must be >= 1",
          new IllegalArgumentException("start page: " + startPage));
    }

    JsonNode jsonRecordsResponse;
    try {
      jsonRecordsResponse = zohoV2AccessDao
          .getDeletedOrganizations(startPage);
    } catch (GenericMetisException e) {
      throw new ZohoAccessException("Cannot get deleted organization list from: "
          + startPage, e);
    }

    List<DeletedZohoOrganizationAdapter> deletedOrganizationsList =
        getDeletedOrganizationsListFromJsonNode(jsonRecordsResponse);
    return extractIdsFromDeletedZohoOrganizations(deletedOrganizationsList);
  }

  /**
   * This method extracts organization URIs from deleted organizations.
   *
   * @param deletedOrganizationsList The list of deleted organizations
   * @return ID list
   */
  private List<String> extractIdsFromDeletedZohoOrganizations(
      List<DeletedZohoOrganizationAdapter> deletedOrganizationsList) {
    List<String> idList = new ArrayList<>(deletedOrganizationsList.size());

    for (DeletedZohoOrganizationAdapter deletedOrganization : deletedOrganizationsList) {
      idList.add(ZohoAccessService.URL_ORGANIZATION_PREFFIX + deletedOrganization.getZohoId());
    }
    return idList;
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

  /**
   * This method retrieves map of records for deleted organizations.
   *
   * @return map representation of the records
   */
  protected List<DeletedZohoOrganizationAdapter> getDeletedOrganizationsListFromJsonNode(
      JsonNode jsonRecordsResponse) {

    JsonNode accountsNode = jsonRecordsResponse.get(ZohoApi2Fields.DATA_STRING);
    List<DeletedZohoOrganizationAdapter> res = new ArrayList<>();
    if (accountsNode == null) {
      return res;
    }

    // one result in the response
    boolean oneResult = accountsNode.get(0) == null;
    if (oneResult) {
      res.add(new DeletedZohoOrganizationAdapter(accountsNode));
      return res;
    }

    // a list of results in the response
    for (JsonNode accountNode : accountsNode) {
      res.add(new DeletedZohoOrganizationAdapter(accountNode));
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
    return createMapOfStringList(key, createList(value));
  }

  Map<String, String> createMap(String key, String value) {
    if (value == null) {
      return null;
    }
    return Collections.singletonMap(key, value);
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
    return Collections.singletonMap(key, value);
  }

  Map<String, List<String>> createLanguageMapOfStringList(List<String> languages,
      List<String> values) {
    if (languages == null) {
      return null;
    }
    Map<String, List<String>> resMap = new HashMap<>(languages.size());
    for (int i = 0; i < languages.size(); i++) {
      resMap.put(toIsoLanguage(languages.get(i)), createList(values.get(i)));
    }
    return resMap;
  }

  Map<String, List<String>> createLanguageMapOfStringList(String language,
      String value) {
    if (value == null) {
      return null;
    }
    return Collections.singletonMap(toIsoLanguage(language), createList(value));
  }

  Map<String, List<String>> createLanguageMapOfStringList(String language,
      List<String> value) {
    if (value == null) {
      return null;
    }
    return Collections.singletonMap(toIsoLanguage(language), value);
  }

  public FastDateFormat getDateFormatter() {
    return ZohoApiFields.getZohoTimeFormatter();
  }

  public EntityConverterUtils getEntityConverterUtils() {
    return entityConverterUtils;
  }
}
