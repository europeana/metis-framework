package eu.europeana.indexing.tiers.view;

/**
 * The metadata tier breakdown
 */
public class MetadataTierBreakdown {

  private final LanguageBreakdown languageBreakdown;
  private final EnablingElementsBreakdown enablingElementsBreakdown;
  private final ContextualClassesBreakdown contextualClassesBreakdown;

  /**
   * Constructor with required parameters.
   *
   * @param languageBreakdown the language breakdown
   * @param enablingElementsBreakdown the enabling elements breakdown
   * @param contextualClassesBreakdown teh contextual classes breakdown
   */
  public MetadataTierBreakdown(LanguageBreakdown languageBreakdown,
      EnablingElementsBreakdown enablingElementsBreakdown,
      ContextualClassesBreakdown contextualClassesBreakdown) {
    this.languageBreakdown = languageBreakdown;
    this.enablingElementsBreakdown = enablingElementsBreakdown;
    this.contextualClassesBreakdown = contextualClassesBreakdown;
  }

  public LanguageBreakdown getLanguageBreakdown() {
    return languageBreakdown;
  }

  public EnablingElementsBreakdown getEnablingElements() {
    return enablingElementsBreakdown;
  }

  public ContextualClassesBreakdown getContextualClasses() {
    return contextualClassesBreakdown;
  }
}
