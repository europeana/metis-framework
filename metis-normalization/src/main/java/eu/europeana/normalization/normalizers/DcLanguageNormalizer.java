package eu.europeana.normalization.normalizers;

import eu.europeana.normalization.languages.LanguageMatcher;
import eu.europeana.normalization.normalizers.ValueNormalizeActionWrapper.CopySettings;
import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.Namespace.Element;
import eu.europeana.normalization.util.XpathQuery;

/**
 * This normalizer normalizes dc:language references in the provider proxy. It doesn't overwrite the
 * values, but instead copies them to the Europeana proxy.
 */
public class DcLanguageNormalizer extends AbstractLanguageNormalizer {

  private static final Element DC_IDENTIFIER = Namespace.DC.getElement("identifier");
  private static final Element DC_LANGUAGE = Namespace.DC.getElement("language");
  private static final Element ORE_PROXY = Namespace.ORE.getElement("Proxy");
  private static final Element EDM_EUROPEANA_PROXY = Namespace.EDM.getElement("europeanaProxy");

  private static final XpathQuery PROVIDER_PROXY_LANGUAGES = new XpathQuery(
      "/%s/%s[not(%s='true')]/%s", XpathQuery.RDF_TAG, ORE_PROXY, EDM_EUROPEANA_PROXY, DC_LANGUAGE);
  private static final XpathQuery EUROPEANA_PROXY = new XpathQuery("/%s/%s[%s='true']",
      XpathQuery.RDF_TAG, ORE_PROXY, EDM_EUROPEANA_PROXY);

  /**
   * Constructor.
   *
   * @param languageMatcher A language matcher.
   * @param minimumConfidence The minimum confidence to apply to normalizations.
   */
  public DcLanguageNormalizer(LanguageMatcher languageMatcher, float minimumConfidence) {
    super(languageMatcher, minimumConfidence);
  }

  @Override
  public RecordNormalizeAction getAsRecordNormalizer() {
    final CopySettings copySettings = new CopySettings(EUROPEANA_PROXY, DC_LANGUAGE, DC_IDENTIFIER);
    return new ValueNormalizeActionWrapper(this, copySettings, PROVIDER_PROXY_LANGUAGES);
  }
}
