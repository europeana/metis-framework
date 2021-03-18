package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

public enum AggregationFieldType {

  PROVIDER(aggregation -> Optional.ofNullable(aggregation.getProvider()).map(List::of)
      .orElse(Collections.emptyList()).stream().map(ResourceOrLiteralType.class::cast)
      .collect(Collectors.toList()), EntityType.ORGANIZATION),
  DATA_PROVIDER(
      aggregation -> Optional.ofNullable(aggregation.getDataProvider()).map(List::of)
          .orElse(Collections.emptyList()).stream().map(ResourceOrLiteralType.class::cast)
          .collect(Collectors.toList()), EntityType.ORGANIZATION),
  INTERMEDIATE_PROVIDER(
      aggregation -> Optional.ofNullable(aggregation.getIntermediateProviderList()).stream()
          .flatMap(Collection::stream).map(ResourceOrLiteralType.class::cast)
          .collect(Collectors.toList()), EntityType.ORGANIZATION);

  private final Function<Aggregation, List<ResourceOrLiteralType>> valueProvider;
  private final EntityType entityType;

  AggregationFieldType(Function<Aggregation, List<ResourceOrLiteralType>> valueProvider,
      EntityType entityType) {
    this.valueProvider = valueProvider;
    this.entityType = entityType;
  }

  /**
   * @return the entity type associated to this field - it is not null.
   */
  public EntityType getEntityType() {
    return entityType;
  }

  /**
   * Get the values for the specific field from aggregation.
   *
   * @param aggregation the aggregation to use
   * @return the list of values
   */
  public List<ResourceOrLiteralType> getResourceOrLiteral(Aggregation aggregation) {
    return valueProvider.apply(aggregation);
  }

  /**
   * Extract the field values set from aggregation.
   * <p>It gets the values for the specific field and creates a set of {@link FieldValue}s</p>
   *
   * @param aggregation the aggregation to use
   * @return the set of field values
   */
  public Set<FieldValue> extractFieldValuesForEnrichment(Aggregation aggregation) {
    return getResourceOrLiteral(aggregation).stream()
        .filter(content -> StringUtils.isNotEmpty(content.getString())).map(this::convert)
        .collect(Collectors.toSet());
  }

  private FieldValue convert(ResourceOrLiteralType content) {
    final String language = content.getLang() == null ? null : content.getLang().getLang();
    return new FieldValue(content.getString(), language);
  }
}
