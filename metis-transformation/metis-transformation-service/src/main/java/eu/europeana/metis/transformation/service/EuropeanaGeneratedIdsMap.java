package eu.europeana.metis.transformation.service;

/**
 * Contains the mapping between a ProvidedCHO rdf:about and its sanitized version of Europeana Id and
 * the corresponding generated Europeana based rdf:about for elements to be used in the RDF.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-06-11
 */
public class EuropeanaGeneratedIdsMap {

  private static final String AGGREGATION_ABOUT_PREFIX = "/aggregation/provider";
  private static final String EUROPEANA_AGGREGATION_ABOUT_PREFIX = "/aggregation/europeana";
  private static final String PROXY_ABOUT_PREFIX = "/proxy/provider";
  private static final String EUROPEANA_PROXY_ABOUT_PREFIX = "/proxy/europeana";

  private String sourceProvidedChoAbout;
  private String europeanaGeneratedId;
  private String aggregationAboutPrefixed;
  private String europeanaAggregationAboutPrefixed;
  private String proxyAboutPrefixed;
  private String europeanaProxyAboutPrefixed;

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
    this.aggregationAboutPrefixed = AGGREGATION_ABOUT_PREFIX + europeanaGeneratedId;
    this.europeanaAggregationAboutPrefixed =
        EUROPEANA_AGGREGATION_ABOUT_PREFIX + europeanaGeneratedId;
    this.proxyAboutPrefixed = PROXY_ABOUT_PREFIX + europeanaGeneratedId;
    this.europeanaProxyAboutPrefixed = EUROPEANA_PROXY_ABOUT_PREFIX + europeanaGeneratedId;
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

  public String getEuropeanaProxyAboutPrefixed() {
    return europeanaProxyAboutPrefixed;
  }

  public boolean isEuropeanaIdAndSourceRecordCHOIdEqual() {
    return sourceProvidedChoAbout.equals(europeanaGeneratedId);
  }
}
