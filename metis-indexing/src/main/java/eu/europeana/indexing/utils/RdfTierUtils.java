package eu.europeana.indexing.utils;

import eu.europeana.indexing.exception.IndexingException;
import eu.europeana.indexing.exception.RecordRelatedIndexingException;
import eu.europeana.indexing.exception.SetupRelatedIndexingException;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.indexing.tiers.model.MetadataTier;
import eu.europeana.indexing.tiers.model.Tier;
import eu.europeana.metis.schema.jibx.AggregatedCHO;
import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.Created;
import eu.europeana.metis.schema.jibx.EuropeanaAggregationType;
import eu.europeana.metis.schema.jibx.HasBody;
import eu.europeana.metis.schema.jibx.HasQualityAnnotation;
import eu.europeana.metis.schema.jibx.HasTarget;
import eu.europeana.metis.schema.jibx.QualityAnnotation;
import eu.europeana.metis.schema.jibx.RDF;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/**
 * This class provides utilities methods with respect to the tiers in {@link RdfTier}.
 */
public final class RdfTierUtils {

  private static final Map<String, RdfTier> tiersByUri = Collections.unmodifiableMap(
      Stream.of(RdfTier.values()).collect(Collectors.toMap(RdfTier::getUri, Function.identity())));
  private static final Map<Enum<? extends Tier>, RdfTier> tiersByValue = Collections.unmodifiableMap(
      Stream.of(RdfTier.values()).collect(Collectors.toMap(RdfTier::getTier, Function.identity())));
  private static final Map<RdfTier, SolrTier> tiersToSolrTiers = Collections.unmodifiableMap(
      Stream.of(SolrTier.values()).collect(Collectors.toMap(SolrTier::getTier, Function.identity())));

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
   * Find solr tier represented by the corresponding rdfTier
   *
   * @param rdfTier the rdf tier
   * @return the solr tier
   */
  public static SolrTier getSolrTier(RdfTier rdfTier) {
    return tiersToSolrTiers.get(rdfTier);
  }

  public static RdfTier getTierByURI(String uri) {
    return Arrays.stream(RdfTier.values()).filter(rdfTier -> rdfTier.getUri().equals(uri)).findFirst().orElse(null);
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
   * @param metadataTier The metadata tier value to add.
   * @throws IndexingException In case no tier value could be added to the record.
   */
  public static void setTier(RDF rdf, MetadataTier metadataTier) throws IndexingException {
    setTierInternal(rdf, metadataTier);
  }

  private static void setTierInternal(RDF rdf, Enum<? extends Tier> value)
      throws IndexingException {

    // Get the right instance of RdfTier.
    final RdfTier tier = tiersByValue.get(value);
    if (tier == null) {
      throw new SetupRelatedIndexingException("Cannot find settings for tier value "
          + value.getDeclaringClass().getName() + "." + value.name());
    }

    // Determine if there is something to reference and somewhere to add the reference.
    final RdfWrapper rdfWrapper = new RdfWrapper(rdf);
    final Set<String> aggregationAbouts = rdfWrapper.getAggregations().stream()
        .filter(Objects::nonNull).map(Aggregation::getAbout).filter(StringUtils::isNotBlank)
        .collect(Collectors.toSet());
    if (aggregationAbouts.isEmpty()) {
      throw new RecordRelatedIndexingException("Cannot find provider aggregation in record.");
    }
    final EuropeanaAggregationType europeanaAggregation = rdfWrapper.getEuropeanaAggregation()
        .orElseThrow(() -> new RecordRelatedIndexingException(
            "Cannot find Europeana aggregation in record."));
    final String choAbout = Optional.ofNullable(europeanaAggregation.getAggregatedCHO()).map(
        AggregatedCHO::getResource).orElseThrow(() -> new RecordRelatedIndexingException(
        "Cannot find aggregated CHO in Europeana aggregation."));
    final String annotationAboutBase = "/item" + choAbout;

    // Create the annotation
    final QualityAnnotation annotation = new QualityAnnotation();
    final Created created = new Created();
    created.setString(Instant.now().toString());
    annotation.setCreated(created);
    annotation.setHasTargetList(aggregationAbouts.stream().map(about -> {
      final HasTarget hasTarget = new HasTarget();
      hasTarget.setResource(about);
      return hasTarget;
    }).collect(Collectors.toList()));
    final HasBody hasBody = new HasBody();
    hasBody.setResource(tier.getUri());
    annotation.setHasBody(hasBody);
    annotation.setAbout(annotationAboutBase + tier.getAboutSuffix());

    // Add the annotation (remove all annotations with the same about)
    final Stream<QualityAnnotation> existingAnnotations = rdfWrapper.getQualityAnnotations()
        .stream()
        .filter(existingAnnotation -> !annotation.getAbout().equals(existingAnnotation.getAbout()));
    rdf.setQualityAnnotationList(
        Stream.concat(existingAnnotations, Stream.of(annotation)).collect(Collectors.toList()));

    // Add the link to the annotation to the europeana aggregation.
    final HasQualityAnnotation link = new HasQualityAnnotation();
    link.setResource(annotation.getAbout());
    final Stream<HasQualityAnnotation> existingLinks = Optional
        .ofNullable(europeanaAggregation.getHasQualityAnnotationList()).stream()
        .flatMap(Collection::stream)
        .filter(existingLink -> !link.getResource().equals(existingLink.getResource()));
    europeanaAggregation.setHasQualityAnnotationList(
        Stream.concat(existingLinks, Stream.of(link)).collect(Collectors.toList()));
  }
}
