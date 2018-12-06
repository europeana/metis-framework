package eu.europeana.enrichment.service.zoho;

import com.zoho.crm.library.crud.ZCRMRecord;
import com.zoho.crm.library.crud.ZCRMTrashRecord;
import eu.europeana.corelib.definitions.edm.entity.Address;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.AddressImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.service.EntityConverterUtils;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
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
