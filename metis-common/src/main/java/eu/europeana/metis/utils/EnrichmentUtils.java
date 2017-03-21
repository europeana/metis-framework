package eu.europeana.metis.utils;

import eu.europeana.corelib.definitions.jibx.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.jibx.runtime.*;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * Utilities for enrichment and dereferencing
 * Created by gmamakis on 8-3-17.
 */
public class EnrichmentUtils {
    private static IBindingFactory agentFactory;
    private static IBindingFactory conceptFactory;
    private static IBindingFactory placeFactory;
    private static IBindingFactory tsFactory;
    private static IBindingFactory rdfFactory;
    private final static String UTF8= "UTF-8";

    private EnrichmentUtils() throws JiBXException {
        agentFactory = BindingDirectory.getFactory(AgentType.class);
        conceptFactory = BindingDirectory.getFactory(Concept.class);
        placeFactory = BindingDirectory.getFactory(PlaceType.class);
        tsFactory = BindingDirectory.getFactory(TimeSpanType.class);
        rdfFactory = BindingDirectory.getFactory(RDF.class);
    }

    /**
     * Merge entities for dereferencing (simpler rules than enrichment)
     * @param record The original record
     * @param entity The xml representation of the entity
     * @return  A JibX RDF object (Java object)
     * @throws JiBXException
     */
    public static RDF mergeEntityForDereferencing (String record, String entity) throws JiBXException {
       return mergeEntity(record,entity, false,null);
    }

    private static RDF mergeEntity(String record, String entity, boolean isEnrichment, String fieldName) throws JiBXException{
        IUnmarshallingContext rdfCTX = rdfFactory.createUnmarshallingContext();
        RDF rdf = (RDF)rdfCTX.unmarshalDocument(IOUtils.toInputStream(record),UTF8);
        if(StringUtils.contains(entity,"skos:Concept")){
            return appendConceptInRDF(rdf, entity,isEnrichment,fieldName);
        } else if(StringUtils.contains(entity,"edm:Agent")) {
            return appendAgentInRDF(rdf, entity,isEnrichment,fieldName);
        } else if(StringUtils.contains(entity,"edm:Place")) {
            return appendPlaceInRDF(rdf, entity,isEnrichment,fieldName);
        } else if(StringUtils.contains(entity,"edm:Timespan")) {
            return appendTimespanInRDF(rdf, entity,isEnrichment,fieldName);
        }

        return rdf;
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
        return mergeEntity(record, entity,true, fieldName);
    }

    private static RDF appendAgentInRDF(RDF rdf, String entity, boolean isEnrichment, String fieldName) throws JiBXException {

        IUnmarshallingContext unmarshaller = agentFactory.createUnmarshallingContext();
        AgentType agentType = (AgentType) unmarshaller.unmarshalDocument(IOUtils.toInputStream(entity),UTF8);
        List<AgentType> agentTypeList =rdf.getAgentList()==null?new ArrayList<>():rdf.getAgentList();
        agentTypeList.add(agentType);
        rdf.setAgentList(agentTypeList);
        if(isEnrichment && StringUtils.isNotEmpty(fieldName)){
            return appendToProxy(rdf,agentType.getAbout(),fieldName);
        }
        return rdf;
    }

    private static RDF appendConceptInRDF(RDF rdf, String entity, boolean isEnrichment,String fieldName) throws JiBXException {

        IUnmarshallingContext unmarshaller = conceptFactory.createUnmarshallingContext();
        Concept conceptType = (Concept) unmarshaller.unmarshalDocument(IOUtils.toInputStream(entity),UTF8);
        List<Concept> conceptList =rdf.getConceptList()==null?new ArrayList<>():rdf.getConceptList();
        conceptList.add(conceptType);
        rdf.setConceptList(conceptList);
        if(isEnrichment && StringUtils.isNotEmpty(fieldName)){
            return appendToProxy(rdf,conceptType.getAbout(),fieldName);
        }
        return rdf;
    }

    private static RDF appendPlaceInRDF(RDF rdf, String entity, boolean isEnrichment,String fieldName) throws JiBXException {
        IUnmarshallingContext unmarshaller = placeFactory.createUnmarshallingContext();
        PlaceType placeType = (PlaceType) unmarshaller.unmarshalDocument(IOUtils.toInputStream(entity),UTF8);
        List<PlaceType> placeTypeList =rdf.getPlaceList()==null?new ArrayList<>():rdf.getPlaceList();
        placeTypeList.add(placeType);
        rdf.setPlaceList(placeTypeList);
        if(isEnrichment && StringUtils.isNotEmpty(fieldName)){
            return appendToProxy(rdf,placeType.getAbout(),fieldName);
        }
        return rdf;
    }

    private static RDF appendTimespanInRDF(RDF rdf, String entity, boolean isEnrichment,String fieldName) throws JiBXException {
        IUnmarshallingContext unmarshaller = tsFactory.createUnmarshallingContext();
        TimeSpanType tsType = (TimeSpanType) unmarshaller.unmarshalDocument(IOUtils.toInputStream(entity),UTF8);
        List<TimeSpanType> tsTypeList =rdf.getTimeSpanList()==null?new ArrayList<>():rdf.getTimeSpanList();
        tsTypeList.add(tsType);
        rdf.setTimeSpanList(tsTypeList);
        if(isEnrichment && StringUtils.isNotEmpty(fieldName)){
            return appendToProxy(rdf,tsType.getAbout(),fieldName);
        }
        return rdf;
    }

    private static RDF appendToProxy(RDF rdf, String about, String fieldName) {
        ProxyType europeanaProxy = getEuropeanaProxy(rdf);
        appendToProxy(europeanaProxy,EnrichmentFields.valueOf(fieldName),about);
        return replaceProxy(rdf,europeanaProxy);
    }

    private static RDF replaceProxy(RDF rdf, ProxyType europeanaProxy) {
        List<ProxyType> proxyTypeList = new ArrayList<>();
        proxyTypeList.add(europeanaProxy);
        for(ProxyType proxyType:rdf.getProxyList()){
            if(!StringUtils.equals(proxyType.getAbout(),europeanaProxy.getAbout())){
                proxyTypeList.add(proxyType);
            }
        }
        rdf.setProxyList(proxyTypeList);
        return rdf;
    }

    private static void appendToProxy(ProxyType europeanaProxy, EnrichmentFields enrichmentFields, String about) {
        List<EuropeanaType.Choice> choices = europeanaProxy.getChoiceList();
        choices.add(enrichmentFields.createChoice(about));
        europeanaProxy.setChoiceList(choices);
    }


    private static ProxyType getEuropeanaProxy(RDF rdf){
        for(ProxyType proxyType:rdf.getProxyList()){
            if(proxyType.getEuropeanaProxy()!=null && proxyType.getEuropeanaProxy().isEuropeanaProxy()){
                return proxyType;
            }
        }
        return null;
    }
    private static ProxyType getProviderProxy(RDF rdf){
        for(ProxyType proxyType:rdf.getProxyList()){
            if(proxyType.getEuropeanaProxy()==null || !proxyType.getEuropeanaProxy().isEuropeanaProxy()){
                return proxyType;
            }
        }
        return null;
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
     * Extract the fields to enrich from and XML record
     * @param record
     * @return
     * @throws JiBXException
     */
    public static List<InputValue> extractFieldsForEnrichment(String record) throws JiBXException {
        IUnmarshallingContext rdfCTX = rdfFactory.createUnmarshallingContext();
        RDF rdf = (RDF)rdfCTX.unmarshalDocument(IOUtils.toInputStream(record),UTF8);
        ProxyType providerProxy = getProviderProxy(rdf);
        List<InputValue> valuesForEnrichment= new ArrayList<>();
        for(EnrichmentFields field: EnrichmentFields.values()){
            List<InputValue> values = field.extractFieldValuesForEnrichment(providerProxy);
            if(values.size()>0){
                valuesForEnrichment.addAll(values);
            }
        }
        return valuesForEnrichment;
    }
}
