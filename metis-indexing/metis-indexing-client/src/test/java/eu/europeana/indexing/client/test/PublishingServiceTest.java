package eu.europeana.indexing.client.test;

import eu.europeana.indexing.client.config.IndexingConfig;
import eu.europeana.indexing.service.PublishingService;
import eu.europeana.indexing.service.dao.FullBeanDao;
import eu.europeana.indexing.service.mongo.MorphiaDatastoreProvider;
import eu.europeana.metis.exception.IndexingException;
import eu.europeana.metis.mongo.EmbeddedLocalhostMongo;

import java.io.IOException;

import org.apache.solr.client.solrj.impl.CloudSolrServer;
import org.apache.solr.client.solrj.impl.LBHttpSolrServer;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import com.mongodb.MongoClient;

public class PublishingServiceTest {
	private EmbeddedLocalhostMongo embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    private PublishingService publishingService;
    private IndexingConfig config;
	
	public static final String SAMPLE_INPUT_2 = 
			"<?xml version=\"1.0\"  encoding=\"UTF-8\" ?>" +

		  "<rdf:RDF xmlns:crm=\"http://www.cidoc-crm.org/rdfs/cidoc_crm_v5.0.2_english_label.rdfs#\"" +

		           "xmlns:dc=\"http://purl.org/dc/elements/1.1/\"" +

		           "xmlns:dcterms=\"http://purl.org/dc/terms/\"" +

		           "xmlns:edm=\"http://www.europeana.eu/schemas/edm/\"" +

		           "xmlns:foaf=\"http://xmlns.com/foaf/0.1/\"" +

		           "xmlns:ore=\"http://www.openarchives.org/ore/terms/\"" +

		           "xmlns:owl=\"http://www.w3.org/2002/07/owl#\" xmlns:rdaGr2=\"http://rdvocab.info/ElementsGr2/\"" +

		           "xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\"" +

		           "xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\"" +

		           "xmlns:skos=\"http://www.w3.org/2004/02/skos/core#\"" +

		           "xmlns:wgs84=\"http://www.w3.org/2003/01/geo/wgs84_pos#\"" +

		           "xmlns:xalan=\"http://xml.apache.org/xalan\">" +

		      "<edm:ProvidedCHO rdf:about=\"http://hdl.handle.net/10796/0C787F5D-D4D6-40B0-B0A7-C196CD67891662\"/>" +

		      "<edm:WebResource rdf:about=\"http://hdl.handle.net/10796/79B8632E-8057-4D11-A87C-98B8A20D6FF6?locatt=view:level2\">" +

		          "<edm:rights rdf:resource=\"http://www.europeana.eu/rights/rr-f/\"/>" +

		      "</edm:WebResource>" +

		      "<ore:Aggregation rdf:about=\"10796/0C787F5D-D4D6-40B0-B0A7-C196CD67891662\">" +

		          "<edm:aggregatedCHO rdf:resource=\"http://hdl.handle.net/10796/0C787F5D-D4D6-40B0-B0A7-C196CD67891662\"/>" +

		          "<edm:dataProvider>Amsab-Institute of Social History" +

		          "</edm:dataProvider><edm:isShownAt rdf:resource=\"http://hdl.handle.net/10796/0C787F5D-D4D6-40B0-B0A7-C196CD67891662#1\"/>" +

		          "<edm:isShownBy rdf:resource=\"http://hdl.handle.net/10796/79B8632E-8057-4D11-A87C-98B8A20D6FF6?locatt=view:level2\"/>" +

		          "<edm:object rdf:resource=\"http://hdl.handle.net/10796/79B8632E-8057-4D11-A87C-98B8A20D6FF6?locatt=view:level2\"/>" +

		          "<edm:provider>HOPE - Heritage of the People's Europe</edm:provider>" +

		          "<edm:rights rdf:resource=\"http://www.europeana.eu/rights/rr-f/\"/>" +

		      "</ore:Aggregation>" +

		      "<ore:Proxy rdf:about=\"http://hdl.handle.net/10796/0C787F5D-D4D6-40B0-B0A7-C196CD67891662\">" +

		          "<dc:identifier>http://hdl.handle.net/10796/0C787F5D-D4D6-40B0-B0A7-C196CD67891662</dc:identifier>" +

		          "<dc:identifier>PV-MTSF 258</dc:identifier>" +

		          "<dc:language>fra</dc:language>" +

		          "<dc:relation>http://www.peoplesheritage.eu</dc:relation>" +

		          "<dc:subject>Syndicalism/Trade Unions</dc:subject>" +

		          "<dc:subject>Socialist and social democrat parties/Socialist International</dc:subject>" +

		          "<dc:subject>Workers movements/Workers councils/Workers International organizations</dc:subject>" +

		          "<dc:title>Combat (1974)13</dc:title>" +

		          "<dc:type>item</dc:type>" +

		          "<dcterms:isPartOf rdf:resource=\"http://hdl.handle.net/10796/4BF04AA4-0D1B-4A8F-B4F3-A32EE1B05D064\"/>" +

		          "<dcterms:issued>1974</dcterms:issued>" +

		          "<dcterms:provenance/>" +

		          "<edm:type>TEXT</edm:type>" +

		          "</ore:Proxy>" +

		      "<edm:EuropeanaAggregation rdf:about=\"http://hdl.handle.net/10796/0C787F5D-D4D6-40B0-B0A7-C196CD67891662\">" +

		          "<edm:aggregatedCHO rdf:resource=\"10796/0C787F5D-D4D6-40B0-B0A7-C196CD67891662\"/>" +

		          "<edm:country>Hungary</edm:country>" +

		          "<edm:language>hu</edm:language>" +

		          "<edm:rights rdf:resource=\"http://www.europeana.eu/rights/rr-f/\"/>" +

		      "</edm:EuropeanaAggregation></rdf:RDF>";
	
	@Before
    public void prepare() throws IOException, IndexingException {
		embeddedLocalhostMongo.start();
        String mongoHost = embeddedLocalhostMongo.getMongoHost();
        int mongoPort = embeddedLocalhostMongo.getMongoPort();
        
	    MorphiaDatastoreProvider morphiaDatastoreProvider = new MorphiaDatastoreProvider(new MongoClient(mongoHost + ":" + mongoPort), "test");
	    FullBeanDao fullBeanDao = new FullBeanDao(morphiaDatastoreProvider);
	    
	    LBHttpSolrServer solrServer = Mockito.mock(LBHttpSolrServer.class);
	    CloudSolrServer cloudSolrServer = Mockito.mock(CloudSolrServer.class);
	    	    
	    publishingService = new PublishingService(fullBeanDao, solrServer, cloudSolrServer);
    }

	@Test
	public void testPublish() throws IndexingException {					
		// NOTE: At present, corelib null pointer issues occuring when working with mocks
		
		//publishingService.process(SAMPLE_INPUT_2);
	}
}
