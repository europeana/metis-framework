package eu.europeana.indexing.fullbean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang.StringUtils;
import eu.europeana.corelib.definitions.jibx.LiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.ResourceType;

/**
 * Class with utility methods for converting an instance of
 * {@link eu.europeana.corelib.definitions.jibx.RDF} to an instance of
 * {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
final class FieldInputUtils {

  private FieldInputUtils() {
    // This class should not be instantiated.
  }

  /**
   * Method that converts a LiteralType.class object to a multilingual map of strings
   *
   * @param obj The LiteralType object
   * @return A Map of strings. The keys are the languages and the values are lists of strings for
   *         the corresponding language. If the object is null, the method returns null. In case a
   *         language is missing the def notation is used as key
   */
  static <T extends LiteralType> Map<String, List<String>> createLiteralMapFromString(T obj) {
    Map<String, List<String>> retMap = new HashMap<>();
    if (obj != null) {
      if (obj.getLang() != null && StringUtils.isNotBlank(obj.getLang().getLang())) {
        List<String> val = new ArrayList<>();
        if (StringUtils.isNotBlank(obj.getString())) {
          val.add(obj.getString());
          retMap.put(obj.getLang().getLang(), val);
        }
      } else {
        List<String> val = new ArrayList<>();
        if (StringUtils.isNotBlank(obj.getString())) {
          val.add(obj.getString());
          retMap.put("def", val);
        }
      }
      return retMap.isEmpty() ? null : retMap;
    }

    return null;
  }

  /**
   * Method that converts a Enum object to a multilingual map of strings
   *
   * @param obj
   * @return A Map of strings containing the value with the def notation as key
   */
  static Map<String, List<String>> createLiteralMapFromString(String obj) {
    Map<String, List<String>> retMap = new HashMap<>();

    if (obj != null) {
      List<String> val = new ArrayList<>();
      if (StringUtils.isNotBlank(obj)) {
        val.add(obj);
        retMap.put("def", val);
      }
      return retMap.isEmpty() ? null : retMap;
    }

    return null;
  }

  /**
   * Method that converts a ResourceOrLiteralType.class object to a multilingual map of strings
   *
   * @param obj The ResourceOrLiteralType object
   * @return A Map of strings. The keys are the languages and the values are lists of strings for
   *         the corresponding language. If the object is null, the method returns null. In case a
   *         language is missing the def notation is used as key
   */
  static <T extends ResourceOrLiteralType> Map<String, List<String>> createResourceOrLiteralMapFromString(
      T obj) {
    Map<String, List<String>> retMap = new HashMap<>();
    if (obj != null) {
      if (obj.getLang() != null && StringUtils.isNotEmpty(obj.getLang().getLang())) {
        if (obj.getString() != null && StringUtils.trimToNull(obj.getString()) != null) {
          List<String> val = new ArrayList<>();
          val.add(StringUtils.trim(obj.getString()));
          retMap.put(obj.getLang().getLang(), val);
        }
        if (obj.getResource() != null) {
          List<String> val =
              retMap.get(obj.getLang().getLang()) != null ? retMap.get(obj.getLang().getLang())
                  : new ArrayList<>();

          val.add(obj.getResource().getResource());

          retMap.put(obj.getLang().getLang(), val);
        }
      } else {
        if (StringUtils.isNotBlank(StringUtils.trimToNull(obj.getString()))) {
          List<String> val = retMap.get("def") != null ? retMap.get("def") : new ArrayList<>();
          val.add(obj.getString());
          retMap.put("def", val);
        }
        if (obj.getResource() != null
            && StringUtils.isNotBlank(StringUtils.trimToNull(obj.getResource().getResource()))) {
          List<String> val = retMap.get("def") != null ? retMap.get("def") : new ArrayList<>();
          val.add(StringUtils.trim(obj.getResource().getResource()));
          retMap.put("def", val);
        }
      }
      return retMap;
    }

    return null;
  }

  static <T extends ResourceType> Map<String, List<String>> createResourceMapFromString(T obj) {
    Map<String, List<String>> retMap = new HashMap<>();
    if (obj != null) {

      if (StringUtils.isNotBlank(StringUtils.trimToNull(obj.getResource()))) {
        List<String> val = retMap.get("def") != null ? retMap.get("def") : new ArrayList<>();
        val.add(obj.getResource());
        retMap.put("def", val);
      }
      return retMap;

    }
    return null;
  }

  /**
   * Method that converts a LiteralType.class list to a multilingual map of strings
   *
   * @param list The LiteralType list
   * @return A Map of strings. The keys are the languages and the values are lists of strings for
   *         the corresponding language. If the object is null, the method returns null. In case a
   *         language is missing the def notation is used as key
   */
  static <T extends LiteralType> Map<String, List<String>> createLiteralMapFromList(List<T> list) {
    if (list != null && !list.isEmpty()) {
      Map<String, List<String>> retMap = new HashMap<>();
      for (T obj : list) {
        if (obj.getLang() != null && StringUtils.isNotBlank(obj.getLang().getLang())) {
          String lang = obj.getLang().getLang();
          List<String> val = retMap.get(lang);
          if (val == null) {
            val = new ArrayList<>();
          }
          val.add(StringUtils.trim(obj.getString()));
          retMap.put(lang, val);
        } else {
          List<String> val = retMap.get("def");
          if (val == null) {
            val = new ArrayList<>();
          }
          if (StringUtils.isNotBlank(StringUtils.trimToNull(obj.getString()))) {
            val.add(obj.getString());
            retMap.put("def", val);
          }
        }
      }
      return retMap.isEmpty() ? null : retMap;
    }
    return null;
  }

  static <T extends ResourceType> Map<String, List<String>> createResourceMapFromList(
      List<T> list) {
    if (list != null && !list.isEmpty()) {
      Map<String, List<String>> retMap = new HashMap<>();
      for (T obj : list) {

        List<String> val = retMap.get("def");
        if (val == null) {
          val = new ArrayList<>();
        }
        if (StringUtils.isNotBlank(StringUtils.trimToNull(obj.getResource()))) {
          val.add(obj.getResource());
          retMap.put("def", val);
        }

      }
      return retMap.isEmpty() ? null : retMap;
    }
    return null;
  }

  /**
   * Method that converts a ResourceOrLiteralType.class list to a multilingual map of strings
   *
   * @param list The ResourceOrLiteralType list
   * @return A Map of strings. The keys are the languages and the values are lists of strings for
   *         the corresponding language. If the object is null, the method returns null. In case a
   *         language is missing the def notation is used as key
   */
  static <T extends ResourceOrLiteralType> Map<String, List<String>> createResourceOrLiteralMapFromList(
      List<T> list) {
    if (list != null && !list.isEmpty()) {
      Map<String, List<String>> retMap = new HashMap<>();
      for (T obj : list) {
        if (obj.getString() != null && StringUtils.isNotBlank(obj.getString())) {
          if (obj.getLang() != null && StringUtils.isNotBlank(obj.getLang().getLang())) {
            List<String> val = retMap.get((obj.getLang().getLang()));
            if (val == null) {
              val = new ArrayList<>();

            }
            if (StringUtils.isNotBlank(StringUtils.trimToNull(obj.getString()))) {
              val.add(obj.getString());
              retMap.put(obj.getLang().getLang(), val);
            }

          } else {
            List<String> val = retMap.get("def");
            if (val == null) {
              val = new ArrayList<>();
            }
            if (StringUtils.isNotBlank(StringUtils.trimToNull(obj.getString()))) {
              val.add(obj.getString());
              retMap.put("def", val);
            }
          }
        }
        if (obj.getResource() != null && StringUtils.isNotBlank(obj.getResource().getResource())) {
          if (obj.getLang() != null) {
            String lang = obj.getLang().getLang();
            List<String> val;
            if (retMap.containsKey(lang)) {
              val = retMap.get(lang);

            } else {
              val = new ArrayList<>();
            }
            if (StringUtils.isNotBlank(StringUtils.trimToNull(obj.getResource().getResource()))) {
              val.add(obj.getResource().getResource());
              retMap.put(lang, val);
            }
          } else {
            List<String> val = retMap.get("def");
            if (val == null) {
              val = new ArrayList<>();
            }
            if (StringUtils.isNotBlank(StringUtils.trimToNull(obj.getResource().getResource()))) {
              val.add(obj.getResource().getResource());
              retMap.put("def", val);
            }
          }
        }
      }
      if (!retMap.isEmpty()) {
        return retMap;
      }
    }
    return null;
  }

  /**
   * Returns an array of strings based on values from a ResourceOrLiteralType list. Since it is
   * perfectly valid to have both an rdf:resource and a value on a field This method will return
   * both in a String array
   *
   * @param list The ResourceOrLiteralType list
   * @return An array of strings with the values of the list
   */
  public static String[] resourceOrLiteralListToArray(List<? extends ResourceOrLiteralType> list) {
    if (list != null) {
      List<String> lst = new ArrayList<>();

      for (ResourceOrLiteralType obj : list) {
        if (obj.getResource() != null) {
          lst.add(obj.getResource().getResource());
        }
        if (obj.getString() != null && StringUtils.isNotEmpty(obj.getString())) {
          lst.add(obj.getString());
        }
      }
      return lst.stream().toArray(String[]::new);
    }
    return new String[] {};
  }

  /**
   * Returns an array of strings based on values from a ResourceType list.
   *
   * @param list The ResourceType list
   * @return An array of strings with the values of the list
   */
  static String[] resourceListToArray(List<? extends ResourceType> list) {
    if (list != null) {
      String[] arr = new String[list.size()];
      int i = 0;
      for (ResourceType obj : list) {
        arr[i] = obj.getResource();
        i++;
      }
      return arr;
    }
    return new String[] {};
  }

  /**
   *
   * @param obj The ResourceType object
   * @return a string from a ResourceType object
   */
  static String getResourceString(ResourceType obj) {
    if (obj != null) {
      return obj.getResource() != null ? obj.getResource() : null;
    }
    return null;
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
