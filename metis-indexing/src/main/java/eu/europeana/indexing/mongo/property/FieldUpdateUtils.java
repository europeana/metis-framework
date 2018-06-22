package eu.europeana.indexing.mongo.property;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.query.UpdateOperations;

/**
 * Class with field update utility methods for field update functionality in this package.
 */
final class FieldUpdateUtils {

  private FieldUpdateUtils() {
    // Constructor must be private
  }

  private static boolean listEquals(List<String> listA, List<String> listB) {

    // Check for null
    if (listA == null || listB == null) {
      return listA == null && listB == null;
    }

    // Sort and compare
    final List<String> sortedListA = new ArrayList<>(listA);
    final List<String> sortedListB = new ArrayList<>(listB);
    Collections.sort(sortedListA);
    Collections.sort(sortedListB);
    return sortedListA.equals(sortedListB);
  }

  /**
   * Checks whether two maps contain exactly the same key value combinations. The value lists are
   * said to be the same if they contain the same elements, regardless of the order but counting
   * multiplicity.
   *
   * @param mapA The first map.
   * @param mapB The second map.
   * @return Whether the maps contain the same key value combinations.
   */
  static boolean mapEquals(Map<String, List<String>> mapA, Map<String, List<String>> mapB) {

    // Check for null and key set equality
    if (mapA == null || mapB == null || !mapA.keySet().equals(mapB.keySet())) {
      return mapA == null && mapB == null;
    }

    // So key set is equal. Compare values.
    return mapA.keySet().stream().allMatch(key -> listEquals(mapA.get(key), mapB.get(key)));
  }

  /**
   * Check if two arrays contain the same values, regardless of the order, but counting
   * multiplicity.
   *
   * @param arrA The first array.
   * @param arrB The second array.
   * @return Whether the arrays contain the same values.
   */
  static boolean arrayEquals(String[] arrA, String[] arrB) {
    if (arrA == null || arrB == null) {
      return arrA == null && arrB == null;
    }
    return listEquals(Arrays.asList(arrA), Arrays.asList(arrB));
  }

  static <T> void updateMap(UpdateTrigger updateTrigger, T saved, T updated, String updateField,
      UpdateOperations<? extends T> ops, Function<T, Map<String, List<String>>> getter,
      BiConsumer<T, Map<String, List<String>>> setter) {

    Map<String, List<String>> savedValues = getter.apply(saved);
    Map<String, List<String>> updatedValues = getter.apply(updated);

    if (updatedValues != null) {
      if (savedValues == null || !FieldUpdateUtils.mapEquals(updatedValues, savedValues)) {
        ops.set(updateField, updatedValues);
        setter.accept(saved, updatedValues);
        updateTrigger.triggerUpdate();
      }
    } else {
      if (saved != null) {
        ops.unset(updateField);
        setter.accept(saved, null);
        updateTrigger.triggerUpdate();
      }
    }
  }

  static <T> void updateArray(UpdateTrigger updateTrigger, T saved, T updated, String updateField,
      UpdateOperations<? extends T> ops, Function<T, String[]> getter,
      BiConsumer<T, String[]> setter) {
    String[] savedValues = getter.apply(saved);
    String[] updatedValues = getter.apply(updated);

    if (updatedValues != null) {
      if (savedValues == null || !FieldUpdateUtils.arrayEquals(updatedValues, savedValues)) {
        for (int i = 0; i < updatedValues.length; i++) {
          if (StringUtils.isNotBlank(updatedValues[i])) {
            updatedValues[i] = updatedValues[i].trim();
          }
        }
        ops.set(updateField, updatedValues);
        setter.accept(saved, updatedValues);
        updateTrigger.triggerUpdate();
      }
    } else {
      if (saved != null) {
        ops.unset(updateField);
        setter.accept(saved, null);
        updateTrigger.triggerUpdate();
      }
    }
  }

  static <T> void updateString(UpdateTrigger updateTrigger, T saved, T updated,
      String updateField, UpdateOperations<? extends T> ops, Function<T, String> getter,
      BiConsumer<T, String> setter) {
    String savedValues = getter.apply(saved);
    String updatedValues = getter.apply(updated);

    if (updatedValues != null) {
      if (savedValues == null || !StringUtils.equals(updatedValues, savedValues)) {
        ops.set(updateField, updatedValues.trim());
        setter.accept(saved, updatedValues.trim());
        updateTrigger.triggerUpdate();
      }
    } else {
      if (saved != null) {
        ops.unset(updateField);
        setter.accept(saved, null);
        updateTrigger.triggerUpdate();
      }
    }
  }
}
