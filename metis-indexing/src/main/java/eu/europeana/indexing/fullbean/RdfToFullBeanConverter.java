package eu.europeana.indexing.fullbean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.Aggregation;
import eu.europeana.corelib.definitions.jibx.CollectionName;
import eu.europeana.corelib.definitions.jibx.Concept;
import eu.europeana.corelib.definitions.jibx.DatasetName;
import eu.europeana.corelib.definitions.jibx.EuropeanaAggregationType;
import eu.europeana.corelib.definitions.jibx.License;
import eu.europeana.corelib.definitions.jibx.PlaceType;
import eu.europeana.corelib.definitions.jibx.ProvidedCHOType;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.Service;
import eu.europeana.corelib.definitions.jibx.TimeSpanType;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.ServiceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.indexing.exception.IndexingException;

/**
 * This class converts instances of {@link RDF} to instances of {@link FullBeanImpl}.
 * 
 * @author jochen
 *
 */
public class RdfToFullBeanConverter {

  /**
   * Converts an RDF to Full Bean.
   * 
   * @param rdf The RDF.
   * @return The Full Bean.
   * @throws IndexingException In case there was a problem with the parsing or conversion.
   */
  public FullBeanImpl convertRdfToFullBean(RDF rdf) throws IndexingException {

    // Convert RDF to FullBean
    final FullBeanImpl fBean = convertInternal(rdf);

    // Sanity Check - shouldn't happen
    if (fBean == null) {
      throw new IndexingException("Could not construct FullBean: null was returned.");
    }

    // Done.
    return fBean;
  }

  private FullBeanImpl convertInternal(RDF record) {
    FullBeanImpl fullBean = new FullBeanImpl();
    List<AgentImpl> agents = new ArrayList<>();
    List<AggregationImpl> aggregations = new ArrayList<>();
    List<ConceptImpl> concepts = new ArrayList<>();
    List<PlaceImpl> places = new ArrayList<>();
    List<TimespanImpl> timespans = new ArrayList<>();
    List<ProxyImpl> proxies = new ArrayList<>();
    List<ProvidedCHOImpl> providedCHOs = new ArrayList<>();
    List<LicenseImpl> licenses = new ArrayList<>();
    List<ServiceImpl> services = new ArrayList<>();

    for (ProvidedCHOType pcho : record.getProvidedCHOList()) {
      fullBean.setAbout(pcho.getAbout());
      providedCHOs.add(new ProvidedCHOFieldInput().createProvidedCHOMongoFields(pcho));
    }
    for (ProxyType proxytype : record.getProxyList()) {
      proxies.add(new ProxyFieldInput().createProxyMongoFields(new ProxyImpl(), proxytype));
    }
    for (Aggregation aggregation : record.getAggregationList()) {
      final List<WebResourceImpl> webResources;
      if (record.getWebResourceList() != null && !record.getWebResourceList().isEmpty()) {
        Collection<WebResourceType> webResourcesBeforeConversion =
            record.getWebResourceList().stream().collect(Collectors.toMap(WebResourceType::getAbout,
                UnaryOperator.identity(), (first, second) -> first)).values();
        final WebResourceFieldInput webResourceConverter = new WebResourceFieldInput();
        webResources = webResourcesBeforeConversion.stream()
            .map(webResourceConverter::createWebResources).collect(Collectors.toList());
      } else {
        webResources = Collections.emptyList();
      }
      aggregations
          .add(new AggregationFieldInput().createAggregationMongoFields(aggregation, webResources));
    }

    if (record.getConceptList() != null) {
      for (Concept concept : record.getConceptList()) {
        concepts.add(new ConceptFieldInput().createNewConcept(concept));
      }
    }

    if (record.getPlaceList() != null) {
      for (PlaceType place : record.getPlaceList()) {
        places.add(new PlaceFieldInput().createNewPlace(place));
      }
    }

    if (record.getTimeSpanList() != null) {
      for (TimeSpanType tspan : record.getTimeSpanList()) {
        timespans.add(new TimespanFieldInput().createNewTimespan(tspan));
      }
    }
    if (record.getAgentList() != null) {
      for (AgentType agent : record.getAgentList()) {
        agents.add(new AgentFieldInput().createNewAgent(agent));
      }
    }
    if (record.getLicenseList() != null) {
      for (License license : record.getLicenseList()) {
        licenses.add(new LicenseFieldInput().createLicenseMongoFields(license));
      }
    }
    if (record.getEuropeanaAggregationList() != null) {
      for (EuropeanaAggregationType eaggregation : record.getEuropeanaAggregationList()) {
        fullBean.setEuropeanaAggregation(
            new EuropeanaAggregationFieldInput().createAggregationMongoFields(eaggregation));
      }
    }
    if (record.getServiceList() != null) {
      for (Service service : record.getServiceList()) {
        services.add(new ServiceFieldInput().createServiceMongoFields(service));
      }
    }
    fullBean.setProvidedCHOs(providedCHOs);

    fullBean.setAggregations(aggregations);
    fullBean.setServices(services);

    if (!agents.isEmpty()) {
      fullBean.setAgents(agents);
    }
    if (!concepts.isEmpty()) {
      fullBean.setConcepts(concepts);
    }
    if (!places.isEmpty()) {
      fullBean.setPlaces(places);
    }
    if (!timespans.isEmpty()) {
      fullBean.setTimespans(timespans);
    }
    if (!proxies.isEmpty()) {
      fullBean.setProxies(proxies);
    }
    if (!licenses.isEmpty()) {
      fullBean.setLicenses(licenses);
    }

    fullBean.setEuropeanaCollectionName(new String[] {getDatasetNameFromRdf(record)});

    return fullBean;
  }

  private static String getDatasetNameFromRdf(RDF rdf) {

    // Try to find the europeana aggregation
    final EuropeanaAggregationType aggregation;
    if (rdf.getEuropeanaAggregationList() == null || rdf.getEuropeanaAggregationList().isEmpty()) {
      aggregation = null;
    } else {
      aggregation = rdf.getEuropeanaAggregationList().get(0);
    }
    if (aggregation == null) {
      return "";
    }

    // Try the dataset name property from the aggregation.
    final DatasetName datasetNameObject = aggregation.getDatasetName();
    final String datasetName = datasetNameObject == null ? null : datasetNameObject.getString();
    if (StringUtils.isNotBlank(datasetName)) {
      return datasetName;
    }

    // If that fails, try the collection name property from the aggregation.
    final CollectionName collectionNameObject = aggregation.getCollectionName();
    final String collectionName =
        collectionNameObject == null ? null : collectionNameObject.getString();
    return StringUtils.isNotBlank(collectionName) ? collectionName : "";
  }
}
