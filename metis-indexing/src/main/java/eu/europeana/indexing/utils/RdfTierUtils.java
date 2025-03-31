package eu.europeana.indexing.utils;

import static eu.europeana.indexing.utils.RdfTier.CONTENT_TIER_BASE_URI;
import static eu.europeana.indexing.utils.RdfTier.METADATA_TIER_BASE_URI;

import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
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
import eu.europeana.metis.schema.jibx.ResourceType;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
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
   * Set the given tier value in the given {@link RDF} record. This will replace all existing The {@link MediaTier} or
   * {@link MetadataTier} tier values that apply to this record.
   *
   * @param rdf The record.
   * @param tier The {@link MediaTier} or {@link MetadataTier} tier value to add.
   * @throws IndexingException In case no tier value could be added to the record.
   */
  public static void setTier(RDF rdf, Tier tier) throws IndexingException {
    setTierInternal(rdf, tier);
  }

  /**
   * Set the given tier value in the given {@link RDF} record. This will replace all existing The {@link MediaTier} or
   * {@link MetadataTier} tier values that apply to this record.
   *
   * @param rdf the rdf
   * @param tier the tier
   * @throws IndexingException the indexing exception
   */
  public static void setTierIfAbsent(RDF rdf, Tier tier) throws IndexingException {
    if (!RdfTierUtils.hasTierCalculationByTarget(rdf, tier.getClass())) {
      setTierInternal(rdf, tier);
    }
  }

  /**
   * Sets tier europeana value in the given {@link RDF} record. This will replace all existing The {@link MediaTier} or
   * {@link MetadataTier} tier values that apply to this record.
   *
   * @param rdf the rdf
   * @param tier the {@link MediaTier} or {@link MetadataTier} tier
   * @throws IndexingException the indexing exception
   */
  public static void setTierEuropeana(RDF rdf, Tier tier) throws IndexingException {
    setTierInternalEuropeana(rdf, tier);
  }

  /**
   * Sets tier europeana value in the given {@link RDF} record. Only if absent This will replace all existing The
   * {@link MediaTier} or {@link MetadataTier} tier values that apply to this record.
   *
   * @param rdf the rdf
   * @param tier the tier
   * @throws IndexingException the indexing exception
   */
  public static void setTierEuropeanaIfAbsent(RDF rdf, Tier tier) throws IndexingException {
    if (!RdfTierUtils.hasTierEuropeanaCalculationByTarget(rdf, tier.getClass())) {
      setTierInternalEuropeana(rdf, tier);
    }
  }

  /**
   * Check if Europeana Aggregation already has a tier calculation.
   *
   * @param rdf the rdf
   * @param tier the tier
   * @return the boolean
   */
  public static boolean hasTierEuropeanaCalculation(RDF rdf, Class<? extends Tier> tier) {
    List<String> tierEuropeanaData = extractTierData(rdf.getEuropeanaAggregationList(),
        EuropeanaAggregationType::getHasQualityAnnotationList);

    return containsTierCalculation(tier, tierEuropeanaData);
  }

  /**
   * Check if Aggregation already has a tier calculation.
   *
   * @param rdf the rdf
   * @param tier the tier
   * @return the boolean
   */
  public static boolean hasTierCalculation(RDF rdf, Class<? extends Tier> tier) {
    List<String> tierData = extractTierData(rdf.getAggregationList(), Aggregation::getHasQualityAnnotationList);

    return containsTierCalculation(tier, tierData);
  }

  /**
   * Extract tier data from a list of aggregations.
   *
   * @param <T> the type parameter
   * @param aggregationList the aggregation list
   * @param qualityAnnotationSupplier the quality annotation supplier
   * @return the extracted tier data
   */
  public static <T extends AboutType> List<String> extractTierData(List<T> aggregationList,
      Function<T, List<HasQualityAnnotation>> qualityAnnotationSupplier) {
    return aggregationList
        .stream()
        .map(aboutType ->
            Optional.ofNullable(qualityAnnotationSupplier.apply(aboutType))
                    .map(annotations -> annotations
                        .stream()
                        .map(HasQualityAnnotation::getQualityAnnotation)
                        .filter(Objects::nonNull)
                        .map(QualityAnnotation::getHasBody)
                        .filter(Objects::nonNull)
                        .map(ResourceType::getResource)
                        .filter(Objects::nonNull)
                        .toList())
                    .orElse(null))
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .toList();
  }


  /**
   * Extract tier data by target .
   *
   * @param <T> the type parameter
   * @param aggregationList the aggregation list
   * @param qualityAnnotationSupplier the quality annotation supplier
   * @param target the target
   * @return the list
   */
  public static <T extends AboutType> List<String> extractTierDataByTarget(List<T> aggregationList,
      Function<T, List<HasQualityAnnotation>> qualityAnnotationSupplier,
      String target) {
    return aggregationList
        .stream()
        .filter(aggregation -> aggregation.getAbout().equals(target))
        .map(aboutType ->
            Optional.ofNullable(qualityAnnotationSupplier.apply(aboutType))
                    .map(mapperQualityAnnotationBodyBy(target))
                    .orElse(null))
        .filter(Objects::nonNull)
        .flatMap(List::stream)
        .toList();
  }

  @NotNull
  private static Function<List<HasQualityAnnotation>, List<String>> mapperQualityAnnotationBodyBy(String target) {
    return annotations -> annotations
        .stream()
        .map(HasQualityAnnotation::getQualityAnnotation)
        .filter(Objects::nonNull)
        .map(mapperQualityAnnotationByTarget(target))
        .filter(Objects::nonNull)
        .map(QualityAnnotation::getHasBody)
        .filter(Objects::nonNull)
        .map(ResourceType::getResource)
        .filter(Objects::nonNull)
        .toList();
  }

  @NotNull
  private static Function<QualityAnnotation, QualityAnnotation> mapperQualityAnnotationByTarget(
      String target) {
    return qualityAnnotation -> qualityAnnotation
        .getHasTargetList()
        .stream()
        .filter(Objects::nonNull)
        .map(HasTarget::getResource)
        .anyMatch(t -> t.equals(target)) ? qualityAnnotation : null;
  }

  /**
   * Has tier calculation by target boolean.
   *
   * @param rdf the rdf
   * @param tier the tier
   * @return the boolean
   */
  public static boolean hasTierCalculationByTarget(RDF rdf, Class<? extends Tier> tier) {
    List<String> tierData = extractTierDataByTarget(rdf.getAggregationList(),
        Aggregation::getHasQualityAnnotationList,
        rdf.getAggregationList()
           .stream()
           .map(Aggregation::getAbout)
           .findFirst()
           .orElse(null)
    );
    return containsTierCalculation(tier, tierData);
  }

  /**
   * Has tier europeana calculation by target boolean.
   *
   * @param rdf the rdf
   * @param tier the tier
   * @return the boolean
   */
  public static boolean hasTierEuropeanaCalculationByTarget(RDF rdf, Class<? extends Tier> tier) {
    List<String> tierEuropeanaData = extractTierData(rdf.getEuropeanaAggregationList(),
        EuropeanaAggregationType::getHasQualityAnnotationList);
    extractTierDataByTarget(rdf.getEuropeanaAggregationList(),
        EuropeanaAggregationType::getHasQualityAnnotationList,
        rdf.getEuropeanaAggregationList()
           .stream()
           .map(EuropeanaAggregationType::getAbout)
           .findFirst()
           .orElse(null)
    );

    return containsTierCalculation(tier, tierEuropeanaData);
  }

  private static boolean containsTierCalculation(Class<? extends Tier> tier, List<String> tierCalculation) {
    if (tier.isInstance(MediaTier.class)) {
      return tierCalculation.stream().filter(Objects::nonNull).anyMatch(t -> t.startsWith(CONTENT_TIER_BASE_URI));
    } else if (tier.isInstance(MetadataTier.class)) {
      return tierCalculation.stream().filter(Objects::nonNull).anyMatch(t -> t.startsWith(METADATA_TIER_BASE_URI));
    } else {
      return false;
    }
  }

  private static String getTierBaseUri(Class<? extends Tier> tier) {
    if (tier.isInstance(MediaTier.class)) {
      return CONTENT_TIER_BASE_URI;
    } else if (tier.isInstance(MetadataTier.class)) {
      return METADATA_TIER_BASE_URI;
    } else {
      return "";
    }
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
    final HasQualityAnnotation newAnnotation = createQualityAnnotation(aggregatorAggregation.getAbout(), rdfTier);

    aggregatorAggregation.setHasQualityAnnotationList(
        Stream.concat(getExistingAnnotationsFromTargetWithoutNewAnnotation(newAnnotation,
                  aggregatorAggregation.getHasQualityAnnotationList(),
                  aggregatorAggregation.getAbout()), Stream.of(newAnnotation))
              .toList());
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

    final HasQualityAnnotation newAnnotation = createQualityAnnotation(europeanaAggregationType.getAbout(), rdfTier);

    europeanaAggregationType.setHasQualityAnnotationList(
        Stream.concat(getExistingAnnotationsFromTargetWithoutNewAnnotation(newAnnotation,
                  europeanaAggregationType.getHasQualityAnnotationList(),
                  europeanaAggregationType.getAbout()), Stream.of(newAnnotation))
              .toList());
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

  private static Stream<HasQualityAnnotation> getExistingAnnotationsFromTargetWithoutNewAnnotation(
      HasQualityAnnotation newAnnotation,
      List<HasQualityAnnotation> qualityAnnotations, String target) {

    return Optional.ofNullable(qualityAnnotations)
                   .stream()
                   .flatMap(Collection::stream)
                   .filter(hasQualityAnnotationByTarget(newAnnotation, target))
                   .filter(hasNotQualityAnnotationByTier(newAnnotation));
  }

  @NotNull
  private static Predicate<HasQualityAnnotation> hasQualityAnnotationByTarget(HasQualityAnnotation newAnnotation,
      String target) {
    return existingLink -> newAnnotation.getQualityAnnotation()
                                        .getHasTargetList()
                                        .stream()
                                        .anyMatch(hasTarget ->
                                            hasTarget.getResource()
                                                     .equals(target));
  }

  @NotNull
  private static Predicate<HasQualityAnnotation> hasNotQualityAnnotationByTier(HasQualityAnnotation newAnnotation) {
    return existingLink ->
        !getTierBaseUri(RdfTier.fromUri(newAnnotation.getQualityAnnotation()
                                                     .getHasBody().
                                                     getResource())
                               .getTier().getClass())
            .equals(getTierBaseUri(RdfTier.fromUri(existingLink.getQualityAnnotation()
                                                               .getHasBody()
                                                               .getResource())
                                          .getTier().getClass()));
  }

  @NotNull
  private static HasQualityAnnotation createQualityAnnotation(String aggregatorAggregation, RdfTier rdfTier) {
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
