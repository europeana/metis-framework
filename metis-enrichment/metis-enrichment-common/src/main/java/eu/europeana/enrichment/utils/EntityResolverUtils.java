package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.entity.client.utils.EntityApiConstants;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * EntityClientResolver util class
 *
 * @author Srishti.singh@europeana.eu
 */
public final class EntityResolverUtils {

  private EntityResolverUtils() {
  }

  /**
   * Returns the first Entity from the List<EnrichmentBase> for each input value
   *
   * @param results
   * @param <T>
   * @return
   */
  public static <T> Map<T, EnrichmentBase> pickFirstFromTheList(Map<T, List<EnrichmentBase>> results) {
    Map<T, EnrichmentBase> finalValues = new HashMap<>();
    results.forEach((key, value) -> finalValues.put(key, value.stream().findFirst().orElse(null)));
    return finalValues;
  }

  public static <I> boolean isSearchTermOrReferenceThatIsNotAEuropeanaEntity(I batchItem) {
    return batchItem instanceof SearchTerm || !((ReferenceTerm) batchItem).getReference().toString()
                                                                          .startsWith(EntityApiConstants.BASE_URL);
  }
}
