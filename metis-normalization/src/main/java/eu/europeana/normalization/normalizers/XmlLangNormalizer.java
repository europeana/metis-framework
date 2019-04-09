package eu.europeana.normalization.normalizers;

import eu.europeana.normalization.languages.LanguageMatcher;
import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.Namespace.Element;
import eu.europeana.normalization.util.XpathQuery;
import java.util.function.Function;

/**
 * This normalizer normalizes xml:lang references in the provider proxy, provider aggregation and
 * contextual classes. It overwrites the values.
 */
public class XmlLangNormalizer extends AbstractLanguageNormalizer {

  private static final Element XML_LANG = Namespace.XML.getElement("lang");
  private static final Element ORE_PROXY = Namespace.ORE.getElement("Proxy");
  private static final Element EDM_EUROPEANA_PROXY = Namespace.EDM.getElement("europeanaProxy");
  private static final Element ORE_AGGREGATION = Namespace.ORE.getElement("Aggregation");
  private static final Element EDM_AGENT = Namespace.EDM.getElement("Agent");
  private static final Element SKOS_CONCEPT = Namespace.SKOS.getElement("Concept");
  private static final Element EDM_PLACE = Namespace.EDM.getElement("Place");
  private static final Element EDM_TIMESPAN = Namespace.EDM.getElement("TimeSpan");

  private static final XpathQuery PROVIDER_PROXY_LANGUAGES = new XpathQuery(
      "/%s/%s[not(%s='true')]//@%s", XpathQuery.RDF_TAG, ORE_PROXY, EDM_EUROPEANA_PROXY, XML_LANG);
  private static final Function<Element, XpathQuery> XPATH_QUERY_CREATOR = element -> new XpathQuery(
      "/%s/%s//@%s", XpathQuery.RDF_TAG, element, XML_LANG);
  private static final XpathQuery PROVIDER_AGGREGATION_LANGUAGES = XPATH_QUERY_CREATOR
      .apply(ORE_AGGREGATION);
  private static final XpathQuery AGENT_LANGUAGES = XPATH_QUERY_CREATOR.apply(EDM_AGENT);
  private static final XpathQuery CONCEPT_LANGUAGES = XPATH_QUERY_CREATOR.apply(SKOS_CONCEPT);
  private static final XpathQuery PLACE_LANGUAGES = XPATH_QUERY_CREATOR.apply(EDM_PLACE);
  private static final XpathQuery TIMESPAN_LANGUAGES = XPATH_QUERY_CREATOR.apply(EDM_TIMESPAN);

  /**
   * Constructor.
   *
   * @param languageMatcher A language matcher.
   * @param minimumConfidence The minimum confidence to apply to normalizations.
   */
  public XmlLangNormalizer(LanguageMatcher languageMatcher, float minimumConfidence) {
    super(languageMatcher, minimumConfidence);
  }

  @Override
  public RecordNormalizeAction getAsRecordNormalizer() {
    return new ValueNormalizeActionWrapper(this, PROVIDER_PROXY_LANGUAGES,
        PROVIDER_AGGREGATION_LANGUAGES, AGENT_LANGUAGES, CONCEPT_LANGUAGES, PLACE_LANGUAGES,
        TIMESPAN_LANGUAGES);
  }
}
