package eu.europeana.indexing.tiers.model;

import eu.europeana.indexing.tiers.metadata.ContextualClassesClassifier;
import eu.europeana.indexing.tiers.metadata.EnablingElementsClassifier;
import eu.europeana.indexing.tiers.metadata.LanguageClassifier;
import eu.europeana.indexing.tiers.view.ContextualClasses;
import eu.europeana.indexing.tiers.view.EnablingElements;
import eu.europeana.indexing.tiers.view.LanguageBreakdown;
import eu.europeana.indexing.tiers.view.MetadataTierBreakdown;
import eu.europeana.indexing.utils.RdfWrapper;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * This tier classifier combines various classifiers. When asked to classify a record, it calls all the containing classifiers and
 * awards the record the minimum of the resulting tiers.
 *
 * @param <T> The type of the tier that this classifier returns.
 */
public class MetadataClassifier implements TierClassifier<MetadataTier, MetadataTierBreakdown> {

  private final LanguageClassifier languageClassifier;
  private final EnablingElementsClassifier enablingElementsClassifier;
  private final ContextualClassesClassifier contextualClassesClassifier;

  public MetadataClassifier(LanguageClassifier languageClassifier,
      EnablingElementsClassifier enablingElementsClassifier,
      ContextualClassesClassifier contextualClassesClassifier) {
    Objects.requireNonNull(languageClassifier, "Language classifier cannot be null");
    Objects.requireNonNull(enablingElementsClassifier, "Enabling elements classifier cannot be null");
    Objects.requireNonNull(contextualClassesClassifier, "Contextual classes classifier cannot be null");
    this.languageClassifier = languageClassifier;
    this.enablingElementsClassifier = enablingElementsClassifier;
    this.contextualClassesClassifier = contextualClassesClassifier;
  }

  @Override
  public TierClassification<MetadataTier, MetadataTierBreakdown> classify(RdfWrapper entity) {
    final TierClassification<MetadataTier, LanguageBreakdown> languageBreakdownTierClassification = languageClassifier.classify(
        entity);
    final TierClassification<MetadataTier, EnablingElements> enablingElementsTierClassification = enablingElementsClassifier.classify(
        entity);
    final TierClassification<MetadataTier, ContextualClasses> contextualClassesTierClassification = contextualClassesClassifier.classify(
        entity);

    final MetadataTierBreakdown metadataTierBreakdown = new MetadataTierBreakdown();
    metadataTierBreakdown.setLanguageBreakdown(languageBreakdownTierClassification.getClassification());
    metadataTierBreakdown.setEnablingElements(enablingElementsTierClassification.getClassification());
    metadataTierBreakdown.setContextualClasses(contextualClassesTierClassification.getClassification());

    MetadataTier metadataTier = Stream.of(languageBreakdownTierClassification.getTier(),
                                          enablingElementsTierClassification.getTier(), contextualClassesTierClassification.getTier()).reduce(Tier::min)
                                      .orElseThrow(IllegalStateException::new);

    return new TierClassification<>(metadataTier, metadataTierBreakdown);
  }
}

