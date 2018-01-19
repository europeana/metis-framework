package eu.europeana.enrichment.utils;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;

/**
 * Utilities for enrichment and dereferencing
 * Created by gmamakis on 8-3-17.
 */
public class EnrichmentUtils {
  
    private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentUtils.class);
    private static IBindingFactory rdfBindingFactory;
    private static final String UTF8 = StandardCharsets.UTF_8.name();

    static {
      try {
        rdfBindingFactory = BindingDirectory.getFactory(RDF.class);
      } catch (JiBXException e) {
        LOGGER.error("Unable to create binding factory", e);
      }
    }

    private EnrichmentUtils() {}

    private static IBindingFactory getRdfBindingFactory() {
      if (rdfBindingFactory != null) {
        return rdfBindingFactory;
      }
      throw new IllegalStateException("No binding factory available.");
    }
    
    /**
     * Merge entities in a record after enrichment
     * @param record The record to enrich
     * @param entity The entity to append in XML
     * @param fieldName The name of the field so that it can be connected to Europeana Proxy
     * @return An RDF object with the merged entities
     * @throws JiBXException
     */
    public static RDF mergeEntityForEnrichment (String record, String entity, String fieldName) throws JiBXException {
        return EntityMergeUtils.mergeEntityForEnrichment(record, entity, fieldName);
    }
    
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
     * Convert an RDF to a UTF-8 encoded XML
     * @param rdf The RDF object to convert
     * @return An XML string representation of the RDF object
     * @throws JiBXException
     * @throws UnsupportedEncodingException
     */
    public static String convertRDFtoString(RDF rdf) throws JiBXException, UnsupportedEncodingException {
        IMarshallingContext context = getRdfBindingFactory().createMarshallingContext();
        context.setIndent(2);
        ByteArrayOutputStream out  = new ByteArrayOutputStream();
        context.marshalDocument(rdf, UTF8, null, out);
        return out.toString(UTF8);
    }
    
    /**
     * Extract the fields to enrich from an XML record
     * @param record
     * @return
     * @throws JiBXException
     */
    public static List<InputValue> extractFieldsForEnrichment(String record) throws JiBXException {
        IUnmarshallingContext rdfCTX = getRdfBindingFactory().createUnmarshallingContext();
        RDF rdf = (RDF)rdfCTX.unmarshalDocument(IOUtils.toInputStream(record),UTF8);
        
        return extractFieldsForEnrichmentFromRDF(rdf);
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
