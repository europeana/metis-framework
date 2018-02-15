package eu.europeana.indexing.service;

import eu.europeana.corelib.edm.utils.construct.AgentUpdater;
import eu.europeana.corelib.edm.utils.construct.AggregationUpdater;
import eu.europeana.corelib.edm.utils.construct.ConceptUpdater;
import eu.europeana.corelib.edm.utils.construct.EuropeanaAggregationUpdater;
import eu.europeana.corelib.edm.utils.construct.LicenseUpdater;
import eu.europeana.corelib.edm.utils.construct.PlaceUpdater;
import eu.europeana.corelib.edm.utils.construct.ProvidedChoUpdater;
import eu.europeana.corelib.edm.utils.construct.ProxyUpdater;
import eu.europeana.corelib.edm.utils.construct.ServiceUpdater;
import eu.europeana.corelib.definitions.edm.entity.Proxy;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.edm.exceptions.MongoDBException;
import eu.europeana.corelib.edm.utils.MongoConstructor;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.edm.utils.construct.TimespanUpdater;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.mongo.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.ServiceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.indexing.service.dao.FullBeanDao;
import eu.europeana.metis.exception.IndexingException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.apache.commons.io.IOUtils;
import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.common.SolrInputDocument;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PublishingService {
	private static final Logger LOGGER = LoggerFactory.getLogger(PublishingService.class);	
	private FullBeanDao fullBeanDao;	
    private static IBindingFactory rdfBindingFactory;
    private static final String UTF8 = StandardCharsets.UTF_8.name();
    private SolrServer solrServer; 
    private CloudSolrServer cloudSolrServer;

    static {    	
    	try {
    		rdfBindingFactory = BindingDirectory.getFactory(RDF.class);
    	} catch (JiBXException e) {
    		LOGGER.warn("Error creating the JibX factory.");    		
    	}
    }

    public PublishingService(FullBeanDao fullBeanDao, LBHttpSolrServer solrServer, CloudSolrServer cloudSolrServer) {
  	  this.fullBeanDao = fullBeanDao;
  	  this.solrServer = solrServer;
  	  this.cloudSolrServer = cloudSolrServer;
    }

    public boolean process(String record) throws IndexingException
    {    	
    	LOGGER.info("processing record...");
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Record to process: " + record);
		}
		
    	RDF rdf = null;
    	
		try {
			rdf = toRDF(record);
		} catch (JiBXException e) {
			throw new IndexingException("Could not convert record to RDF.", e.getCause());
		} 
    	
    	FullBeanImpl fBean = null;
    	
    	if (rdf != null) {
    		try {
    			fBean = new MongoConstructor().constructFullBean(rdf);
    		} catch (InstantiationException | IllegalAccessException | IOException e) {
    			throw new IndexingException("Could not construct FullBean using MongoConstructor.", e.getCause());
    		}
    	}
    	      	
    	if (fBean != null) {
    		processFullBean(fBean, fullBeanDao, cloudSolrServer);
    		LOGGER.info("Successfully processed record.");
            return true;
    	}
    	
		LOGGER.debug("Failed to process record.");
    	return false;
    }
    
    private static void processFullBean(FullBeanImpl fBean, FullBeanDao fullBeanDao, SolrServer cloudSolrServer) throws IndexingException {
    	fBean.setEuropeanaCollectionName(new String[100]); // To prevent potential null pointer exceptions
		
		SolrDocumentHandler solrDocHandler = new SolrDocumentHandler(cloudSolrServer);
		SolrInputDocument solrInputDoc;
		
		try {
			solrInputDoc = solrDocHandler.generate(fBean);
		} catch (SolrServerException e) {
			throw new IndexingException("Could not generate Solr input document.", e.getCause());
		}

		try {
			cloudSolrServer.add(solrInputDoc);
		} catch (IOException | SolrServerException e) {
			throw new IndexingException("Could not add Solr input document to Solr server.", e.getCause());
		}
		    		  
        if (fBean.getAbout() == null) {
            try {
				saveEdmClasses(fBean, true, fullBeanDao);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | MongoDBException e) {
				throw new IndexingException("Could not save EDM classes of FullBean to Mongo.", e.getCause());
			}
            
            fullBeanDao.save(fBean);
        } else {
            try {
				updateFullBean(fBean, fullBeanDao);
			} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException | MongoDBException e) {
				throw new IndexingException("Could not update FullBean.", e.getCause());
			}
        } 
    }
    
    private static RDF toRDF(String xml) throws JiBXException {
    	IUnmarshallingContext context = getRdfBindingFactory().createUnmarshallingContext();
    	return (RDF) context.unmarshalDocument(IOUtils.toInputStream(xml), UTF8);
    }

    private static IBindingFactory getRdfBindingFactory() {
    	if (rdfBindingFactory != null)
    		return rdfBindingFactory;
    	
    	throw new IllegalStateException("No binding factory available.");
    }

    public static String convertRDFtoString(RDF rdf) throws JiBXException, UnsupportedEncodingException {
    	IMarshallingContext context = getRdfBindingFactory().createMarshallingContext();
    	context.setIndent(2);
    	ByteArrayOutputStream out  = new ByteArrayOutputStream();
    	context.marshalDocument(rdf, UTF8, null, out);
    	return out.toString(UTF8);
    }
    
    private static void saveEdmClasses(FullBeanImpl fullBean, boolean isFirstSave, FullBeanDao fullBeanDao) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, MongoDBException {
    	List<AgentImpl> agents = new ArrayList<>();
    	List<ConceptImpl> concepts = new ArrayList<>();
    	List<TimespanImpl> timespans = new ArrayList<>();
    	List<PlaceImpl> places = new ArrayList<>();
    	List<LicenseImpl> licenses = new ArrayList<>();
    	List<ServiceImpl> services = new ArrayList<>();

    	EdmMongoServer mongoServer = new EdmMongoServerImpl(fullBeanDao.getMongo(), fullBeanDao.getDBName());

    	if (fullBean.getAgents() != null)
    		updateAgents(fullBean.getAgents(), agents, fullBeanDao, mongoServer);

    	if (fullBean.getPlaces() != null)
    		updatePlaces(fullBean.getPlaces(), places, fullBeanDao, mongoServer);
    		
    	if (fullBean.getConcepts() != null)
    		updateConcepts(fullBean.getConcepts(), concepts, fullBeanDao, mongoServer);
    		
    	if (fullBean.getTimespans() != null)
    		updateTimespans(fullBean.getTimespans(), timespans, fullBeanDao, mongoServer);
    	
    	if(fullBean.getLicenses() != null)
    		updateLicenses(fullBean.getLicenses(), licenses, fullBeanDao, mongoServer);
   
    	if(fullBean.getServices() != null)
    		updateServices(fullBean.getServices(), services, fullBeanDao, mongoServer);
   
    	if (isFirstSave)
    		executeFirstSave(fullBean, fullBeanDao);		
    	else 
    		executeUpdate(fullBean, fullBeanDao, mongoServer);
    	
    	fullBean.setAgents(agents);
    	fullBean.setPlaces(places);
    	fullBean.setConcepts(concepts);
    	fullBean.setTimespans(timespans);
    	fullBean.setLicenses(licenses);
    	fullBean.setServices(services);
    }
    
    private static void updateAgents(List<AgentImpl> agentsToAdd, List<AgentImpl> agents, FullBeanDao fullBeanDao, EdmMongoServer mongoServer) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {    	
    	for (AgentImpl agent : agentsToAdd) {
    		AgentImpl retAgent = fullBeanDao.searchByAbout(AgentImpl.class, agent.getAbout());
    		if (retAgent != null) {
    			agents.add(new AgentUpdater().update(retAgent, agent, mongoServer));
    		} else {
    			try {
    				fullBeanDao.save(agent);
    				agents.add(agent);
    			} catch (Exception e) {
    				agents.add(new AgentUpdater().update(fullBeanDao.searchByAbout(AgentImpl.class, agent.getAbout()), agent, mongoServer));
    			}
    		}
    	}
    }
    
    private static void updatePlaces(List<PlaceImpl> placesToAdd, List<PlaceImpl> places, FullBeanDao fullBeanDao, EdmMongoServer mongoServer) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    	for (PlaceImpl place : placesToAdd) {
    		PlaceImpl retPlace = fullBeanDao.searchByAbout(PlaceImpl.class, place.getAbout());
    		if (retPlace != null) {
    			places.add(new PlaceUpdater().update(retPlace, place, mongoServer));
    		} else {
    			try {
    				fullBeanDao.save(place);
    				places.add(place);
    			} catch (Exception e) {
    				places.add(new PlaceUpdater().update(fullBeanDao.searchByAbout(PlaceImpl.class, place.getAbout()), place, mongoServer));
    			}
    		}
    	}
    }
        
    private static void updateConcepts(List<ConceptImpl> conceptsToAdd, List<ConceptImpl> concepts, FullBeanDao fullBeanDao, EdmMongoServer mongoServer) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    	for (ConceptImpl concept : conceptsToAdd) {
    		ConceptImpl retConcept = fullBeanDao.searchByAbout(ConceptImpl.class, concept.getAbout());
    		if (retConcept != null) {
    			concepts.add(new ConceptUpdater().update(retConcept, concept, mongoServer));
    		} else {
    			try {
    				fullBeanDao.save(concept);
    				concepts.add(concept);
    			} catch (Exception e) {
    				concepts.add(new ConceptUpdater().update(fullBeanDao.searchByAbout(ConceptImpl.class, concept.getAbout()), concept, mongoServer));
    			}
    		}
    	}
    }

    private static void updateTimespans(List<TimespanImpl> timespansToAdd, List<TimespanImpl> timespans, FullBeanDao fullBeanDao, EdmMongoServer mongoServer) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    	for (TimespanImpl timespan : timespansToAdd) {
    		TimespanImpl retTimespan = fullBeanDao.searchByAbout(TimespanImpl.class, timespan.getAbout());
    		if (retTimespan != null) {
    			timespans.add(new TimespanUpdater().update(retTimespan, timespan, mongoServer));
    		} else {
    			try {
    				fullBeanDao.save(timespan);
    				timespans.add(timespan);
    			} catch (Exception e) {
    				timespans.add(new TimespanUpdater().update(fullBeanDao.searchByAbout(TimespanImpl.class, timespan.getAbout()), timespan, mongoServer));
    			}
    		}
    	}
    }
    
    private static void updateLicenses(List<LicenseImpl> licensesToAdd, List<LicenseImpl> licenses, FullBeanDao fullBeanDao, EdmMongoServer mongoServer) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    	for(LicenseImpl license : licensesToAdd) {
    		LicenseImpl retLicense = fullBeanDao.searchByAbout(LicenseImpl.class, license.getAbout());

    		if (retLicense != null) {
    			licenses.add(new LicenseUpdater().update(retLicense, license, mongoServer));
    		} else {
    			try {
    				fullBeanDao.save(license);
    				licenses.add(license);
    			} catch (Exception e) {
    				licenses.add(new LicenseUpdater().update(fullBeanDao.searchByAbout(LicenseImpl.class, license.getAbout()), license, mongoServer));
    			}
    		}
    	}
    }
    	
    private static void updateServices(List<ServiceImpl> servicesToAdd, List<ServiceImpl> services, FullBeanDao fullBeanDao, EdmMongoServer mongoServer) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    	for(ServiceImpl service : servicesToAdd) {
    		ServiceImpl retService = fullBeanDao.searchByAbout(ServiceImpl.class, service.getAbout());
    		if(retService!=null) {
    			services.add(new ServiceUpdater().update(retService, service, mongoServer));
    		} else {
    			try {
    				fullBeanDao.save(service);
    				services.add(service);
    			} catch (Exception e) {
    				services.add(new ServiceUpdater().update(retService, service, mongoServer));
    			}
    		}
    	}
    }
    
    private static void executeFirstSave(FullBeanImpl fullBean, FullBeanDao fullBeanDao) {
    	fullBeanDao.saveProvidedCHOs(fullBean.getProvidedCHOs());
    	fullBeanDao.save(fullBean.getEuropeanaAggregation());
    	fullBeanDao.saveProxies(fullBean.getProxies());
    	fullBeanDao.saveAggregations(fullBean.getAggregations());

    	if (fullBean.getEuropeanaAggregation().getWebResources() != null)
    		fullBeanDao.saveWebResources(fullBean.getEuropeanaAggregation().getWebResources());    		

    	for (AggregationImpl aggr : fullBean.getAggregations()) {
    		if (aggr.getWebResources() != null)
    			fullBeanDao.saveWebResources(aggr.getWebResources());    
    	}
    }
    
    private static void executeUpdate(FullBeanImpl fullBean, FullBeanDao fullBeanDao, EdmMongoServer mongoServer) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    	List<ProvidedCHOImpl> pChos = new ArrayList<>();
		List<ProxyImpl> proxies = new ArrayList<>();
		List<AggregationImpl> aggregations = new ArrayList<>();

		for (ProvidedCHOImpl pCho : fullBean.getProvidedCHOs()) {            	
			ProvidedCHOImpl pCHOImpl = fullBeanDao.searchByAbout(ProvidedCHOImpl.class, pCho.getAbout());

			if (pCHOImpl != null)
				pChos.add(new ProvidedChoUpdater().update(pCHOImpl, pCho, mongoServer));           
		}

		for (AggregationImpl aggr : fullBean.getAggregations()) {            	
			AggregationImpl aggImpl = fullBeanDao.searchByAbout(AggregationImpl.class, aggr.getAbout());

			if (aggImpl != null)
				aggregations.add(new AggregationUpdater().update(aggImpl, aggr, mongoServer));            		            	
		}

		EuropeanaAggregationImpl eurImpl = fullBeanDao.searchByAbout(EuropeanaAggregationImpl.class,
				fullBean.getEuropeanaAggregation().getAbout());

		if (eurImpl != null)
			fullBean.setEuropeanaAggregation(new EuropeanaAggregationUpdater().update(eurImpl, (EuropeanaAggregationImpl) fullBean.getEuropeanaAggregation(), mongoServer));                   

		for (ProxyImpl prx : fullBean.getProxies()) {            	
			ProxyImpl proxImpl = fullBeanDao.searchByAbout(ProxyImpl.class, prx.getAbout());

			if (proxImpl != null)
				proxies.add(new ProxyUpdater().update(proxImpl, prx, mongoServer));
		}

		fullBean.setProvidedCHOs(pChos);
		fullBean.setAggregations(aggregations);
		fullBean.setProxies(proxies);
    }
    		    
    // Modified and appended legacy code
    public static FullBeanImpl updateFullBean(FullBeanImpl fullBean, FullBeanDao fullBeanDao) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, MongoDBException {
        saveEdmClasses(fullBean, false, fullBeanDao);
        
        EdmMongoServer mongoServer = new EdmMongoServerImpl(fullBeanDao.getMongo(), fullBeanDao.getDBName());
        
        Query<FullBeanImpl> updateQuery = mongoServer.getDatastore()
                .createQuery(FullBeanImpl.class).field("about")
                .equal(fullBean.getAbout().replace("/item", ""));
        
        UpdateOperations<FullBeanImpl> ops = mongoServer.getDatastore().createUpdateOperations(FullBeanImpl.class);
        
        // To avoid potential index out of bounds
        if (fullBean.getProxies().isEmpty())
        {
        	ArrayList<Proxy> proxyList = new ArrayList<>();
        	ProxyImpl proxy = new ProxyImpl();
        	proxyList.add(proxy);
        	fullBean.setProxies(proxyList);
        }
        	        
        ops.set("title", fullBean.getTitle() != null ? fullBean.getTitle() : new String[]{});
        ops.set("year", fullBean.getYear() != null ? fullBean.getYear() : new String[]{});
        ops.set("provider", fullBean.getProvider() != null ? fullBean.getProvider() : new String[]{});
        ops.set("language", fullBean.getLanguage() != null ? fullBean.getLanguage() : new String[]{});
        ops.set("type", fullBean.getType() != null ? fullBean.getType() : DocType.IMAGE);
        ops.set("europeanaCompleteness", fullBean.getEuropeanaCompleteness());
        ops.set("places", fullBean.getPlaces() != null ? fullBean.getPlaces() : new ArrayList<PlaceImpl>());
        ops.set("agents", fullBean.getAgents() != null ? fullBean.getAgents() : new ArrayList<AgentImpl>());
        ops.set("timespans", fullBean.getTimespans() != null ? fullBean.getTimespans() : new ArrayList<TimespanImpl>());
        ops.set("concepts", fullBean.getConcepts() != null ? fullBean.getConcepts() : new ArrayList<ConceptImpl>());
        ops.set("aggregations", fullBean.getAggregations());
        ops.set("providedCHOs", fullBean.getProvidedCHOs());
        ops.set("europeanaAggregation", fullBean.getEuropeanaAggregation());
        ops.set("proxies", fullBean.getProxies());
        ops.set("country", fullBean.getCountry() != null ? fullBean.getCountry() : new String[]{});
        ops.set("services",fullBean.getServices());
        ops.set("europeanaCollectionName", fullBean.getEuropeanaCollectionName());
        
        mongoServer.getDatastore().update(updateQuery, ops);
        
        return fullBeanDao.getFullBean(fullBean.getAbout());
    }
}