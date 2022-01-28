package eu.europeana.indexing.tiers.model;

import static java.util.Objects.requireNonNull;

import eu.europeana.indexing.tiers.metadata.ContextualClassesClassifier;
import eu.europeana.indexing.tiers.metadata.EnablingElementsClassifier;
import eu.europeana.indexing.tiers.metadata.LanguageClassifier;
import eu.europeana.indexing.tiers.view.ContextualClassesBreakdown;
import eu.europeana.indexing.tiers.view.EnablingElementsBreakdown;
import eu.europeana.indexing.tiers.view.LanguageBreakdown;
import eu.europeana.indexing.tiers.view.MetadataTierBreakdown;
import eu.europeana.indexing.utils.RdfWrapper;
import java.util.stream.Stream;

/**
 * This tier classifier combines various classifiers. When asked to classify a record, it calls all the containing classifiers and
 * awards the record the minimum of the resulting tiers.
 */
public class MetadataClassifier implements TierClassifier<MetadataTier, MetadataTierBreakdown> {

  private final LanguageClassifier languageClassifier;
  private final EnablingElementsClassifier enablingElementsClassifier;
  private final ContextualClassesClassifier contextualClassesClassifier;

  /**
   * Constructor with required parameters.
   *
   * @param languageClassifier the language classifier
   * @param enablingElementsClassifier the enabling elements classifier
   * @param contextualClassesClassifier the contextual classes classifier
   */
  public MetadataClassifier(LanguageClassifier languageClassifier,
      EnablingElementsClassifier enablingElementsClassifier,
      ContextualClassesClassifier contextualClassesClassifier) {
    requireNonNull(languageClassifier, "Language classifier cannot be null");
    requireNonNull(enablingElementsClassifier, "Enabling elements classifier cannot be null");
    requireNonNull(contextualClassesClassifier, "Contextual classes classifier cannot be null");
    this.languageClassifier = languageClassifier;
    this.enablingElementsClassifier = enablingElementsClassifier;
    this.contextualClassesClassifier = contextualClassesClassifier;
  }

  @Override
  public TierClassification<MetadataTier, MetadataTierBreakdown> classify(RdfWrapper entity) {
    final LanguageBreakdown languageBreakdownTierClassification = languageClassifier.classifyBreakdown(
        entity);
    final EnablingElementsBreakdown enablingElementsTierClassification = enablingElementsClassifier.classifyBreakdown(
        entity);
    final ContextualClassesBreakdown contextualClassesTierClassification = contextualClassesClassifier.classifyBreakdown(
        entity);

    final MetadataTierBreakdown metadataTierBreakdown = new MetadataTierBreakdown(
        languageBreakdownTierClassification, enablingElementsTierClassification,
        contextualClassesTierClassification);

    MetadataTier metadataTier = Stream.of(languageBreakdownTierClassification.getMetadataTier(),
                                          enablingElementsTierClassification.getMetadataTier(), contextualClassesTierClassification.getMetadataTier())
                                      .reduce(Tier::min)
                                      .orElseThrow(IllegalStateException::new);

    return new TierClassification<>(metadataTier, metadataTierBreakdown);
  }
}

