package eu.europeana.indexing.fullbean;

import eu.europeana.corelib.definitions.jibx.LiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import eu.europeana.corelib.definitions.jibx.RightsStatements;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang.StringUtils;

/**
 * Class with utility methods for converting an instance of {@link eu.europeana.corelib.definitions.jibx.RDF}
 * to an instance of {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
final class FieldInputUtils {

  private static final String DEFAULT_LANG_KEY = "def";

  private static final Function<LiteralType, String> LITERAL_TYPE_KEY_GETTER = literal -> Optional
      .ofNullable(literal.getLang()).map(LiteralType.Lang::getLang).orElse(null);
  private static final Function<ResourceOrLiteralType, String> RESOURCE_OR_LITERAL_TYPE_KEY_GETTER = object -> Optional
      .ofNullable(object.getLang()).map(ResourceOrLiteralType.Lang::getLang).orElse(null);

  private static final Function<String, List<String>> STRING_TYPE_VALUES_GETTER = Collections::singletonList;
  private static final Function<LiteralType, List<String>> LITERAL_TYPE_VALUES_GETTER = literal -> Collections
      .singletonList(literal.getString());
  private static final Function<ResourceType, List<String>> RESOURCE_TYPE_VALUES_GETTER = resource -> Collections
      .singletonList(resource.getResource());
  private static final Function<RightsStatements, List<String>> RIGHTS_STATEMENTS_VALUES_GETTER = resource -> Collections
      .singletonList(resource.getResource());
  private static final Function<ResourceOrLiteralType, List<String>> RESOURCE_OR_LITERAL_TYPE_VALUES_GETTER = object -> {
    final String objectString = object.getString();
    final String resourceString = Optional.ofNullable(object.getResource())
        .map(Resource::getResource).orElse(null);
    return Arrays.asList(objectString, resourceString);
  };

  private FieldInputUtils() {
    // This class should not be instantiated.
  }

  private static <T> Function<T, String> singleKeyGetter() {
    return v -> null;
  }

  private static <T> Map<String, List<String>> createMapFromList(List<? extends T> list,
      Function<T, String> keyGetter, Function<T, List<String>> valuesGetter) {

    // Sanity check
    if (list == null || list.isEmpty()) {
      return null;
    }

    // Go by all objects in input list
    final Map<String, List<String>> retMap = new HashMap<>();
    for (T listItem : list) {

      // Obtain the trimmed non-null non-empty values to add.
      final Stream<String> values = Optional.ofNullable(listItem).map(valuesGetter)
          .filter(valueList -> !valueList.isEmpty()).map(List::stream).orElseGet(Stream::empty);
      final List<String> filteredValues = values.filter(Objects::nonNull).map(String::trim)
          .filter(StringUtils::isNotEmpty).collect(Collectors.toList());

      // If there are values to add, we add them to the map.
      if (!filteredValues.isEmpty()) {
        final String key = Optional.ofNullable(listItem).map(keyGetter)
            .filter(StringUtils::isNotBlank).orElse(DEFAULT_LANG_KEY);
        retMap.computeIfAbsent(key, k -> new ArrayList<>()).addAll(filteredValues);
      }
    }

    // Return result.
    return retMap.isEmpty() ? null : retMap;
  }

  private static <T> String[] createArrayFromList(List<? extends T> list,
      Function<T, List<String>> valuesGetter) {
    final Map<String, List<String>> map = createMapFromList(list, singleKeyGetter(), valuesGetter);
    final Stream<String> result = Optional.ofNullable(map).map(Map::values).map(Collection::stream)
        .flatMap(Stream::findAny).map(List::stream).orElseGet(Stream::empty);
    return result.toArray(String[]::new);
  }

  /**
   * Method that converts a Enum object to a multilingual map of strings
   *
   * @return A Map of strings containing the value with the def notation as key
   */
  static Map<String, List<String>> createMapFromString(String obj) {
    return createMapFromList(Collections.singletonList(obj), singleKeyGetter(),
        STRING_TYPE_VALUES_GETTER);
  }

  static <T extends ResourceType> Map<String, List<String>> createResourceMapFromString(T obj) {
    return createResourceMapFromList(Collections.singletonList(obj));
  }

  static <T extends RightsStatements> Map<String, List<String>> createRightsStatementsMapFromString(
      T obj) {
    return createRightsStatementsMapFromList(Collections.singletonList(obj));
  }

  static <T extends ResourceType> Map<String, List<String>> createResourceMapFromList(
      List<T> list) {
    return createMapFromList(list, singleKeyGetter(), RESOURCE_TYPE_VALUES_GETTER);
  }

  static <T extends RightsStatements> Map<String, List<String>> createRightsStatementsMapFromList(
      List<T> list) {
    return createMapFromList(list, singleKeyGetter(), RIGHTS_STATEMENTS_VALUES_GETTER);
  }

  /**
   * Method that converts a LiteralType.class object to a multilingual map of strings
   *
   * @param obj The LiteralType object
   * @return A Map of strings. The keys are the languages and the values are lists of strings for
   * the corresponding language. If the object is null, the method returns null. In case a language
   * is missing the def notation is used as key
   */
  static <T extends LiteralType> Map<String, List<String>> createLiteralMapFromString(T obj) {
    return createLiteralMapFromList(Collections.singletonList(obj));
  }

  /**
   * Method that converts a LiteralType.class list to a multilingual map of strings
   *
   * @param list The LiteralType list
   * @return A Map of strings. The keys are the languages and the values are lists of strings for
   * the corresponding language. If the object is null, the method returns null. In case a language
   * is missing the def notation is used as key
   */
  static <T extends LiteralType> Map<String, List<String>> createLiteralMapFromList(List<T> list) {
    return createMapFromList(list, LITERAL_TYPE_KEY_GETTER, LITERAL_TYPE_VALUES_GETTER);
  }

  /**
   * Method that converts a ResourceOrLiteralType.class object to a multilingual map of strings
   *
   * @param obj The ResourceOrLiteralType object
   * @return A Map of strings. The keys are the languages and the values are lists of strings for
   * the corresponding language. If the object is null, the method returns null. In case a language
   * is missing the def notation is used as key
   */
  static <T extends ResourceOrLiteralType> Map<String, List<String>> createResourceOrLiteralMapFromString(
      T obj) {
    return createResourceOrLiteralMapFromList(Collections.singletonList(obj));
  }

  /**
   * Method that converts a ResourceOrLiteralType.class list to a multilingual map of strings
   *
   * @param list The ResourceOrLiteralType list
   * @return A Map of strings. The keys are the languages and the values are lists of strings for
   * the corresponding language. If the object is null, the method returns null. In case a language
   * is missing the def notation is used as key
   */
  static <T extends ResourceOrLiteralType> Map<String, List<String>> createResourceOrLiteralMapFromList(
      List<T> list) {
    return createMapFromList(list, RESOURCE_OR_LITERAL_TYPE_KEY_GETTER,
        RESOURCE_OR_LITERAL_TYPE_VALUES_GETTER);
  }

  /**
   * Returns an array of strings based on values from a ResourceOrLiteralType list. Since it is
   * perfectly valid to have both an rdf:resource and a value on a field This method will return
   * both in a String array
   *
   * @param list The ResourceOrLiteralType list
   * @return An array of strings with the values of the list
   */
  static String[] resourceOrLiteralListToArray(List<? extends ResourceOrLiteralType> list) {
    return createArrayFromList(list, RESOURCE_OR_LITERAL_TYPE_VALUES_GETTER);
  }

  /**
   * Returns an array of strings based on values from a ResourceType list.
   *
   * @param list The ResourceType list
   * @return An array of strings with the values of the list
   */
  static String[] resourceListToArray(List<? extends ResourceType> list) {
    return createArrayFromList(list, RESOURCE_TYPE_VALUES_GETTER);
  }

  /**
   * @param obj The ResourceType object
   * @return a string from a ResourceType object
   */
  static String getResourceString(ResourceType obj) {
    final String[] array = resourceListToArray(Collections.singletonList(obj));
    return array.length > 0 ? array[0] : null;
  }

  static Map<String, List<String>> mergeMaps(Map<String, List<String>> map1,
      Map<String, List<String>> map2) {

    // In case one (or both) of them is null
    if (map1 == null || map2 == null) {
      return map1 == null ? map2 : map1;
    }

    // So neither are null. We merge them.
    final Map<String, List<String>> result = new HashMap<>(map1);
    for (Entry<String, List<String>> entry : map2.entrySet()) {
      result.merge(entry.getKey(), entry.getValue(), (list1, list2) -> {
        List<String> resultList = new ArrayList<>(list1);
        resultList.addAll(list2);
        return resultList;
      });
    }

    // Done
    return result;
  }
}
