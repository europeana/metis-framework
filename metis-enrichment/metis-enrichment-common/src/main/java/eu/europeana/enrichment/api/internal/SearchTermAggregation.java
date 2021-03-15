package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This is an implementation of {@link SearchTerm} that provides context in the sense that it is
 * aware of the field type(s) in which the search term was found, related to Aggregation
 */
public class SearchTermAggregation extends AbstractSearchTerm {

  private final Set<AggregationFieldType> aggregationFieldTypes;

  public SearchTermAggregation(String textValue, String language, Set<AggregationFieldType> aggregationFieldTypes) {
    super(textValue, language);
    this.aggregationFieldTypes = Set.copyOf(aggregationFieldTypes);
  }

  @Override
  public Set<EntityType> getCandidateTypes() {
    return aggregationFieldTypes.stream().map(AggregationFieldType::getEntityType).collect(Collectors.toSet());
  }

  public Set<AggregationFieldType> getAggregationFieldTypes() {
    return aggregationFieldTypes;
  }
}
