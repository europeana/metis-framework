package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public enum AggregationFieldType implements FieldType {

  PROVIDER(aggregation -> Optional.ofNullable(aggregation.getProvider()).map(List::of)
      .orElse(Collections.emptyList()).stream().map(ResourceOrLiteralType.class::cast)
      .collect(Collectors.toList()), EntityType.ORGANIZATION),

  DATA_PROVIDER(aggregation -> Optional.ofNullable(aggregation.getDataProvider()).map(List::of)
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

  @Override
  public EntityType getEntityType() {
    return entityType;
  }


  @Override
  public Stream<? extends ResourceOrLiteralType> extractFields(AboutType aggregation) {
    return valueProvider.apply((Aggregation) aggregation).stream();
  }

  @Override
  public Set<FieldValue> extractFieldValuesForEnrichment(AboutType aggregation) {
    return extractFields(aggregation).filter(content -> StringUtils.isNotEmpty(content.getString()))
        .map(this::convert).collect(Collectors.toSet());
  }

  @Override
  public Set<String> extractFieldLinksForEnrichment(AboutType proxy) {
    return Collections.emptySet();
  }
}
