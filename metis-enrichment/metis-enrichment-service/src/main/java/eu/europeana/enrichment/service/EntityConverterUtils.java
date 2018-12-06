package eu.europeana.enrichment.service;

import com.zoho.crm.library.crud.ZCRMRecord;
import com.zoho.crm.library.crud.ZCRMTrashRecord;
import eu.europeana.corelib.definitions.edm.entity.Address;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.AddressImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.api.external.model.TextProperty;
import eu.europeana.enrichment.api.external.model.WebResource;
import eu.europeana.metis.zoho.ZohoConstants;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.BinaryOperator;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

/**
 * This class supports conversion of different organization objects into OrganizationImpl object.
 *
 * @author GrafR
 */
public class EntityConverterUtils {

  public static final String URL_ORGANIZATION_PREFFIX = "http://data.europeana.eu/organization/";
  private static final String UNDEFINED_LANGUAGE_KEY = "def";
  private static final int LANGUAGE_CODE_LENGTH = 2;
  private static final int MAX_ALTERNATIVES = 5;
  private static final int MAX_LANG_ALTERNATIVES = 5;
  private static final int MAX_SAME_AS = 3;

  /**
   * Create singleton map mapping to a list.
   *
   * @param key The key
   * @param value The value
   * @return map with list, or null if the value is null.
   */
  public Map<String, List<String>> createMapWithLists(String key, String value) {
    if (value == null) {
      return null;
    }
    return Collections.singletonMap(key, Collections.singletonList(value));
  }

  /**
   * Create singleton map.
   *
   * @param key The key.
   * @param value The value.
   * @return The map, or null if the value is null.
   */
  public Map<String, String> createMap(String key, String value) {
    if (value == null) {
      return null;
    }
    return Collections.singletonMap(key, value);
  }

  /**
   * Create singleton list.
   *
   * @param value The value.
   * @return The list, or null if the value is null.
   */
  List<String> createList(String value) {
    if (value == null) {
      return null;
    }
    return Collections.singletonList(value);
  }

  /**
   * Create map mapping to a list. The map will contain all keys and the associated values (that
   * have the same index as its key). Note: in case of duplicate keys only one of the values will be
   * present in the result.
   *
   * @param keys The list of keys.
   * @param values The list of associated values.
   * @return The map, or null if it is empty.
   */
  public Map<String, List<String>> createMapWithLists(List<String> keys, List<String> values) {
    if (keys == null || keys.isEmpty()) {
      return null;
    }
    final Map<String, List<String>> resMap = new HashMap<>(keys.size());
    for (int i = 0; i < keys.size(); i++) {
      resMap.put(toIsoLanguage(keys.get(i)), createList(values.get(i)));
    }
    return resMap;
  }

  /**
   * This method creates language map from text property object list. Values are merged if the same
   * key is encountered
   *
   * @param textPropertyList the list of text property objects
   * @return the language map
   */
  public Map<String, List<String>> createMapWithListsFromTextPropertyListMerging(
      List<? extends TextProperty> textPropertyList) {
    return createMapWithListsFromTextPropertyList(textPropertyList, true);
  }

  /**
   * This method creates language map from text property object list. Values are ignored if the same
   * key is encountered.
   *
   * @param textPropertyList the list of text property objects
   * @return the language map
   */
  public Map<String, List<String>> createMapWithListsFromTextPropertyListNonMerging(
      List<? extends TextProperty> textPropertyList) {
    return createMapWithListsFromTextPropertyList(textPropertyList, false);
  }

  /**
   * This method creates language map from text property object list. Lists are <b>not</b>
   * concatenated: if two properties with the same language are passed, one of them will be
   * ignored.
   *
   * @param textPropertyList the list of text property objects
   * @param mergeLists determines what to do in case two properties with the same language are
   * found. If true, the lists will be merged. If false, one of the properties will be ignored.
   * @return the language map
   */
  private Map<String, List<String>> createMapWithListsFromTextPropertyList(
      List<? extends TextProperty> textPropertyList, boolean mergeLists) {

    // Sanity check.
    if (textPropertyList == null) {
      return null;
    }

    // Determine what to do with language collisions.
    final BinaryOperator<List<String>> mergeOperator;
    if (mergeLists) {
      mergeOperator = this::mergeStringLists;
    } else {
      mergeOperator = (list1, list2) -> list1;
    }

    // Convert the list to a map.
    return textPropertyList.stream()
        .collect(Collectors.toMap(property -> toIsoLanguage(property.getKey()),
            property -> createList(property.getValue()), mergeOperator));
  }

  /**
   * This method creates language map from text property object list. Note: if two properties with
   * the same language are passed, one of them will be ignored.
   *
   * @param textPropertyList The list of text property objects
   * @return The language map
   */
  public Map<String, String> createMapFromTextPropertyList(
      List<? extends TextProperty> textPropertyList) {
    if (textPropertyList == null) {
      return null;
    }
    return textPropertyList.stream()
        .collect(Collectors.toMap(property -> toIsoLanguage(property.getKey()),
            TextProperty::getValue, (value1, value2) -> value1));
  }

  /**
   * This method converts a list of web resource objects to a string array.
   *
   * @param resources The list of web resource objects
   * @return string array, or null if no resources are found.
   */
  public String[] createStringArrayFromPartList(List<? extends WebResource> resources) {
    if (resources == null) {
      return null;
    }
    return resources.stream().map(WebResource::getResourceUri).toArray(String[]::new);
  }

  /**
   * Creates a map of a single key and a List with a single value
   *
   * @param language the language to use
   * @param value the element value
   * @return the created map
   */
  public Map<String, List<String>> createLanguageMapOfStringList(String language, String value) {
    if (value == null) {
      return null;
    }
    return Collections.singletonMap(toIsoLanguage(language), createList(value));
  }

  /**
   * Creates a map of a single key and maps it to the provided list.
   *
   * @param language the language to use
   * @param value the list of values
   * @return the created map
   */
  public Map<String, List<String>> createLanguageMapOfStringList(String language,
      List<String> value) {
    if (value == null) {
      return null;
    }
    return Collections.singletonMap(toIsoLanguage(language), value);
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

  /**
   * @param language
   * @return
   */
  private static String toIsoLanguage(String language) {
    if (StringUtils.isBlank(language)) {
      return UNDEFINED_LANGUAGE_KEY;
    }
    return language.substring(0, LANGUAGE_CODE_LENGTH).toLowerCase(Locale.US);
  }

  /**
   * This method merges two maps with lists. The result will be one map with all values. If a key
   * occurs in both input maps, the result map will contain a merged list for the given key. When
   * adding values from the add map to the base map, duplicates are prevented.
   *
   * @param baseMap The original map to which the new values will be added
   * @param addMap the map containing new values to be added to the map
   * @return enriched base map
   */
  public Map<String, List<String>> mergeMapsWithLists(Map<String, List<String>> baseMap,
      Map<String, List<String>> addMap) {

    // If both maps are null, we are done.
    if (baseMap == null && addMap == null) {
      return null;
    }

    // Fill the result with the base map.
    final Map<String, List<String>> result = new HashMap<>();
    if (baseMap != null) {
      result.putAll(baseMap);
    }

    // Merge the add map data.
    if (addMap != null) {
      for (Map.Entry<String, List<String>> entry : addMap.entrySet()) {
        result.merge(entry.getKey(), new ArrayList<>(entry.getValue()), this::mergeStringLists);
      }
    }

    // Return the result.
    if (result.isEmpty()) {
      return null;
    }
    return result;
  }

  /**
   * <p>
   * This method merges values into the base map. This method assumes (but does not require) that
   * both the base and the add maps map a string key to a <b>singleton</b> list (this is not true
   * for the not merged map). The new base map (also containing only singleton lists) is returned,
   * and the not merged map will be updated to contain all values in the add map that could not be
   * added to the base map.
   * </p>
   *
   * @param baseMap The base map containing singleton lists. This map will not be changed. Is not
   * null.
   * @param addMap The add map containing singleton lists. This map will not be changed. Is not
   * null.
   * @param notMergedMap The not merged map, containing lists of arbitrary size. This map will be
   * updated to contain those values in the add map that could not be added to the base map. Is not
   * null.
   * @return The new merged base map. Is not null.
   */
  public Map<String, List<String>> mergeMapsWithSingletonLists(Map<String, List<String>> baseMap,
      Map<String, List<String>> addMap, Map<String, List<String>> notMergedMap) {

    // Create the result from the base map.
    final Map<String, List<String>> result = new HashMap<>(baseMap);

    // Process the entries in the add map.
    for (Map.Entry<String, List<String>> entry : addMap.entrySet()) {
      final String key = entry.getKey();
      if (result.containsKey(key)) {
        // If it does, add to the not-merged-map that which is not already in the base map.
        final List<String> unmergedValues = entry.getValue().stream().distinct()
            .filter(value -> !result.get(key).contains(value)).collect(Collectors.toList());
        if (!unmergedValues.isEmpty()) {
          notMergedMap.merge(key, unmergedValues, this::mergeStringLists);
        }
      } else {
        // If the base map has no singleton for the key, add it.
        result.put(key, new ArrayList<>(entry.getValue()));
      }
    }

    // Done.
    return result;
  }

  /**
   * This method extends base list by elements of add list. The result will not contain any
   * duplicates.
   *
   * @param baseList The base list, or null (which is treated as an empty list).
   * @param addList The list of items to add, or null (which is treated as an empty list).
   * @return the list containing the sum of the two provided lists, or null if there are no items.
   */
  public List<String> mergeStringLists(List<String> baseList, List<String> addList) {
    final Set<String> result = new HashSet<>();
    if (baseList != null) {
      result.addAll(baseList);
    }
    if (addList != null) {
      result.addAll(addList);
    }
    return result.isEmpty() ? null : new ArrayList<>(result);
  }

  /**
   * This method is a wrapper for {@link #mergeStringLists(List, List)}. It converts the arrays to
   * lists, executes that method and converts it back.
   *
   * @param base The base string array, or null (which is treated as an empty array).
   * @param add The array of items to add, or null (which is treated as an empty array).
   * @return the resulting string array, or null if there are no items.
   */
  public String[] mergeStringArrays(String[] base, String[] add) {
    final List<String> baseList = base == null ? Collections.emptyList() : Arrays.asList(base);
    final List<String> addList = add == null ? Collections.emptyList() : Arrays.asList(add);
    final List<String> mergedList = mergeStringLists(baseList, addList);
    return mergedList == null ? null : mergedList.toArray(new String[0]);
  }

  /**
   * Merges two addresses. If the base organization already has an address set that contains any
   * non-country data then nothing happens. Otherwise, the address properties from the
   * addOrganization are copied to the baseOrganization.
   *
   * @param baseOrganization The base organization. Cannot be null.
   * @param addOrganization The add organization. Cannot be null.
   */
  public void mergeAddress(Organization baseOrganization, Organization addOrganization) {

    // If does, or the add organization has nothing to replace, we're done.
    if (addOrganization.getAddress() == null) {
      return;
    }

    // Make sure that the base address is not null
    if (baseOrganization.getAddress() == null) {
      baseOrganization.setAddress(new AddressImpl());
    }

    // Copy the values from the add organization. Copy the country only if there is a value
    // (otherwise keep the base country).
    final Address baseAddress = baseOrganization.getAddress();
    final Address addAddress = addOrganization.getAddress();

    // TODO: enable when the issues related to locality and street address are solved in the mapping
    // baseAddress.setVcardLocality(addAddress.getVcardLocality());
    // baseAddress.setVcardPostalCode(addAddress.getVcardPostalCode());
    // baseAddress.setVcardPostOfficeBox(addAddress.getVcardPostOfficeBox());
    // baseAddress.setVcardStreetAddress(addAddress.getVcardStreetAddress());

    //update country only if not available in base
    if (StringUtils.isEmpty(baseAddress.getVcardCountryName())
        && StringUtils.isNotEmpty(addAddress.getVcardCountryName())) {
      baseAddress.setVcardCountryName(addAddress.getVcardCountryName());
    }

    //update hasGeo if not available in base
    if(StringUtils.isEmpty(baseAddress.getVcardHasGeo())){
      baseAddress.setVcardHasGeo(addAddress.getVcardHasGeo());
    }

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
    org.setDcIdentifier(createMapWithLists(UNDEFINED_LANGUAGE_KEY,
        Long.toString(zohoOrganization.getEntityId())));

    String isoLanguage = toIsoLanguage(
        (String) zohoOrganizationFields.get(ZohoConstants.LANG_ORGANIZATION_NAME_FIELD));
    org.setPrefLabel(createMapWithLists(isoLanguage,
        (String) zohoOrganizationFields.get(ZohoConstants.ACCOUNT_NAME_FIELD)));
    org.setAltLabel(createMapWithLists(
        getFieldArray(zohoOrganizationFields, ZohoConstants.LANG_ALTERNATIVE_FIELD,
            MAX_LANG_ALTERNATIVES),
        getFieldArray(zohoOrganizationFields, ZohoConstants.ALTERNATIVE_FIELD, MAX_ALTERNATIVES)));
    org.setEdmAcronym(createLanguageMapOfStringList(
        (String) zohoOrganizationFields.get(ZohoConstants.LANG_ACRONYM_FIELD),
        (String) zohoOrganizationFields.get(ZohoConstants.ACRONYM_FIELD)));
    org.setFoafLogo(
        (String) zohoOrganizationFields.get(ZohoConstants.LOGO_LINK_TO_WIKIMEDIACOMMONS_FIELD));
    org.setFoafHomepage((String) zohoOrganizationFields.get(ZohoConstants.WEBSITE_FIELD));
    final List<String> organizationRoleStringList = (List<String>) zohoOrganizationFields
        .get(ZohoConstants.ORGANIZATION_ROLE_FIELD);
    if (organizationRoleStringList != null) {
      org.setEdmEuropeanaRole(
          createLanguageMapOfStringList(Locale.ENGLISH.getLanguage(), organizationRoleStringList));
    }
    final List<String> organizationDomainStringList = (List<String>) zohoOrganizationFields
        .get(ZohoConstants.DOMAIN_FIELD);
    org.setEdmOrganizationDomain(createMap(Locale.ENGLISH.getLanguage(),
        CollectionUtils.isEmpty(organizationDomainStringList) ? null
            : organizationDomainStringList.get(0)));
    org.setEdmOrganizationSector(createMap(Locale.ENGLISH.getLanguage(),
        (String) zohoOrganizationFields.get(ZohoConstants.SECTOR_FIELD)));
    org.setEdmOrganizationScope(createMap(Locale.ENGLISH.getLanguage(),
        (String) zohoOrganizationFields.get(ZohoConstants.SCOPE_FIELD)));
    final List<String> geographicLevelList = (List<String>) zohoOrganizationFields
        .get(ZohoConstants.GEOGRAPHIC_LEVEL_FIELD);
    org.setEdmGeorgraphicLevel(createMap(Locale.ENGLISH.getLanguage(),
        CollectionUtils.isEmpty(geographicLevelList) ? null : geographicLevelList.get(0)));
    String organizationCountry = toEdmCountry(
        (String) zohoOrganizationFields.get(ZohoConstants.ORGANIZATION_COUNTRY_FIELD));
    org.setEdmCountry(createMap(Locale.ENGLISH.getLanguage(), organizationCountry));
    final List<String> sameAsList = getFieldArray(zohoOrganizationFields,
        ZohoConstants.SAME_AS_FIELD, MAX_SAME_AS);
    if (!CollectionUtils.isEmpty(sameAsList)) {
      org.setOwlSameAs(sameAsList.toArray(new String[]{}));
    }

    // address
    Address address = new AddressImpl();
    address.setAbout(org.getAbout() + "#address");
    address.setVcardStreetAddress((String) zohoOrganizationFields.get(ZohoConstants.STREET_FIELD));
    address.setVcardLocality((String) zohoOrganizationFields.get(ZohoConstants.CITY_FIELD));
    address.setVcardCountryName((String) zohoOrganizationFields.get(
        ZohoConstants.COUNTRY_FIELD)); //This is the Address Information Country as opposed to the Organization one above
    address.setVcardPostalCode((String) zohoOrganizationFields.get(ZohoConstants.ZIP_CODE_FIELD));
    address.setVcardPostOfficeBox((String) zohoOrganizationFields.get(ZohoConstants.PO_BOX_FIELD));
    org.setAddress(address);

    return org;
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
      idList.add(URL_ORGANIZATION_PREFFIX + deletedOrganization.getEntityId());
    }
    return idList;
  }
}
