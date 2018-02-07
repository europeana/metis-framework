package eu.europeana.enrichment.utils;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import org.jibx.runtime.JiBXException;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;

/**
 * Utilities for enrichment and dereferencing
 * Created by gmamakis on 8-3-17.
 */
public class EnrichmentUtils {
  
    private EnrichmentUtils() {}

    /**
     * Merge entities in a record after enrichment
     * @param rdf The RDF to enrich
     * @param enrichmentBaseList The information to append
     * @param fieldName The name of the field so that it can be connected to Europeana Proxy
     * @return An RDF object with the merged entities
     */
    public static RDF mergeEntity(RDF rdf, List<EnrichmentBase> enrichmentBaseList, String fieldName) {
    	return EntityMergeUtils.mergeEntity(rdf, enrichmentBaseList, fieldName);
    }

    /**
     * Convert an RDF to a UTF-8 encoded XML.
     * @param rdf The RDF object to convert
     * @return An XML string representation of the RDF object
     * @throws JiBXException
     * @throws UnsupportedEncodingException
     * @deprecated Use {@link RdfConversionUtils#convertRdftoString(RDF)}.
     */
    @Deprecated
    public static String convertRDFtoString(RDF rdf) throws JiBXException, UnsupportedEncodingException {
        return RdfConversionUtils.convertRdftoString(rdf);
    }
    
    /**
     * Extract the fields to enrich from an RDF file
     * @param RDF file
     * @return List<InputValue>
     * @throws JiBXException
     */
    public static List<InputValue> extractFieldsForEnrichmentFromRDF(RDF rdf) {
        ProxyType providerProxy = EntityMergeUtils.getProviderProxy(rdf);
        List<InputValue> valuesForEnrichment= new ArrayList<>();
        
        if (providerProxy != null) {
        	for(EnrichmentFields field: EnrichmentFields.values()) {
        		List<InputValue> values = field.extractFieldValuesForEnrichment(providerProxy);
        		valuesForEnrichment.addAll(values);
        	}
        }
        
        return valuesForEnrichment;
    }
}
