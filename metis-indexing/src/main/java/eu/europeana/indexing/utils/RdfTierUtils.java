package eu.europeana.indexing.utils;

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
import java.util.ArrayList;
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

  private static final Map<String, RdfTier> TIERS_BY_URI = Collections.unmodifiableMap(
      Stream.of(RdfTier.values()).collect(Collectors.toMap(RdfTier::getUri, Function.identity())));
  private static final Map<Tier, RdfTier> TIERS_BY_VALUE = Collections.unmodifiableMap(
      Stream.of(RdfTier.values()).collect(Collectors.toMap(RdfTier::getTier, Function.identity())));

  private RdfTierUtils() {
  }

  /**
   * Find the tier represented by the given quality annotation.
   *
   * @param annotation The corelib annotation.
   * @return The tier, or null if the annotation does not match any tier.
   */
  public static RdfTier getTier(
      eu.europeana.corelib.definitions.edm.entity.QualityAnnotation annotation) {
    return Optional.ofNullable(annotation)
                   .map(eu.europeana.corelib.definitions.edm.entity.QualityAnnotation::getBody)
                   .map(TIERS_BY_URI::get)
                   .orElse(null);
  }

  /**
   * Set the given tier value in the given {@link RDF} record. This will replace all existing The {@link MediaTier} or
   * {@link MetadataTier} tier values that apply to this record.
   *
   * @param rdf The record.
   * @param tier The {@link MediaTier} or {@link MetadataTier} tier value to add.
   * @throws IndexingException the indexing exception
   */
  public static void setTier(RDF rdf, Tier tier) throws IndexingException {
    setTierForTargetOverwrite(rdf, getTarget(rdf), tier);
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
    setTierForTargetInitialise(rdf, getTarget(rdf), tier);
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
    setTierForTargetOverwrite(rdf, getEuropeanaTarget(rdf), tier);
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
    setTierForTargetInitialise(rdf, getEuropeanaTarget(rdf), tier);
  }

  /**
   * Get the provider or aggregator aggregation that should be the target of the tier value.
   *
   * @param rdf The record.
   * @return The target.
   * @throws IndexingException Thrown if no suitable aggregation exists in the record.
   */
  @NotNull
  private static AggregatorOrProviderTarget getTarget(RDF rdf) throws IndexingException {
    final RdfWrapper rdfWrapper = new RdfWrapper(rdf);
    final Aggregation target = rdfWrapper.getAggregatorAggregations()
        .stream()
        .filter(Objects::nonNull)
        .findAny()
        .orElse(rdfWrapper.getProviderAggregations()
            .stream()
            .filter(Objects::nonNull)
            .findAny()
            .orElseThrow(() -> new RecordRelatedIndexingException(
                "Cannot find suitable aggregator or provider aggregation in record.")));
    return new AggregatorOrProviderTarget(target);
  }

  /**
   * Get the Europeana aggregation that should be the target of the tier value.
   *
   * @param rdf The record.
   * @return The target.
   * @throws IndexingException Thrown if no suitable aggregation exists in the record.
   */
  @NotNull
  private static EuropeanaTarget getEuropeanaTarget(RDF rdf) throws IndexingException {
    final EuropeanaAggregationType target = new RdfWrapper(rdf).getEuropeanaAggregation()
        .orElseThrow(() -> new RecordRelatedIndexingException(
            "Cannot find suitable aggregator or provider aggregation in record."));
    return new EuropeanaTarget(target);
  }

  /**
   * Sets tier for target initialise.
   *
   * @param rdf the rdf
   * @param target the target
   * @param tier the tier
   * @throws IndexingException the indexing exception
   */
  private static void setTierForTargetInitialise(RDF rdf, AbstractTierTarget<?> target, Tier tier)
      throws IndexingException {
    final Optional<QualityAnnotation> existingAnnotation = Optional.ofNullable(getExistingAnnotation(rdf, target.getAbout(), tier));
    if (existingAnnotation.isEmpty()) {
      addNewAnnotationToTarget(target, tier);
    }
  }

  /**
   * Sets tier for target overwrite.
   *
   * @param rdf the rdf
   * @param target the target
   * @param tier the tier
   * @throws IndexingException the indexing exception
   */
  private static void setTierForTargetOverwrite(RDF rdf, AbstractTierTarget<?> target, Tier tier)
      throws IndexingException {
    final Optional<QualityAnnotation> existingAnnotation = Optional.ofNullable(getExistingAnnotation(rdf, target.getAbout(), tier));
    final RdfTier rdfTier = getRdfTier(tier);
    if (existingAnnotation.isPresent()) {
      // We overwrite the value. We can do this because the annotation only has one target. If this
      // is no longer the case, we will need to split up the annotation.
      existingAnnotation.get().getHasBody().setResource(rdfTier.getUri());
    } else {
      addNewAnnotationToTarget(target, tier);
    }
  }

  /**
   * Add a new annotation to the target
   *
   * @param target the target
   * @param tier the tier
   * @throws IndexingException the indexing exception
   */
  private static void addNewAnnotationToTarget(AbstractTierTarget<?> target, Tier tier)
      throws IndexingException {
    final RdfTier rdfTier = getRdfTier(tier);
    final HasQualityAnnotation newAnnotation = createQualityAnnotation(target.getAbout(), rdfTier);
    final List<HasQualityAnnotation> existingAnnotations = new ArrayList<>(Optional
        .ofNullable(target.getHasQualityAnnotationList()).orElseGet(Collections::emptyList));
    existingAnnotations.add(newAnnotation);
    target.setHasQualityAnnotationList(existingAnnotations);
  }

  /**
   * Retrieves the quality annotation from the record that matches the provided target and tier
   * type. If no such annotation exists, this method returns null.
   *
   * @param rdf    The record to check.
   * @param target The target to match against.
   * @param tier   The tier (type) to match against.
   * @return The annotation matching the target and tier (type). Or null if no such annotation is
   * present in the record.
   * @throws RecordRelatedIndexingException In case there is some inconsistency in the record. This
   *                                        could be that there are multiple annotations matching
   *                                        the target and tier (type) or that one annotation has
   *                                        multiple target values.
   */
  private static QualityAnnotation getExistingAnnotation(RDF rdf, String target, Tier tier)
      throws RecordRelatedIndexingException {
    final RdfWrapper wrappedRecord = new RdfWrapper(rdf);
    final Stream<List<HasQualityAnnotation>> annotationsFromAggregations = wrappedRecord
        .getAggregations().stream().map(Aggregation::getHasQualityAnnotationList);
    final Stream<List<HasQualityAnnotation>> annotationsFromEuropeanaAggregation = wrappedRecord
        .getEuropeanaAggregation().stream().map(EuropeanaAggregationType::getHasQualityAnnotationList);
    final List<QualityAnnotation> resultCandidates = Stream
        .concat(annotationsFromAggregations, annotationsFromEuropeanaAggregation)
        .filter(Objects::nonNull).flatMap(List::stream).filter(Objects::nonNull)
        .map(HasQualityAnnotation::getQualityAnnotation).filter(Objects::nonNull)
        .filter(annotation -> annotationMatches(annotation, target, tier))
        .toList();
    if (resultCandidates.size() > 1) {
      throw new RecordRelatedIndexingException("Multiple annotations found for target '"
          + target + "' and type '" + tier.getClass() + "'.");
    } else if (!resultCandidates.isEmpty()
        && resultCandidates.getFirst().getHasTargetList().size() > 1) {
      throw new RecordRelatedIndexingException("Annotation found with multiple targets.");
    }
    return resultCandidates.isEmpty() ? null : resultCandidates.getFirst();
  }

  /**
   * Determines whether the annotation matches the target and tier type as provided.
   *
   * @param annotation The annotation to check.
   * @param target     The target to check against
   * @param tier       The tier (type) to check against.
   * @return The result of the test.
   */
  private static boolean annotationMatches(QualityAnnotation annotation, String target, Tier tier) {
    final boolean typeMatches = Optional.ofNullable(annotation.getHasBody())
        .map(ResourceType::getResource).orElse("").startsWith(RdfTier.getTierBaseUri(tier));
    return typeMatches && Optional.ofNullable(annotation.getHasTargetList())
        .stream().flatMap(List::stream).filter(Objects::nonNull).map(HasTarget::getResource)
        .filter(Objects::nonNull).anyMatch(target::equals);
  }

  @NotNull
  private static RdfTier getRdfTier(Tier tier) throws SetupRelatedIndexingException {
    // Get the right instance of RdfTier.
    final RdfTier rdfTier = TIERS_BY_VALUE.get(tier);
    if (rdfTier == null) {
      throw new SetupRelatedIndexingException("Cannot find settings for tier value "
          + tier.getClass());
    }
    return rdfTier;
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

  private abstract static class AbstractTierTarget<T extends AboutType> {

    private final T target;

    public AbstractTierTarget(T target) {
      this.target = target;
    }

    public T getTarget() {
      return target;
    }

    public String getAbout() {
      return target.getAbout();
    }

    public abstract List<HasQualityAnnotation> getHasQualityAnnotationList();

    public abstract void setHasQualityAnnotationList(
        List<HasQualityAnnotation> hasQualityAnnotationList);
  }

  private static class AggregatorOrProviderTarget extends AbstractTierTarget<Aggregation> {

    public AggregatorOrProviderTarget(Aggregation target) {
      super(target);
    }

    @Override
    public List<HasQualityAnnotation> getHasQualityAnnotationList() {
      return super.getTarget().getHasQualityAnnotationList();
    }

    @Override
    public void setHasQualityAnnotationList(List<HasQualityAnnotation> hasQualityAnnotationList) {
      super.getTarget().setHasQualityAnnotationList(hasQualityAnnotationList);
    }
  }

  private static class EuropeanaTarget extends AbstractTierTarget<EuropeanaAggregationType> {

    public EuropeanaTarget(EuropeanaAggregationType target) {
      super(target);
    }

    @Override
    public List<HasQualityAnnotation> getHasQualityAnnotationList() {
      return super.getTarget().getHasQualityAnnotationList();
    }

    @Override
    public void setHasQualityAnnotationList(List<HasQualityAnnotation> hasQualityAnnotationList) {
      super.getTarget().setHasQualityAnnotationList(hasQualityAnnotationList);
    }
  }
}
