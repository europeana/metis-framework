package eu.europeana.enrichment.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.jibx.runtime.JiBXException;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;

/**
 * Utilities for enrichment and dereferencing Created by gmamakis on 8-3-17.
 */
public class EnrichmentUtils {

  private EnrichmentUtils() {}

  /**
   * Convert an RDF to a UTF-8 encoded XML.
   * 
   * @param rdf The RDF object to convert
   * @return An XML string representation of the RDF object
   * @throws JiBXException
   * @throws UnsupportedEncodingException
   * @deprecated Use {@link RdfConversionUtils#convertRdfToString(RDF)}.
   */
  @Deprecated
  public static String convertRDFtoString(RDF rdf)
      throws JiBXException, UnsupportedEncodingException {
    return RdfConversionUtils.convertRdfToString(rdf);
  }

  /**
   * Extract the fields to enrich from an RDF file
   * 
   * @param rdf The RDF to extract from.
   * @return List<InputValue> The extracted fields that need to be enriched.
   */
  public static List<InputValue> extractFieldsForEnrichmentFromRDF(RDF rdf) {
    ProxyType providerProxy = RdfProxyUtils.getProviderProxy(rdf);
    List<InputValue> valuesForEnrichment = new ArrayList<>();
    for (EnrichmentFields field : EnrichmentFields.values()) {
      List<InputValue> values = field.extractFieldValuesForEnrichment(providerProxy);
      valuesForEnrichment.addAll(values);
    }
    return valuesForEnrichment;
  }
}
