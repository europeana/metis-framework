package eu.europeana.metis.utils;

import eu.europeana.corelib.definitions.jibx.AboutType;
import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.definitions.jibx.EuropeanaType;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

;

/**
 * Created by erikkonijnenburg on 28/07/2017.
 */
class EntityMergeUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityMergeUtils.class);
  private final static String UTF8= "UTF-8";
  private static IBindingFactory agentFactory;
  private static IBindingFactory conceptFactory;
  private static IBindingFactory placeFactory;
  private static IBindingFactory tsFactory;
  private static IBindingFactory rdfFactory;

  static {
    try {
      agentFactory = BindingDirectory.getFactory(AgentType.class);
      conceptFactory = BindingDirectory.getFactory(Concept.class);
      placeFactory = BindingDirectory.getFactory(PlaceType.class);
      tsFactory = BindingDirectory.getFactory(TimeSpanType.class);
      rdfFactory = BindingDirectory.getFactory(RDF.class);
    } catch (JiBXException e) {
      LOGGER.error("Unable to get BindingFactory", e);
      System.exit(-1);
    }
  }

  static RDF mergeEntity(String record, String entity) throws JiBXException {
    IUnmarshallingContext rdfCTX = rdfFactory.createUnmarshallingContext();
    RDF rdf = (RDF)rdfCTX.unmarshalDocument(IOUtils.toInputStream(record),UTF8);

    appendEntityInRDF(entity, rdf);

    return rdf;
  }

  static RDF mergeEntityForEnrichment(String record, String entity, String fieldName) throws JiBXException {
    IUnmarshallingContext rdfCTX = rdfFactory.createUnmarshallingContext();
    RDF rdf = (RDF)rdfCTX.unmarshalDocument(IOUtils.toInputStream(record),UTF8);

    AboutType type =  appendEntityInRDF(entity, rdf);

    if (StringUtils.isNotEmpty(fieldName)) {
      return appendToProxy(rdf, type, fieldName);
    }
    return rdf;
  }

  static ProxyType getProviderProxy(RDF rdf) {
    for(ProxyType proxyType:rdf.getProxyList()) {
      if(proxyType.getEuropeanaProxy()==null || !proxyType.getEuropeanaProxy().isEuropeanaProxy()) {
        return proxyType;
      }
    }
    return null;
  }

  private static AboutType appendEntityInRDF(String entity, RDF rdf)
      throws JiBXException {

    AboutType type = null;
    if(StringUtils.contains(entity,"skos:Concept")){
      type = appendConceptInRDF(rdf, entity);
    } else if(StringUtils.contains(entity,"edm:Agent")) {
      type = appendAgentInRDF(rdf, entity);
    } else if(StringUtils.contains(entity,"edm:Place")) {
      type = appendPlaceInRDF(rdf, entity);
    } else if(StringUtils.contains(entity,"edm:Timespan")) {
      type =  appendTimespanInRDF(rdf, entity);
    }
    if (type == null) {
      LOGGER.warn("Unknown entity found: {}", entity);
    }

    return type;
  }

  private static AgentType appendAgentInRDF(RDF rdf, String entity) throws JiBXException {
    IUnmarshallingContext unmarshaller = agentFactory.createUnmarshallingContext();
    AgentType agentType = (AgentType) unmarshaller.unmarshalDocument(IOUtils.toInputStream(entity),UTF8);

    if (rdf.getAgentList() == null) {
      rdf.setAgentList(new ArrayList<>());
    }
    rdf.getAgentList().add(agentType);
    return agentType;

  }

  private static Concept appendConceptInRDF(RDF rdf, String entity) throws JiBXException {
    IUnmarshallingContext unmarshaller = conceptFactory.createUnmarshallingContext();
    Concept conceptType = (Concept) unmarshaller.unmarshalDocument(IOUtils.toInputStream(entity),UTF8);

    if(rdf.getConceptList() == null) {
      rdf.setConceptList(new ArrayList<>());
    }
    rdf.getConceptList().add(conceptType);
    return conceptType;
  }

  private static PlaceType appendPlaceInRDF(RDF rdf, String entity) throws JiBXException {
    IUnmarshallingContext unmarshaller = placeFactory.createUnmarshallingContext();
    PlaceType placeType = (PlaceType) unmarshaller.unmarshalDocument(IOUtils.toInputStream(entity),UTF8);
    if (rdf.getPlaceList()==null) {
      rdf.setPlaceList(new ArrayList<>());
    }
    rdf.getPlaceList().add(placeType);
    return placeType;
  }

  private static TimeSpanType appendTimespanInRDF(RDF rdf, String entity) throws JiBXException {
    IUnmarshallingContext unmarshaller = tsFactory.createUnmarshallingContext();
    TimeSpanType tsType = (TimeSpanType) unmarshaller.unmarshalDocument(IOUtils.toInputStream(entity),UTF8);
    if (rdf.getTimeSpanList()==null) {
      rdf.setTimeSpanList(new ArrayList<>());
    }
    rdf.getTimeSpanList().add(tsType);
    return tsType;
  }

  private static RDF appendToProxy(RDF rdf, AboutType about, String fieldName) {
    ProxyType europeanaProxy = getEuropeanaProxy(rdf);
    appendToProxy(europeanaProxy,EnrichmentFields.valueOf(fieldName), about.getAbout());
    return replaceProxy(rdf,europeanaProxy);
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
}
