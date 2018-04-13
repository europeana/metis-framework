package eu.europeana.enrichment.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
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
   * This method compares two language maps of type Map<String, List<String>> 
   * @param zohoOrganization
   * @param wikidataOrganization
   * @return true if language maps are equal, false otherwise
   */
  public boolean isEqualLanguageListMap(     
      Map<String, List<String>> zohoOrganization
      , Map<String, List<String>> wikidataOrganization) {
    
    boolean res = false;
    
    for (Map.Entry<String, List<String>> entry : zohoOrganization.entrySet()) {
        String zohoLanguage = entry.getKey();
        List<String> zohoList = entry.getValue();
        List<String> wikidataList = wikidataOrganization.get(zohoLanguage);
        if (zohoList != null && wikidataList != null) {
          res = (zohoList.size() == wikidataList.size()) && zohoList.containsAll(wikidataList);
          if (res) {
            res = compareMapsByValues(zohoOrganization, wikidataOrganization);
          }
        } else {
          /** no such key */
          return false;
        }
    }
    return res;
  }
  
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
    
    for (String key : baseMap.keySet()) {
      T baseValue = baseMap.get(key);
      T addValue = addMap.get(key);
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
    for (String key : baseMap.keySet()) {
      T baseValue = baseMap.get(key);
      T addValue = addMap.get(key);
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
    for(Entry<String, T> entry : diffEntrySet) {
        resMap.put(entry.getKey(), entry.getValue());
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
   * This method extends base language map in format Map<String, List<String>> 
   * by additional map values if they are not duplicates
   * @param baseMap
   * @param addMap
   * @return enriched base map
   */
  @SuppressWarnings("unchecked")
  public <T> Map<String, T> mergeLanguageListMap(
      Map<String, T> baseMap
      , Map<String, T> addMap) {
    
    if (baseMap == null) {
      /** if base map is empty and add map not - result is add map */
      return addMap;
    }
    
    /** go through all keys of the add map and add not equal values to the base map */
    for (String key : addMap.keySet()) {
      T baseValue = baseMap.get(key);
      T addValue = addMap.get(key);
      if (baseValue == null && addValue == null)
          continue;
      else if (baseValue == null && addValue != null) {
        /** use value from add list for given key if it is not null and base value is null */
        baseMap.put(key, addValue);
        continue;
      }
      
      if (!addValue.getClass().equals(List.class)) {
        /** add all elements to add list from base list */
        List<String> addValueList = new ArrayList<String>((List<String>) addValue);
        List<String> baseValueList = new ArrayList<String>((List<String>) baseValue);
        addValueList.addAll(baseValueList);
        T addValuesWithoutDuplicates = (T) new ArrayList<String>(new HashSet<String>(addValueList));
        baseMap.put(key, addValuesWithoutDuplicates);
      }
      if (!baseValue.equals(addValue))
        baseMap.put(key, addValue);
    }   
    
    return baseMap;
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
  
  /**
   * This method reads organization data stored in a file
   * @param contentFile
   * @return organization in string format
   * @throws IOException
   */
  public String readFile(File contentFile) throws IOException {
    BufferedReader in = new BufferedReader(
        new InputStreamReader(new FileInputStream(contentFile), "UTF8"));
    String res = "";
    String line;
    while((line = in.readLine()) != null) {
      res = res + line;
    }
    in.close();    
    return res;
  }
  
}
