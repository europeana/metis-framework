package eu.europeana.metis.transformation.service;

/**
 * Contains the mapping between a ProvidedCHO rdf:about and its sanitized version of Europeana Id and
 * the corresponding generated Europeana based rdf:about for elements to be used in the RDF.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-06-11
 */
public class EuropeanaGeneratedIdsMap {

  private static final String DATA_EUROPEANA_EU_HTTP_PREFIX = "http://data.europeana.eu";
  private static final String AGGREGATION_ABOUT_PREFIX = "/aggregation/provider";
  private static final String EUROPEANA_AGGREGATION_ABOUT_PREFIX = "/aggregation/europeana";
  private static final String PROXY_ABOUT_PREFIX = "/proxy/provider";
  private static final String EUROPEANA_PROXY_ABOUT_PREFIX = "/proxy/europeana";
  private static final String PROVIDEDCHO_ABOUT_PREFIX = "/item";

  private String sourceProvidedChoAbout;
  private String europeanaGeneratedId;
  private String aggregationAboutPrefixed;
  private String europeanaAggregationAboutPrefixed;
  private String proxyAboutPrefixed;
  private String europeanProxyAboutPrefixed;
  private String providedChoAboutPrefixed;

  /**
   * Constructor that created the map in one go.
   * The {@code europeanaGeneratedId} is supposed to be of structure
   * "/" + sanitizeDatasetIdLegacy(datasetId) + "/" + sanitizeRdfAboutLegacy(rdfAbout);
   *
   * @param sourceProvidedChoAbout the source record CHO id, should be the rdf:about from the ProvidedCHO
   * @param europeanaGeneratedId the Europeana id that was generated from the source record CHO id
   */
  public EuropeanaGeneratedIdsMap(String sourceProvidedChoAbout, String europeanaGeneratedId) {
    this.sourceProvidedChoAbout = sourceProvidedChoAbout;
    this.europeanaGeneratedId = europeanaGeneratedId;
    this.aggregationAboutPrefixed = DATA_EUROPEANA_EU_HTTP_PREFIX + AGGREGATION_ABOUT_PREFIX + europeanaGeneratedId;
    this.europeanaAggregationAboutPrefixed =
        DATA_EUROPEANA_EU_HTTP_PREFIX + EUROPEANA_AGGREGATION_ABOUT_PREFIX + europeanaGeneratedId;
    this.proxyAboutPrefixed = DATA_EUROPEANA_EU_HTTP_PREFIX + PROXY_ABOUT_PREFIX + europeanaGeneratedId;
    this.europeanProxyAboutPrefixed =
        DATA_EUROPEANA_EU_HTTP_PREFIX + EUROPEANA_PROXY_ABOUT_PREFIX + europeanaGeneratedId;
    this.providedChoAboutPrefixed = DATA_EUROPEANA_EU_HTTP_PREFIX + PROVIDEDCHO_ABOUT_PREFIX + europeanaGeneratedId;
  }

  public String getSourceProvidedChoAbout() {
    return sourceProvidedChoAbout;
  }

  public String getEuropeanaGeneratedId() {
    return europeanaGeneratedId;
  }

  public String getAggregationAboutPrefixed() {
    return aggregationAboutPrefixed;
  }

  public String getEuropeanaAggregationAboutPrefixed() {
    return europeanaAggregationAboutPrefixed;
  }

  public String getProxyAboutPrefixed() {
    return proxyAboutPrefixed;
  }

  public String getEuropeanProxyAboutPrefixed() {
    return europeanProxyAboutPrefixed;
  }

  public String getProvidedChoAboutPrefixed() {
    return providedChoAboutPrefixed;
  }

  public boolean isEuropeanaIdAndSourceRecordCHOIdEqual() {
    return sourceProvidedChoAbout.equals(europeanaGeneratedId);
  }
}
