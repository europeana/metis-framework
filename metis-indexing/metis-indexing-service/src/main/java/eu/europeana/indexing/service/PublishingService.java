package eu.europeana.indexing.service;

import eu.europeana.corelib.edm.utils.construct.AgentUpdater;
import eu.europeana.corelib.edm.utils.construct.AggregationUpdater;
import eu.europeana.corelib.edm.utils.construct.ConceptUpdater;
import eu.europeana.corelib.edm.utils.construct.EuropeanaAggregationUpdater;
import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;
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
import eu.europeana.corelib.storage.impl.MongoProviderImpl;
import eu.europeana.indexing.model.IndexingMongoServer;
import eu.europeana.indexing.service.dao.FullBeanDao;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PublishingService {
	private static final Logger LOGGER = LoggerFactory.getLogger(PublishingService.class);
	
	private FullBeanDao fullBeanDao;
	
	private MongoProviderImpl mongoProviderFullBean;

    private static IBindingFactory rdfBindingFactory;
    private static final String UTF8 = StandardCharsets.UTF_8.name();

    private static SolrServer solrServer, cloudSolrServer;
    private static FullBeanHandler handler;

    static {
    	try {

    		rdfBindingFactory = BindingDirectory.getFactory(RDF.class);
    		
    		
    	} catch (JiBXException e) {
    		e.printStackTrace();
    		LOGGER.warn("Error creating the JibX factory");
    	}

    }
    
    @Autowired
    public PublishingService(FullBeanDao fullBeanDao, LBHttpSolrServer solrServer, CloudSolrServer cloudSolrServer) {
  	  this.fullBeanDao = fullBeanDao;
  	  this.solrServer = solrServer;
  	  this.cloudSolrServer = cloudSolrServer;
    }
    
    public boolean process(String record) throws JiBXException, SolrServerException
    {    	
    	RDF rdf = toRDF(record); 
    	
    	FullBeanImpl fBean = null;
		try {
			fBean = new MongoConstructor().constructFullBean(rdf);
		} catch (InstantiationException | IllegalAccessException | IOException e) {
			e.printStackTrace();
		}
    	      	
    	if (fBean != null) {
    		fBean.setEuropeanaCollectionName(new String[100]); // To prevent null pointer exception using sample
    		    		
    		SolrDocumentHandler solrDocHandler = new SolrDocumentHandler(cloudSolrServer);
    		SolrInputDocument solrInputDoc = solrDocHandler.generate(fBean);
    		
    		try {
				cloudSolrServer.add(solrInputDoc);
			} catch (IOException e) {
				e.printStackTrace();
			}
    		    		  
            FullBeanImpl saved;
            
            if (fBean.getAbout() == null) {
                try {
					saveEdmClasses(fBean, true, fullBeanDao);
				} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
                
                fullBeanDao.save(fBean);
                
                saved = fullBeanDao.getFullBean(fBean.getAbout());
            } else {
                try {
					saved = updateFullBean(fBean, fullBeanDao);
				} catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            }            
            
            return true;
    	}

    	return false;
    }
    
    public static RDF toRDF(String xml) throws JiBXException {
    	IUnmarshallingContext context = getRdfBindingFactory().createUnmarshallingContext();
    	return (RDF) context.unmarshalDocument(IOUtils.toInputStream(xml), UTF8);
    }

    private static IBindingFactory getRdfBindingFactory() {
    	if (rdfBindingFactory != null) {
    		return rdfBindingFactory;
    	}
    	throw new IllegalStateException("No binding factory available.");
    }

    public static String convertRDFtoString(RDF rdf) throws JiBXException, UnsupportedEncodingException {
    	IMarshallingContext context = getRdfBindingFactory().createMarshallingContext();
    	context.setIndent(2);
    	ByteArrayOutputStream out  = new ByteArrayOutputStream();
    	context.marshalDocument(rdf, UTF8, null, out);
    	return out.toString(UTF8);
    }
    
    private static void saveEdmClasses(FullBeanImpl fullBean, boolean isFirstSave, FullBeanDao fullBeanDao) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        List<AgentImpl> agents = new ArrayList<AgentImpl>();
        List<ConceptImpl> concepts = new ArrayList<ConceptImpl>();
        List<TimespanImpl> timespans = new ArrayList<TimespanImpl>();
        List<PlaceImpl> places = new ArrayList<PlaceImpl>();
        List<LicenseImpl> licenses = new ArrayList<>();
        List<ServiceImpl> services = new ArrayList<>();
               
        EdmMongoServer mongoServer = null;
       
        try {
			mongoServer = new EdmMongoServerImpl(fullBeanDao.getMongoClient(), fullBeanDao.getDB());
		} catch (MongoDBException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
        
        if (fullBean.getAgents() != null) {
            for (AgentImpl agent : fullBean.getAgents()) {
                AgentImpl retAgent = fullBeanDao.searchByAbout(AgentImpl.class, agent.getAbout());
                if (retAgent != null) {
                    agents.add(new AgentUpdater().update(retAgent, agent, mongoServer));
                } else {
                    try {
                        fullBeanDao.getDS().save(agent);
                        agents.add(agent);
                    } catch (Exception e) {
                        agents.add(new AgentUpdater().update(fullBeanDao.searchByAbout(AgentImpl.class,
                                agent.getAbout()), agent, mongoServer));
                    }
                }
            }
        }
        if (fullBean.getPlaces() != null) {
            for (PlaceImpl place : fullBean.getPlaces()) {
                PlaceImpl retPlace = fullBeanDao.searchByAbout(PlaceImpl.class,
                        place.getAbout());
                if (retPlace != null) {
                    places.add(new PlaceUpdater().update(retPlace, place, mongoServer));
                } else {
                    try {
                        mongoServer.getDatastore().save(place);
                        places.add(place);
                    } catch (Exception e) {
                        places.add(new PlaceUpdater().update(fullBeanDao.searchByAbout(PlaceImpl.class,
                                place.getAbout()), place, mongoServer));
                    }
                }
            }
        }
        if (fullBean.getConcepts() != null) {
            for (ConceptImpl concept : fullBean.getConcepts()) {
                ConceptImpl retConcept = fullBeanDao.searchByAbout(
                        ConceptImpl.class, concept.getAbout());
                if (retConcept != null) {
                    concepts.add(new ConceptUpdater().update(retConcept, concept,
                            mongoServer));
                } else {
                    try {
                        mongoServer.getDatastore().save(concept);
                        concepts.add(concept);
                    } catch (Exception e) {
                        concepts.add(new ConceptUpdater().update(fullBeanDao.searchByAbout(
                                ConceptImpl.class, concept.getAbout()), concept,
                                mongoServer));
                    }
                }
            }
        }
        if (fullBean.getTimespans() != null) {
            for (TimespanImpl timespan : fullBean.getTimespans()) {
                TimespanImpl retTimespan = fullBeanDao.searchByAbout(
                        TimespanImpl.class, timespan.getAbout());
                if (retTimespan != null) {
                    timespans.add(new TimespanUpdater().update(retTimespan, timespan,
                            mongoServer));
                } else {
                    try {
                        mongoServer.getDatastore().save(timespan);
                        timespans.add(timespan);
                    } catch (Exception e) {
                        timespans.add(new TimespanUpdater().update(fullBeanDao.searchByAbout(
                                TimespanImpl.class, timespan.getAbout()), timespan,
                                mongoServer));
                    }
                }
            }
        }
        
        if(fullBean.getLicenses()!=null){
        	for(LicenseImpl license: fullBean.getLicenses()){
        		LicenseImpl retLicense = fullBeanDao.searchByAbout(
                        LicenseImpl.class, license.getAbout());
        	
        	if (retLicense != null) {
                licenses.add(new LicenseUpdater().update(retLicense, license,
                        mongoServer));
            } else {
                try {
                    mongoServer.getDatastore().save(license);
                    licenses.add(license);
                } catch (Exception e) {
                    licenses.add(new LicenseUpdater().update(fullBeanDao.searchByAbout(
                            LicenseImpl.class, license.getAbout()), license,
                            mongoServer));
                }
            }
        	}
        }

        if(fullBean.getServices()!=null){
            for(ServiceImpl service:fullBean.getServices()){
                ServiceImpl retService = fullBeanDao.searchByAbout(ServiceImpl.class,service.getAbout());
                if(retService!=null){
                    services.add(new ServiceUpdater().update(retService,service,mongoServer));
                } else {
                    try {
                        mongoServer.getDatastore().save(service);
                        services.add(service);
                    } catch (Exception e){
                        services.add(new ServiceUpdater().update(retService,service,mongoServer));
                    }
                }
            }
        }
        
        if (isFirstSave) {
            mongoServer.getDatastore().save(fullBean.getProvidedCHOs());
            mongoServer.getDatastore().save(fullBean.getEuropeanaAggregation());
            mongoServer.getDatastore().save(fullBean.getProxies());
            mongoServer.getDatastore().save(fullBean.getAggregations());
            if (fullBean.getEuropeanaAggregation().getWebResources() != null) {
                mongoServer.getDatastore().save(
                        fullBean.getEuropeanaAggregation().getWebResources());
            }
            for (AggregationImpl aggr : fullBean.getAggregations()) {
                if (aggr.getWebResources() != null) {
                    mongoServer.getDatastore().save(aggr.getWebResources());
                }
            }
        } else {
            List<ProvidedCHOImpl> pChos = new ArrayList<ProvidedCHOImpl>();
            List<ProxyImpl> proxies = new ArrayList<ProxyImpl>();
            List<AggregationImpl> aggregations = new ArrayList<AggregationImpl>();
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
            	fullBean.setEuropeanaAggregation(new EuropeanaAggregationUpdater().update(eurImpl,
            			(EuropeanaAggregationImpl) fullBean.getEuropeanaAggregation(),
            			mongoServer));                   

            for (ProxyImpl prx : fullBean.getProxies()) {
            	
            	ProxyImpl proxImpl = fullBeanDao.searchByAbout(ProxyImpl.class, prx.getAbout());
            	
            	if (proxImpl != null)
            		proxies.add(new ProxyUpdater().update(proxImpl, prx, mongoServer));
            	
            }
            
            fullBean.setProvidedCHOs(pChos);
            fullBean.setAggregations(aggregations);
            fullBean.setProxies(proxies);
        }
        
        fullBean.setAgents(agents);
        fullBean.setPlaces(places);
        fullBean.setConcepts(concepts);
        fullBean.setTimespans(timespans);
        fullBean.setLicenses(licenses);
        fullBean.setServices(services);
    }
    
    public static FullBeanImpl updateFullBean(FullBeanImpl fullBean, FullBeanDao fullBeanDao) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        saveEdmClasses(fullBean, false, fullBeanDao);
        
        IndexingMongoServer mongoServer = new IndexingMongoServer(fullBeanDao.getDS());
        
        Query<FullBeanImpl> updateQuery = mongoServer.getDatastore()
                .createQuery(FullBeanImpl.class).field("about")
                .equal(fullBean.getAbout().replace("/item", ""));
        
        UpdateOperations<FullBeanImpl> ops = mongoServer.getDatastore().createUpdateOperations(FullBeanImpl.class);
        
        // To avoid index out of bounds below
        if (fullBean.getProxies().size() == 0)
        {
        	ArrayList<Proxy> proxyList = new ArrayList<Proxy>();
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
        
        try {
            return (FullBeanImpl) fullBeanDao.getFullBean(fullBean.getAbout());
        } catch (Exception e) {
            //log.log(Level.SEVERE, e.getMessage());
        }
        
        return fullBean;
    }
}