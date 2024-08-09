package eu.europeana.indexing.utils;

import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.Tier;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.Created;
import eu.europeana.metis.schema.jibx.EuropeanaAggregationType;
import eu.europeana.metis.schema.jibx.HasBody;
import eu.europeana.metis.schema.jibx.HasQualityAnnotation;
import eu.europeana.metis.schema.jibx.HasTarget;
import eu.europeana.metis.schema.jibx.QualityAnnotation;
import eu.europeana.metis.schema.jibx.RDF;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.jetbrains.annotations.NotNull;

/**
 * This class provides utilities methods with respect to the tiers in {@link RdfTier}.
 */
public final class RdfTierUtils {

  private static final Map<String, RdfTier> tiersByUri = Collections.unmodifiableMap(
      Stream.of(RdfTier.values()).collect(Collectors.toMap(RdfTier::getUri, Function.identity())));
  private static final Map<Tier, RdfTier> tiersByValue = Collections.unmodifiableMap(
      Stream.of(RdfTier.values()).collect(Collectors.toMap(RdfTier::getTier, Function.identity())));

  private RdfTierUtils() {
  }

  /**
   * Find the tier represented by the given quality annotation.
   *
   * @param annotation The annotation.
   * @return The tier, or null if the annotation does not match any tier.
   */
  public static RdfTier getTier(
      eu.europeana.corelib.definitions.edm.entity.QualityAnnotation annotation) {
    return Optional.ofNullable(annotation)
                   .map(eu.europeana.corelib.definitions.edm.entity.QualityAnnotation::getBody)
                   .map(tiersByUri::get).orElse(null);
  }

  /**
   * Set the given tier value in the given {@link RDF} record. This will replace all existing content tier values that apply to
   * this record.
   *
   * @param rdf The record.
   * @param contentTier The content tier value to add.
   * @throws IndexingException In case no tier value could be added to the record.
   */
  public static void setTier(RDF rdf, MediaTier contentTier) throws IndexingException {
    setTierInternal(rdf, contentTier);
  }

  /**
   * Set the given tier value in the given {@link RDF} record. This will replace all existing metadata tier values that apply to
   * this record.
   *
   * @param rdf The record.
   * @param tier The metadata tier value to add.
   * @throws IndexingException In case no tier value could be added to the record.
   */
  public static void setTier(RDF rdf, Tier tier) throws IndexingException {
    setTierInternal(rdf, tier);
  }

  /**
   * Sets tier europeana.
   *
   * @param rdf the rdf
   * @param contentTier the content tier
   * @throws IndexingException the indexing exception
   */
  public static void setTierEuropeana(RDF rdf, MediaTier contentTier) throws IndexingException {
    setTierInternalEuropeana(rdf, contentTier);
  }

  /**
   * Sets tier europeana.
   *
   * @param rdf the rdf
   * @param tier the tier
   * @throws IndexingException the indexing exception
   */
  public static void setTierEuropeana(RDF rdf, Tier tier) throws IndexingException {
    setTierInternalEuropeana(rdf, tier);
  }

  private static void setTierInternal(RDF rdf, Tier tier)
      throws IndexingException {

    // Get the right instance of RdfTier.
    final RdfTier rdfTier = getRdfTier(tier);

    // Determine if there is something to reference and somewhere to add the reference.
    final RdfWrapper rdfWrapper = new RdfWrapper(rdf);
    final Aggregation aggregatorAggregation = rdfWrapper.getAggregatorAggregations()
                                                        .stream()
                                                        .filter(Objects::nonNull)
                                                        .findAny()
                                                        .orElse(rdfWrapper.getProviderAggregations()
                                                                          .stream()
                                                                          .filter(Objects::nonNull)
                                                                          .findAny()
                                                                          .orElse(null));

    checkAggregationNotNull(aggregatorAggregation);

    // Create the annotation
    final HasQualityAnnotation link = getQualityAnnotation(aggregatorAggregation.getAbout(), rdfTier);

    aggregatorAggregation.setHasQualityAnnotationList(
        Stream.concat(getExistingAnnotations(link, aggregatorAggregation.getHasQualityAnnotationList()), Stream.of(link)).toList());
  }

  private static void setTierInternalEuropeana(RDF rdf, Tier tier)
      throws IndexingException {

    final RdfTier rdfTier = getRdfTier(tier);

    // Determine if there is something to reference and somewhere to add the reference.
    final RdfWrapper rdfWrapper = new RdfWrapper(rdf);

    final EuropeanaAggregationType europeanaAggregationType = rdfWrapper.getEuropeanaAggregation()
                                                                        .stream()
                                                                        .filter(Objects::nonNull)
                                                                        .findAny()
                                                                        .orElse(null);

    checkAggregationNotNull(europeanaAggregationType);

    final HasQualityAnnotation link = getQualityAnnotation(europeanaAggregationType.getAbout(), rdfTier);

    europeanaAggregationType.setHasQualityAnnotationList(
        Stream.concat(getExistingAnnotations(link, europeanaAggregationType.getHasQualityAnnotationList()), Stream.of(link)).toList());
  }

  @NotNull
  private static RdfTier getRdfTier(Tier tier) throws SetupRelatedIndexingException {
    // Get the right instance of RdfTier.
    final RdfTier rdfTier = tiersByValue.get(tier);
    if (rdfTier == null) {
      throw new SetupRelatedIndexingException("Cannot find settings for tier value "
          + tier.getClass());
    }
    return rdfTier;
  }

  private static void checkAggregationNotNull(AboutType aggregatorAggregation) throws RecordRelatedIndexingException {
    if (aggregatorAggregation == null) {
      throw new RecordRelatedIndexingException("Cannot find suitable aggregator or provider aggregation in record.");
    }
  }

  private static Stream<HasQualityAnnotation> getExistingAnnotations(HasQualityAnnotation link,
      List<HasQualityAnnotation> qualityAnnotations) {
    return Optional.ofNullable(qualityAnnotations).stream()
                   .flatMap(Collection::stream)
                   .filter(existingLink -> !link.getQualityAnnotation()
                                                .getHasBody()
                                                .getResource().equals(existingLink.getQualityAnnotation()
                                                                                  .getHasBody()
                                                                                  .getResource()));
  }

  @NotNull
  private static HasQualityAnnotation getQualityAnnotation(String aggregatorAggregation, RdfTier rdfTier) {
    // Create the annotation
    final QualityAnnotation annotation = new QualityAnnotation();
    final Created created = new Created();
    created.setString(Instant.now().toString());
    annotation.setCreated(created);

    final HasTarget hasTarget = new HasTarget();
    hasTarget.setResource(aggregatorAggregation);
    annotation.setHasTargetList(List.of(hasTarget));

    final HasBody hasBody = new HasBody();
    hasBody.setResource(rdfTier.getUri());
    annotation.setHasBody(hasBody);

    // Add the link to the annotation to the europeana aggregation.
    final HasQualityAnnotation link = new HasQualityAnnotation();
    link.setQualityAnnotation(annotation);
    return link;
  }
}
