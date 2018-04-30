package eu.europeana.enrichment.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import javax.xml.bind.JAXBException;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import eu.europeana.corelib.definitions.edm.entity.Address;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.api.external.ObjectIdSerializer;
import eu.europeana.enrichment.api.external.model.TextProperty;
import eu.europeana.enrichment.api.external.model.WebResource;
import net.sf.saxon.type.StringConverter.StringToAnyURI;

/**
 * This class supports conversion of different organization objects into OrganizationImpl object.
 * 
 * @author GrafR
 *
 */
public class EntityConverterUtils {

  private static final String UNDEFINED_LANGUAGE_KEY = "def";

  /**
   * Create OrganizationImpl map from value
   * 
   * @param key The field name
   * @param value The value
   * @return map of strings and lists
   */
  public Map<String, List<String>> createMapOfStringList(String key, String value) {
    if (value == null)
      return null;
    List<String> valueList = createList(value);
    return createMapOfStringList(key, valueList);
  }

  /**
   * This method converts string value to Map<String,String> values for given key - language.
   * 
   * @param key The language
   * @param value The input string value
   * @return the Map<String,List<String>>
   */
  public Map<String, String> createMap(String key, String value) {
    if (value == null)
      return null;
    Map<String, String> resMap = new HashMap<String, String>();
    resMap.put(key, value);
    return resMap;
  }

  /**
   * This method creates language map in format Map<String, String> from text property object.
   * 
   * @param textProperty The text property object
   * @return The language map
   */
  public Map<String, String> createMapFromTextProperty(TextProperty textProperty) {
    if (textProperty == null)
      return null;

    String key = textProperty.getKey();
    String value = textProperty.getValue();

    return createMap(key, value);
  }

  /**
   * @param value
   * @return
   */
  List<String> createList(String value) {
    if (value == null)
      return null;

    return Collections.singletonList(value);
  }

  /**
   * Create OrganizationImpl map from list of values
   * 
   * @param key The field name
   * @param value The list of values
   * @return map of strings and lists
   */
  public Map<String, List<String>> createMapOfStringList(String key, List<String> value) {
    Map<String, List<String>> resMap = new HashMap<String, List<String>>();
    resMap.put(key, value);
    return resMap;
  }

  /**
   * @param languages
   * @param values
   * @return
   */
  public Map<String, List<String>> createLanguageMapOfStringList(List<String> languages,
      List<String> values) {
    if (languages == null)
      return null;

    Map<String, List<String>> resMap = new HashMap<String, List<String>>();
    for (int i = 0; i < languages.size(); i++) {
      resMap.put(toIsoLanguage(languages.get(i)), createList(values.get(i)));
    }
    return resMap;
  }

  /**
   * This method creates language map from text property object list.
   * 
   * @param textPropertyList The list of text property objects
   * @return The language map
   */
  public Map<String, List<String>> createLanguageMapFromTextPropertyList(
      List<? extends TextProperty> textPropertyList) {
    if (textPropertyList == null)
      return null;

    Map<String, List<String>> resMap = new HashMap<String, List<String>>();
    for (int i = 0; i < textPropertyList.size(); i++) {
      TextProperty textProperty = textPropertyList.get(i);
      resMap.put(toIsoLanguage(textProperty.getKey()), createList(textProperty.getValue()));
    }
    return resMap;
  }

  /**
   * This method creates language map from text property object list.
   * 
   * @param textPropertyList The list of text property objects
   * @return The language map
   */
  public Map<String, String> createStringStringMapFromTextPropertyList(
      List<? extends TextProperty> textPropertyList) {
    if (textPropertyList == null)
      return null;

    Map<String, String> resMap = new HashMap<String, String>();
    for (int i = 0; i < textPropertyList.size(); i++) {
      TextProperty textProperty = textPropertyList.get(i);
      resMap.put(toIsoLanguage(textProperty.getKey()), textProperty.getValue());
    }
    return resMap;
  }

  /**
   * This method converts a list of Part objects to a string array.
   * 
   * @param resources The list of Part objects
   * @return string array
   */
  public String[] createStringArrayFromPartList(List<? extends WebResource> resources) {
    if (resources == null)
      return null;

    List<String> res = new ArrayList<String>();
    for (int i = 0; i < resources.size(); i++) {
      WebResource part = resources.get(i);
      res.add(part.getResourceUri());
    }
    return res.toArray(new String[] {});
  }

  /**
   * @param language
   * @param value
   * @return
   */
  public Map<String, List<String>> createLanguageMapOfStringList(String language, String value) {

    if (value == null)
      return null;

    Map<String, List<String>> resMap = new HashMap<String, List<String>>();
    resMap.put(toIsoLanguage(language), createList(value));
    return resMap;
  }

  /**
   * @param language
   * @param value
   * @return
   */
  public Map<String, List<String>> createLanguageMapOfStringList(String language,
      List<String> value) {

    if (value == null)
      return null;

    Map<String, List<String>> resMap = new HashMap<String, List<String>>();
    resMap.put(toIsoLanguage(language), value);
    return resMap;
  }

  /**
   * @param language
   * @return
   */
  String toIsoLanguage(String language) {
    if (StringUtils.isBlank(language))
      return UNDEFINED_LANGUAGE_KEY;

    return language.substring(0, 2).toLowerCase();
  }

  /**********************
   * Merging methods
   **********************/

  /**
   * This method performs comparison of Map values for different possible types e.g. Map<String,
   * List<String>> or Map<String, String>
   * 
   * @param baseMap
   * @param addMap
   * @return return true if map values are the same
   */
  @SuppressWarnings("unchecked")
  public <T> boolean compareMapsByValues(Map<String, T> baseMap, Map<String, T> addMap) {

    if (baseMap == null || addMap == null || baseMap.size() != addMap.size())
      return false;

    for (Map.Entry<String, T> entry : baseMap.entrySet()) {
      T baseValue = entry.getValue();
      T addValue = addMap.get(entry.getKey());
      if (baseValue == null && addValue == null)
        continue;
      else if (baseValue == null || addValue == null)
        return false;
      if (!baseValue.getClass().equals(List.class))
        return compareLists((List<String>) baseValue, (List<String>) addValue);
      if (!baseValue.equals(addValue))
        return false;
    }
    return false;
  }

  /**
   * This method creates a map of values, which are different to the base map for different possible
   * types e.g. Map<String, List<String>> or Map<String, String>
   * 
   * @param baseMap
   * @param addMap
   * @return map of differences to the base map
   */
  @SuppressWarnings("unchecked")
  public <T> Map<String, T> extractValuesNotIncludedInBaseMap(Map<String, T> baseMap,
      Map<String, T> addMap) {

    Map<String, T> resMap = new HashMap<String, T>();

    if (baseMap == null) {
      if (addMap == null)
        /** if both maps are null - result is empty map */
        return resMap;
      else
        /** if base map is empty and add map not - result is add map */
        return addMap;
    }

    /** go through all keys of the base map and extract not equal values from the add map */
    for (Map.Entry<String, T> entry : baseMap.entrySet()) {
      String key = entry.getKey();
      T baseValue = entry.getValue();
      T addValue = addMap.get(entry.getKey());
      if (baseValue == null && addValue == null)
        continue;
      else if (baseValue == null && addValue != null) {
        /** use value from add list for given key if it is not null and base value is null */
        resMap.put(key, addValue);
        continue;
      }
      if (!baseValue.getClass().equals(List.class)) {
        /** remove all elements in base list from add list */
        ((List<String>) addValue).removeAll((List<String>) baseValue);
        resMap.put(key, addValue);
      }
      if (!baseValue.equals(addValue))
        resMap.put(key, addValue);
    }

    /** find keys existing in add map that a not existing in the base map */
    Set<Entry<String, T>> diffEntrySet = new HashSet<Entry<String, T>>(addMap.entrySet());
    diffEntrySet.removeAll(baseMap.entrySet());

    /** add extracted keys with their values to the result map */
    for (Entry<String, T> diffEntry : diffEntrySet) {
      resMap.put(diffEntry.getKey(), diffEntry.getValue());
    }

    return resMap;
  }

 
  /**
   * This method compares two List<String>
   * 
   * @param baseList
   * @param addList
   * @return true if lists are equal
   */
  public boolean compareLists(List<String> baseList, List<String> addList) {
    Collections.sort(baseList);
    Collections.sort(addList);
    return baseList.equals(addList);
  }

  /**
   * This method extends base language map in format Map<String, List<String>> or Map<String,
   * String> by additional map values if they are not duplicates
   * 
   * @param langMap The original map to which the new values will be added
   * @param newValuesMap the map containing new values to be added to the map
   * @return enriched base map
   */
  public Map<String, List<String>> mergeLanguageMap(Map<String, List<String>> langMap, Map<String, List<String>> newValuesMap) {
    if (langMap == null && newValuesMap == null){ 
      return null; //nothing to do
    }
    
    Map<String, List<String>> ret = new TreeMap<String, List<String>>();
    if (langMap == null) {
      //init baseMap if needed
      if(newValuesMap != null){
        ret.putAll(newValuesMap);
      }
    }else{
      ret.putAll(langMap);
      // go through all keys of the add map and add not equal values to the base map
      //only if new values are available
      if(newValuesMap != null){
        List<String> mergedList;
        for (Map.Entry<String, List<String>> entry : newValuesMap.entrySet()) {
          if (!langMap.containsKey(entry.getKey())) {
            //add new languages
            ret.put(entry.getKey(), entry.getValue());
          }else{
            //merge values for existing languages
            mergedList = mergeStringLists(langMap.get(entry.getKey()), entry.getValue());
            ret.put(entry.getKey(), mergedList);
          }
        }
      }  
    }
    
    if(ret.isEmpty())
      return null;
    
    return ret;
  }

  /**
   * This method adds new data from Wikidata to Zoho prefLabel, assuming that Zoho prefLabel should
   * have only one value for one language. If a value for the particular language already exists,
   * prefLabel from Wikidata will be added to altLabel of Zoho if it is not existing for each
   * particular language.
   * 
   * @param zohoMap The prefLabel in Zoho organization
   * @param wikidataMap The prefLabel in Wikidata organization
   * @param addToAltLabelMap The values that could not be added to Zoho prefLabel and should be
   *        added to Zoho altLabel
   * @return the new Zoho prefLabel value that should
   */
  public void mergePrefLabel(Map<String, List<String>> zohoMap,
      Map<String, List<String>> wikidataMap, Map<String, List<String>> addToAltLabelMap) {

    // go through all keys of the Wikidata map and add not equal values to the Zoho map
    for (Map.Entry<String, List<String>> entry : wikidataMap.entrySet()) {
      String key = entry.getKey();
      if (!zohoMap.containsKey(key)) {
        // currently the prefLabel is list of strings, but the data in Zoho and wikidata has always
        // only one value
        zohoMap.put(key, new ArrayList<String>(entry.getValue()));
      } else {
        //do not duplicate existing pref labels
        List<String> unknownPrefLabels = new ArrayList<>(entry.getValue());
        unknownPrefLabels.removeAll(zohoMap.get(key));
        //add values to alt labels map 
        addMissingValuesToMap(key, entry.getValue(), addToAltLabelMap);
      }
    }
  }

  /**
   * This method is adding to the language map the missing values from the provided list, but
   * avoiding dupplicated entries.
   * 
   * @param key the language key used in the map
   * @param values the data to be added to the map
   * @param languageMap the language map to which the provided values will be added
   */
  private void addMissingValuesToMap(String key, List<String> values,
      Map<String, List<String>> languageMap) {
    if (!languageMap.containsKey(key)) {
      // add all labels to map
      languageMap.put(key, new ArrayList<String>(values));
    } else {
      // add values from the list to the existing values in the map
      List<String> mergedList = mergeStringLists(languageMap.get(key), values);
      languageMap.remove(key);
      languageMap.put(key, mergedList);
    }
  }

  /**
   * This method extends base list by elements of add list if it is not a duplicate
   * 
   * @param values
   * @param newValues
   * @return the list containing the sum of the two provided lists
   */
  @SuppressWarnings("unchecked")
  public List<String> mergeStringLists(List<String> values, List<String> newValues) {
    if(newValues == null){
      return null;//nothing to do
    }
    if(values == null){
      //add all values from the newValues
      return new ArrayList<String>(newValues);
    }else{
      return ListUtils.sum(values, newValues);
    } 
  }

  /**
   * This method merges the two string arrays, but without removing duplicates
   * 
   * @param base The base string array
   * @param add The additional string array
   * @return the resulting string array
   */
  public String[] mergeStringArrays(String[] base, String[] add) {
    if(base == null && add == null)
      return null;
    
    List<String> mergedList = mergeStringLists(Arrays.asList(base), Arrays.asList(add));
    return mergedList.toArray(new String[mergedList.size()]);
  }

  /**
   * This method serializes an OrganizationImpl object to a string.
   * 
   * @param organization
   * @return wikidata organization
   * @throws JAXBException
   * @throws IOException
   */
  public String serialize(OrganizationImpl organization) throws JAXBException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule sm = new SimpleModule("objId", Version.unknownVersion());
    sm.addSerializer(new ObjectIdSerializer());
    mapper.registerModule(sm);
    String res = mapper.writeValueAsString(organization);
    return res;
  }

  public void mergeAddress(Organization zohoOrganization, Organization wikidataOrganization) {
    //if any Address field coming from Zoho is filled (with the exception of vcard:country) then discard the whole Address data from Wikidata
    if(hasAddress(zohoOrganization) || wikidataOrganization.getAddress() == null){
      return;//nothing to do
    }else{
      Address address = zohoOrganization.getAddress();
      //do not copy about and country as they are mandatory in Zoho
      //still avoid overwriting the data if the hasAddress implementation fails 
      if(StringUtils.isEmpty(address.getVcardLocality())){
        address.setVcardLocality(wikidataOrganization.getAddress().getVcardLocality());
      }
      if(StringUtils.isEmpty(address.getVcardPostalCode())){
        address.setVcardPostalCode(wikidataOrganization.getAddress().getVcardPostalCode());
      }
      if(StringUtils.isEmpty(address.getVcardPostOfficeBox())){
        address.setVcardPostOfficeBox(wikidataOrganization.getAddress().getVcardPostOfficeBox());
      }
      if(StringUtils.isEmpty(address.getVcardStreetAddress())){
        address.setVcardStreetAddress(wikidataOrganization.getAddress().getVcardStreetAddress());
      }
    }
  }

  private boolean hasAddress(Organization organization) {
    Address address = organization.getAddress();
    boolean ret = StringUtils.isNotEmpty(address.getVcardLocality()) 
        || StringUtils.isNotEmpty(address.getVcardPostalCode()) 
        || StringUtils.isNotEmpty(address.getVcardPostOfficeBox()) 
        || StringUtils.isNotEmpty(address.getVcardStreetAddress());
    return ret;
  }


}
