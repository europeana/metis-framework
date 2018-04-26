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
import javax.xml.bind.JAXBException;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.api.external.ObjectIdSerializer;
import eu.europeana.enrichment.api.external.model.TextProperty;
import eu.europeana.enrichment.api.external.model.WebResource;

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
   * This method performs comparison of Map values for 
   * different possible types e.g. Map<String, List<String>>
   * or Map<String, String>
   * @param baseMap
   * @param addMap
   * @return return true if map values are the same
   */
  @SuppressWarnings("unchecked")
  public <T> boolean compareMapsByValues(
      Map<String, T> baseMap
      , Map<String, T> addMap) {

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
   * This method creates a map of values, which are different to the
   * base map for different possible types e.g. Map<String, List<String>>
   * or Map<String, String>
   * @param baseMap
   * @param addMap
   * @return map of differences to the base map
   */
  @SuppressWarnings("unchecked")
  public <T> Map<String, T> extractValuesNotIncludedInBaseMap(
      Map<String, T> baseMap
      , Map<String, T> addMap) {

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
    for(Entry<String, T> diffEntry : diffEntrySet) {
        resMap.put(diffEntry.getKey(), diffEntry.getValue());
    }
    
    return resMap;
  }
  
  /**
   * This method creates a list of values, which are different to the
   * base list compared to add list
   * @param baseList
   * @param addList
   * @return list of differences to the base map
   */
  public List<String> extractValuesNotIncludedInBaseList(
      List<String> baseList
      , List<String> addList) {
   
    if (baseList == null) {
      if (addList == null)
        /** if both lists are null - result is null */
        return null;
      else
        /** if base list is empty and add list not - result is add list */
        return addList;
    }
    
    /** remove all elements in add list from base list */
    addList.removeAll(baseList);
    
    return addList;
  }
  
  /**
   * This method compares two List<String>
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
   * This method extends base language map in format Map<String, List<String>> or Map<String, String> 
   * by additional map values if they are not duplicates
   * @param baseMap The original map
   * @param addMap
   * @return enriched base map
   */
  public <T> Map<String, T> mergeLanguageListMap(
      Map<String, T> baseMap
      , Map<String, T> addMap) {
    
    if (baseMap == null) {
      // if base map is empty and add map not - result is add map 
      return addMap;
    }
    
    // go through all keys of the add map and add not equal values to the base map 
    for (Map.Entry<String, T> entry : addMap.entrySet()) {
      String key = entry.getKey();
      if (!baseMap.keySet().contains(key)) {
        baseMap.put(key, entry.getValue());
      } 
    }       
    return baseMap;
  }
 
  /**
   * This method adds new data from Wikidata to Zoho prefLabel, assuming
   * that Zoho prefLabel should have only one value for one language.
   * If a value for the particular language already exists, prefLabel
   * from Wikidata will be added to altLabel of Zoho if it is not 
   * existing for particular language.
   * @param zohoMap The prefLabel in Zoho organization
   * @param wikidataMap The prefLabel in Wikidata organization
   * @param addToAltLabelMap The values that could not be added to Zoho prefLabel and should be added to Zoho altLabel
   * @return the new Zoho prefLabel value that should
   */
  public Map<String, List<String>> mergePrefLabel(
      Map<String, List<String>> zohoMap
      , Map<String, List<String>> wikidataMap, Map<String, List<String>> addToAltLabelMap) {
    
    if (zohoMap == null) {
      // if Zoho map is empty and Wikidata map not - result is the Wikidata map 
      return wikidataMap;
    }
    
    // go through all keys of the Wikidata map and add not equal values to the Zoho map 
    for (Map.Entry<String, List<String>> entry : wikidataMap.entrySet()) {
      String key = entry.getKey();
      if (!zohoMap.keySet().contains(key)) {
        zohoMap.put(key, entry.getValue());
      } else {
        addToAltLabelMap.put(key, entry.getValue());
      }
    }       
    return zohoMap;
  }
 
  /**
   * This method extends base list by elements of add list if it is not a duplicate
   * @param baseList
   * @param addList
   * @return enriched base list
   */
  public List<String> mergeStringLists(
      List<String> baseList
      , List<String> addList) {
    
    List<String> diffList = extractValuesNotIncludedInBaseList(baseList, addList);
  
    if (diffList != null && diffList.size() > 0) 
      baseList.addAll(diffList);
    
    return baseList;
  }      
 
  /**
   * This method concatenates two string arrays removing duplicates
   * @param base The base string array
   * @param add The additional string array
   * @return the resulting string array
   */
  public String[] concatenateStringArrays(String[] base, String[] add) {
    List<String> baseList = new ArrayList<String>(Arrays.asList(base));
    List<String> addList = new ArrayList<String>(Arrays.asList(add));
    baseList = mergeStringLists(baseList, addList); 
    return baseList.toArray(new String[baseList.size()]);
  }  
  
  /**
   * This method serializes an OrganizationImpl object to a string.
   * @param organization
   * @return wikidata organization
   * @throws JAXBException
   * @throws IOException
   */
  public String serialize(OrganizationImpl organization) throws JAXBException, IOException {    
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule sm = new SimpleModule("objId",
        Version.unknownVersion());
    sm.addSerializer(new ObjectIdSerializer());
    mapper.registerModule(sm);
    String res = mapper.writeValueAsString(organization);    
    return res;
  }
  
  
}
