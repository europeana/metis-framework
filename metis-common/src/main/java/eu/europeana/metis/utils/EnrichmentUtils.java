package eu.europeana.metis.utils;

import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
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

/**
 * Utilities for enrichment and dereferencing
 * Created by gmamakis on 8-3-17.
 */
public class EnrichmentUtils {
    public static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentUtils.class);

    private static IBindingFactory rdfFactory;
    private final static String UTF8= "UTF-8";

    static {
        try {
            rdfFactory = BindingDirectory.getFactory(RDF.class);
        } catch (JiBXException e) {
            LOGGER.error("Unable to get BindingFactory", e);
            System.exit(-1);
        }
    }

    private EnrichmentUtils() { }

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
     * Convert an RDF to a UTF-8 encoded XML
     * @param rdf The RDF object to convert
     * @return An XML string representation of the RDF object
     * @throws JiBXException
     * @throws UnsupportedEncodingException
     */
    public static String convertRDFtoString(RDF rdf) throws JiBXException, UnsupportedEncodingException {
        IMarshallingContext context = rdfFactory.createMarshallingContext();
        context.setIndent(2);
        ByteArrayOutputStream out  = new ByteArrayOutputStream();
        context.marshalDocument(rdf, UTF8,
                null, out);
        return out.toString(UTF8);
    }

    /**
     * Extract the fields to enrich from an XML record
     * @param record
     * @return
     * @throws JiBXException
     */
    public static List<InputValue> extractFieldsForEnrichment(String record) throws JiBXException {
        IUnmarshallingContext rdfCTX = rdfFactory.createUnmarshallingContext();
        RDF rdf = (RDF)rdfCTX.unmarshalDocument(IOUtils.toInputStream(record),UTF8);

        ProxyType providerProxy = EntityMergeUtils.getProviderProxy(rdf);
        List<InputValue> valuesForEnrichment= new ArrayList<>();
        for(EnrichmentFields field: EnrichmentFields.values()){
            List<InputValue> values = field.extractFieldValuesForEnrichment(providerProxy);
            valuesForEnrichment.addAll(values);
        }
        return valuesForEnrichment;
    }
}
