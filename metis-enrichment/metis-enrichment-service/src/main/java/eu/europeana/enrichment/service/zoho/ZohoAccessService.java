package eu.europeana.enrichment.service.zoho;

import com.zoho.crm.library.api.response.BulkAPIResponse;
import com.zoho.crm.library.crud.ZCRMModule;
import com.zoho.crm.library.crud.ZCRMRecord;
import com.zoho.crm.library.crud.ZCRMTrashRecord;
import com.zoho.crm.library.exception.ZCRMException;
import com.zoho.crm.library.setup.restclient.ZCRMRestClient;
import eu.europeana.corelib.definitions.edm.entity.Address;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.AddressImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.service.EntityConverterUtils;
import eu.europeana.enrichment.service.exception.ZohoAccessException;
import eu.europeana.metis.zoho.ZohoApiFields;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-10-27
 */
@Service
public class ZohoAccessService {

  public static final String URL_ORGANIZATION_PREFFIX = "http://data.europeana.eu/organization/";
  private static final String UNDEFINED_LANGUAGE_KEY = "def";
  private static final int LANGUAGE_CODE_LENGTH = 2;
  private static final int MAX_ALTERNATIVES = 5;
  private static final int MAX_LANG_ALTERNATIVES = 5;
  private static final int MAX_SAME_AS = 3;
  private final EntityConverterUtils entityConverterUtils = new EntityConverterUtils();

  /**
   * Constructor of class with required parameters
   */
  @Autowired
  public ZohoAccessService() throws Exception {
    //Initialize Zoho client with default configuration locations
    ZCRMRestClient.initialize();
  }

  //  /**
//   * This method retrieves OrganizationImpl object from Zoho organization record.
//   *
//   * @param organizationId the organization id used to retrieve the organization information
//   * @return representation of the Zoho organization record in OrganizatinImpl format
//   * @throws ZohoAccessException if an error occurred during retrieval from Zoho
//   */
  public ZCRMRecord getOrganization(String organizationId)
      throws ZohoAccessException {
    ZCRMModule zcrmModuleAccounts = ZCRMModule.getInstance("Accounts");
    final BulkAPIResponse bulkAPIResponseAccounts;
    try {
      bulkAPIResponseAccounts = zcrmModuleAccounts
          .searchByCriteria(
              String.format("(id:equals:%s)", organizationId));
    } catch (ZCRMException e) {
      throw new ZohoAccessException("Zoho search by email threw an exception", e);
    }
    final List<ZCRMRecord> zcrmRecords = (List<ZCRMRecord>) bulkAPIResponseAccounts.getData();
    if (zcrmRecords.isEmpty()) {
      throw new ZohoAccessException("Organization Role from Zoho is empty");
    }

    return zcrmRecords.get(0);
  }


  /**
   * Converter for {@link ZCRMRecord} containing Zoho organization info to {@link Organization}
   *
   * @param zohoOrganization the Zoho Organization object to convert
   * @return the converted Organization object
   */
  public Organization toEdmOrganization(ZCRMRecord zohoOrganization) {

    OrganizationImpl org = new OrganizationImpl();

    final HashMap<String, Object> zohoOrganizationFields = zohoOrganization.getData();

    org.setAbout(URL_ORGANIZATION_PREFFIX + zohoOrganization.getEntityId());
    org.setDcIdentifier(getEntityConverterUtils().createMapWithLists(UNDEFINED_LANGUAGE_KEY,
        Long.toString(zohoOrganization.getEntityId())));

    String isoLanguage = toIsoLanguage(
        (String) zohoOrganizationFields.get("Lang_Organisation_Name"));
    org.setPrefLabel(getEntityConverterUtils()
        .createMapWithLists(isoLanguage, (String) zohoOrganizationFields.get("Account_Name")));
    org.setAltLabel(getEntityConverterUtils().createMapWithLists(
        getFieldArray(zohoOrganizationFields, "Lang_Alternative", MAX_LANG_ALTERNATIVES),
        getFieldArray(zohoOrganizationFields, "Alternative", MAX_ALTERNATIVES)));
    org.setEdmAcronym(getEntityConverterUtils().createLanguageMapOfStringList(
        (String) zohoOrganizationFields.get("Lang_Acronym"),
        (String) zohoOrganizationFields.get("Acronym")));
    org.setFoafLogo((String) zohoOrganizationFields.get("Logo_link_to_WikimediaCommons"));
    org.setFoafHomepage((String) zohoOrganizationFields.get("Website"));
    final List<String> organizationRoleStringList = (List<String>) zohoOrganizationFields
        .get("Organisation_Role2");
    if (organizationRoleStringList != null) {
      org.setEdmEuropeanaRole(getEntityConverterUtils()
          .createLanguageMapOfStringList(Locale.ENGLISH.getLanguage(), organizationRoleStringList));
    }
    final List<String> organizationDomainStringList = (List<String>) zohoOrganizationFields
        .get("Domain2");
    org.setEdmOrganizationDomain(getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(),
        CollectionUtils.isEmpty(organizationDomainStringList) ? null
            : organizationDomainStringList.get(0)));
    org.setEdmOrganizationSector(getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(),
        (String) zohoOrganizationFields.get("Sector")));
    org.setEdmOrganizationScope(getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(),
        (String) zohoOrganizationFields.get("Scope")));
    final List<String> geographicLevelList = (List<String>) zohoOrganizationFields
        .get("Geographic_Level");
    org.setEdmGeorgraphicLevel(getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(),
        CollectionUtils.isEmpty(geographicLevelList) ? null : geographicLevelList.get(0)));
    String organizationCountry = toEdmCountry((String) zohoOrganizationFields.get("Country1"));
    org.setEdmCountry(
        getEntityConverterUtils().createMap(Locale.ENGLISH.getLanguage(), organizationCountry));
    final List<String> sameAsList = getFieldArray(zohoOrganizationFields, "SameAs", MAX_SAME_AS);
    if (!CollectionUtils.isEmpty(sameAsList)) {
      org.setOwlSameAs(sameAsList.toArray(new String[]{}));
    }

    // address
    Address address = new AddressImpl();
    address.setAbout(org.getAbout() + "#address");
    address.setVcardStreetAddress((String) zohoOrganizationFields.get("Street"));
    address.setVcardLocality((String) zohoOrganizationFields.get("City"));
    address.setVcardCountryName((String) zohoOrganizationFields.get(
        "Country")); //This is the Address Information Country as opposed to the Organization one above
    address.setVcardPostalCode((String) zohoOrganizationFields.get("ZIP_code"));
    address.setVcardPostOfficeBox((String) zohoOrganizationFields.get("PO_box"));
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
  public List<ZCRMRecord> getOrganizations(int start, int rows,
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
  public List<ZCRMRecord> getOrganizations(int start, int rows,
      Date lastModified, Map<String, String> searchCriteria)
      throws ZohoAccessException {

    if (start < 1) {
      throw new ZohoAccessException(
          "Invalid start index. Index must be >= 1",
          new IllegalArgumentException("start: " + start));
    }

    String lastModifiedTime = null;
    if (lastModified != null) {
      lastModifiedTime = ZohoApiFields.getZohoTimeFormatter().format(lastModified);
    }

    ZCRMModule zcrmModuleAccounts = ZCRMModule.getInstance("Accounts");
    final BulkAPIResponse bulkAPIResponseRecordsOrganizations;
    try {
      if (searchCriteria == null || searchCriteria.isEmpty()) {//No searchCriteria available
        bulkAPIResponseRecordsOrganizations = zcrmModuleAccounts
            .getRecords(null, null, null, start, rows, lastModifiedTime, null, false);
      } else {
        // TODO: 5-12-18 If last modified is required here it should be part of the criteria and the method that creates the string below it should support it
        bulkAPIResponseRecordsOrganizations = zcrmModuleAccounts
            .searchByCriteria(createZohoCriteriaString(searchCriteria), start, rows);
      }
    } catch (ZCRMException e) {
      throw new ZohoAccessException("Cannot get organization list from: "
          + start + " rows :" + rows, e);
    }

    return (List<ZCRMRecord>) bulkAPIResponseRecordsOrganizations
        .getData();
  }

  // TODO: 5-12-18 This criteria creation is for equals only, for last modified there should be an extension here
  private String createZohoCriteriaString(Map<String, String> searchCriteria) {
    if (searchCriteria == null || searchCriteria.isEmpty()) {
      return null;
    }

    String[] filterCriteria;
    StringBuilder criteriaStringBuilder = new StringBuilder();

    for (Map.Entry<String, String> entry : searchCriteria.entrySet()) {
      criteriaStringBuilder = new StringBuilder();
      filterCriteria = entry.getValue().split(ZohoApiFields.DELIMITER_COMMA);

      for (String filter : filterCriteria) {
        criteriaStringBuilder
            .append(String.format("(%s:equals:%s)", entry.getKey(), filter.trim()));
        criteriaStringBuilder.append(ZohoApiFields.OR);
      }
    }

    // remove last OR
    criteriaStringBuilder.delete(criteriaStringBuilder.length() - ZohoApiFields.OR.length(),
        criteriaStringBuilder.length());
    return criteriaStringBuilder.toString();

  }

  /**
   * Get deleted organizations paged.
   *
   * @param startPage The number of start page first index starts with 1. Zoho response has maximal
   * 200 items
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
    ZCRMModule zcrmModuleAccounts = ZCRMModule.getInstance("Accounts");
    final BulkAPIResponse bulkAPIResponseDeletedRecords;
    try {
      bulkAPIResponseDeletedRecords = zcrmModuleAccounts
          .getDeletedRecords(startPage, 200);
    } catch (ZCRMException e) {
      throw new ZohoAccessException("Cannot get deleted organization list from: "
          + startPage, e);
    }
    final List<ZCRMTrashRecord> zcrmRecordsDeletedOrganizations = (List<ZCRMTrashRecord>) bulkAPIResponseDeletedRecords
        .getData();

    return extractIdsFromDeletedZohoOrganizations(zcrmRecordsDeletedOrganizations);
  }

  /**
   * This method extracts organization URIs from deleted organizations.
   *
   * @param deletedOrganizationsList The list of deleted organizations
   * @return ID list
   */
  private List<String> extractIdsFromDeletedZohoOrganizations(
      List<ZCRMTrashRecord> deletedOrganizationsList) {
    List<String> idList = new ArrayList<>(deletedOrganizationsList.size());

    for (ZCRMTrashRecord deletedOrganization : deletedOrganizationsList) {
      idList.add(ZohoAccessService.URL_ORGANIZATION_PREFFIX + deletedOrganization.getEntityId());
    }
    return idList;
  }

  private List<String> getFieldArray(
      HashMap<String, Object> zohoOrganizationFields,
      String fieldBaseName, int size) {
    List<String> res = new ArrayList<>(size);
    String fieldName = fieldBaseName + "_" + "%d";
    for (int i = 0; i < size; i++) {
      String fieldValue = (String) zohoOrganizationFields.get(String.format(fieldName, i));
      // add only existing values
      if (StringUtils.isNotBlank(fieldValue)) {
        res.add(fieldValue);
      }
    }
    if (res.isEmpty()) {
      return null;
    }
    return res;
  }

  public EntityConverterUtils getEntityConverterUtils() {
    return entityConverterUtils;
  }
}
