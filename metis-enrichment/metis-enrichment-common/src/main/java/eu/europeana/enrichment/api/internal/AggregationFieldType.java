package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

public enum AggregationFieldType implements FieldType<Aggregation> {

  PROVIDER(aggregation -> Optional.ofNullable(aggregation.getProvider()).map(List::of)
      .orElse(Collections.emptyList()), EntityType.ORGANIZATION),

  DATA_PROVIDER(aggregation -> Optional.ofNullable(aggregation.getDataProvider()).map(List::of)
      .orElse(Collections.emptyList()), EntityType.ORGANIZATION),

  INTERMEDIATE_PROVIDER(
      aggregation -> Optional.ofNullable(aggregation.getIntermediateProviderList())
          .orElse(Collections.emptyList()), EntityType.ORGANIZATION);

  private final Function<Aggregation, List<? extends ResourceOrLiteralType>> valueProvider;
  private final EntityType entityType;

  AggregationFieldType(Function<Aggregation, List<? extends ResourceOrLiteralType>> valueProvider,
      EntityType entityType) {
    this.valueProvider = valueProvider;
    this.entityType = entityType;
  }

  @Override
  public EntityType getEntityType() {
    return entityType;
  }

  @Override
  public Stream<? extends ResourceOrLiteralType> extractFields(Aggregation aggregation) {
    return valueProvider.apply(aggregation).stream();
  }
}
