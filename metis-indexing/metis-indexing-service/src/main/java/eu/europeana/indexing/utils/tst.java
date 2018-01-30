package eu.europeana.indexing.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.mongodb.MongoClient;

import eu.europeana.corelib.edm.utils.construct.FullBeanHandler;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServer;
import org.apache.solr.client.solrj.SolrServerException;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.util.ClientUtils;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrInputDocument;
import org.apache.solr.common.params.ModifiableSolrParams;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import eu.europeana.corelib.definitions.edm.entity.Agent;
import eu.europeana.corelib.definitions.jibx.AgentType;
import eu.europeana.corelib.definitions.jibx.AltLabel;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.edm.utils.MongoConstructor;
import eu.europeana.corelib.edm.utils.construct.SolrDocumentHandler;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.tools.lookuptable.EuropeanaId;


import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.common.SolrInputDocument;

import eu.europeana.corelib.storage.impl.MongoProviderImpl;





public class tst {
	private static final Logger LOGGER = LoggerFactory.getLogger(tst.class);
	
	private FullBeanDao fullBeanDao;
	
	private MongoProviderImpl mongoProviderFullBean;

    private static PublishService publishService;
    private static IBindingFactory rdfBindingFactory;
    private static final String UTF8 = StandardCharsets.UTF_8.name();

   
    private static SolrServer productionCloudSolrServer;
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
    public tst(FullBeanDao fullBeanDao) {
  	  this.fullBeanDao = fullBeanDao;
    }
    
    public boolean process(String record) throws JiBXException, SolrServerException
    {
    	
    	RDF rdf = toRDF(record);
    	
    	ArrayList<AgentType> g = (ArrayList<AgentType>) rdf.getAgentList();
    	
    	AgentType at = new AgentType();
    
    	AgentImpl ai = new AgentImpl();
    	
    	ai.get
    	
    	
    	FullBeanImpl fBean = new FullBeanImpl();
    	
    	
    	
    	
    	fBean.setAbout(rdf.getProvidedCHOList().get(0).getAbout());
    	fBean.getLicenses()
    	
    	if (fBean != null) {
    		SolrInputDocument doc = new SolrDocumentHandler(publishService.getSolrServer()).generate(fBean);
    		publishService.getSolrServer().add(doc);

    		FullBeanImpl saved;
    		if (publishService.getMongoProduction().getFullBean(fBean.getAbout()) == null) {

    			new FullBeanHandler(publishService.getMongoProduction()).saveEdmClasses(fBean, true);
    			publishService.getMongoProduction().getDatastore().save(fBean);
    			saved = (FullBeanImpl) publishService.getMongoProduction()
    					.getFullBean(fBean.getAbout());
    		} else {
    			saved = new FullBeanHandler(publishService.getMongoProduction()).updateFullBean(fBean);
    		}



    		return true;
    	}

    	return false;
    }
    
    
    
    public static FullBeanImpl toFullBean(RDF rdf) {
    	FullBeanImpl result = new FullBeanImpl();
    	
    	ArrayList<AgentType> agentTypeList = (ArrayList<AgentType>) rdf.getAgentList();
    	ArrayList<AgentImpl> agentImplList = new ArrayList<AgentImpl>();
    	for (AgentType agentType : agentTypeList) {
    		AgentImpl agentImpl = new AgentImpl();
    		
    		agentImpl.setAbout(agentType.getAbout());
    		
    		
    		//agentImpl.setAltLabel(altLabel);
    		
    		HashMap<String, List<String>> resAltLabelList = new HashMap<String, List<String>>();
    		
    		for (AltLabel altLabel : agentType.getAltLabelList()) {
    			alt
    		}
    	}
    	
    	
    	
    	//rdf.getAggregationList()
    	
    	//rdf.getConceptList()
    	
    	//rdf.getDatasetList()
    	
    	//rdf.getEuropeanaAggregationList()
    	
    	//rdf.getLicenseList()
    	
    	//rdf.getOrganizationList()
    	
    	//rdf.getPlaceList()
    	
    	//rdf.getProvidedCHOList()
    	
    	//rdf.getProxyList()
    	
    	//rdf.getServiceList()
    	
    	//rdf.getTimeSpanList()
    	
    	//rdf.getWebResourceList()
    	
    	
    	
    	
    	
    	return result;    	
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
}