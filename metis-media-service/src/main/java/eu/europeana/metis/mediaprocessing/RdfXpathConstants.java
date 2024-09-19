package eu.europeana.metis.mediaprocessing;

/**
 * Rdf xpath string constants.
 */
public final class RdfXpathConstants {

  public static final String RDF_NAMESPACE = "/rdf:RDF";
  public static final String ORE_AGGREGATION = RDF_NAMESPACE + "/ore:Aggregation";
  public static final String EDM_OBJECT =  ORE_AGGREGATION + "/edm:object/@rdf:resource";
  public static final String EDM_IS_SHOWN_BY = ORE_AGGREGATION + "/edm:isShownBy/@rdf:resource";
  public static final String EDM_HAS_VIEW = ORE_AGGREGATION + "/edm:hasView/@rdf:resource";
  public static final String EDM_IS_SHOWN_AT = ORE_AGGREGATION + "/edm:isShownAt/@rdf:resource";
  public static final String SVCS_SERVICE = RDF_NAMESPACE + "/svcs:Service";
  public static final String EDM_WEBRESOURCE = RDF_NAMESPACE + "/edm:WebResource";

  private RdfXpathConstants() {}

}
