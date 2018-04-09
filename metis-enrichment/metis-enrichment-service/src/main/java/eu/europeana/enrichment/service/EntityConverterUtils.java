package eu.europeana.enrichment.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
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

}
