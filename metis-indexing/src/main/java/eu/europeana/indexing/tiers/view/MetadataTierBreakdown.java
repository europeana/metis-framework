package eu.europeana.indexing.tiers.view;

public class MetadataTierBreakdown {

  private LanguageBreakdown languageBreakdown;
  private EnablingElements enablingElements;
  private ContextualClasses contextualClasses;

  public MetadataTierBreakdown() {
  }

  public LanguageBreakdown getLanguageBreakdown() {
    return languageBreakdown;
  }

  public void setLanguageBreakdown(LanguageBreakdown languageBreakdown) {
    this.languageBreakdown = languageBreakdown;
  }

  public EnablingElements getEnablingElements() {
    return enablingElements;
  }

  public void setEnablingElements(EnablingElements enablingElements) {
    this.enablingElements = enablingElements;
  }

  public ContextualClasses getContextualClasses() {
    return contextualClasses;
  }

  public void setContextualClasses(ContextualClasses contextualClasses) {
    this.contextualClasses = contextualClasses;
  }
}
