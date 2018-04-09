package eu.europeana.enrichment.service.wikidata;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.text.ParseException;
import java.util.Date;
import java.util.Properties;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.enrichment.api.external.model.zoho.ZohoOrganization;
import eu.europeana.enrichment.service.EntityService;
import eu.europeana.enrichment.service.dao.WikidataAccessDao;
import eu.europeana.enrichment.service.dao.WikidataAccessService;
import eu.europeana.enrichment.service.dao.WikidataOrganization;
import eu.europeana.enrichment.service.exception.WikidataAccessException;
import eu.europeana.enrichment.service.exception.ZohoAccessException;
import eu.europeana.enrichment.service.zoho.BaseZohoAccessTest;

/**
 * Test class for Wikidata workflow.
 * 
 * The Zoho/Wikidata worklfow: 
 *     1. Get organization from Zoho 
 *     2. Retrieve wikidata for given organization 
 *     3. Store retrieved wikidata in XML format (applying XSLT) 
 *     4. Parse wikidata to OrganizationWikidata object 
 *     5. Merge both organization objects 
 *     6. Store merged data in OrganizationImpl object in Metis
 * 
 * @author GrafR
 *
 */
//@Ignore
public class WikidataWorkflowTest extends BaseZohoAccessTest {

  final String WIKIDATA_TEST_OUTPUT_FILE = "test.out";
  final String TEST_WIKIDATA_ORGANIZATION_ID = "193563";
  
  String mongoHost;
  int mongoPort;
  EntityService entityService;
  WikidataAccessService wikidataAccessService;

  final Logger LOGGER = LoggerFactory.getLogger(getClass());

  
  @Before
  public void setUp() throws Exception {
    super.setUp();
    Properties props = loadProperties("/metis.properties");
    mongoHost = props.getProperty("mongo.hosts");
    mongoPort = Integer.valueOf(props.getProperty("mongo.port"));
    entityService = new EntityService(mongoHost, mongoPort);
    initializeWikidataInfrastucture();
  }

  @After
  public void tearDown() throws Exception {}

  /**
   * This method initializes classes needed for Wikidata related activities
   * 
   * @throws WikidataAccessException
   */
  private void initializeWikidataInfrastucture() throws WikidataAccessException {
    wikidataAccessService = new WikidataAccessService(new WikidataAccessDao());
  }

//  @Test
  public void processWikidataWorkflowTest() 
      throws WikidataAccessException, ZohoAccessException, ParseException, IOException, JAXBException {
  
    Date now = new Date();

    /** 1. Get organization from Zoho */ 
    ZohoOrganization org = zohoAccessService.getOrganization(TEST_ORGANIZATION_ID);
    assertNotNull(org);
    assertTrue(org.getCreated().getTime() > 0);
    assertTrue(org.getModified().getTime() > 0);
    
    /** 2. Retrieve wikidata for given organization */
    /** 3. Store retrieved wikidata in XML format (applying XSLT) */
    String uri = org.getSameAs().get(0);  
    wikidataAccessService.getWikidataAccessDao().dereference(uri);
        
    /** 4. Parse wikidata to OrganizationWikidata object */
    WikidataOrganization wikidataOrganization = 
        wikidataAccessService.getWikidataAccessDao().parse(WIKIDATA_TEST_OUTPUT_FILE);
    assertNotNull(wikidataOrganization);

    /** 5. Merge both organization objects */
    
    /** 6. Store merged data in OrganizationImpl object in Metis */
//    OrganizationTermList termList = entityService.storeOrganization(org);
//    assertNotNull(termList);
//    assertEquals(org.getAbout(), termList.getCodeUri());
//    assertEquals(termList.getCreated(), termList.getModified());
//    assertTrue(termList.getCreated().getTime() > now.getTime());
  }

  @Test
  public void retrieveWikidataToXsltXmlFileTest() 
      throws WikidataAccessException, ZohoAccessException, ParseException, IOException {
    
    String uri = wikidataAccessService.buildOrganizationUriById(TEST_WIKIDATA_ORGANIZATION_ID).toString();
    String wikidataXml = wikidataAccessService.getWikidataAccessDao()
        .dereference(uri).toString();
    assertNotNull(wikidataXml);
  }
  
//  @Test
  public void parseWikidataFromXsltXmlFileTest() 
      throws WikidataAccessException, ZohoAccessException, ParseException, IOException, JAXBException {
    
    WikidataOrganization wikidataOrganization = 
        wikidataAccessService.getWikidataAccessDao().
          parseWikidataFromXsltXmlFile(WIKIDATA_TEST_OUTPUT_FILE);
    assertNotNull(wikidataOrganization);
    LOGGER.info("wikidata organization about: " + wikidataOrganization.getOrganization().getAbout());    
    LOGGER.info("wikidata organization country: " + wikidataOrganization.getOrganization().getCountry());    
    LOGGER.info("wikidata organization homepage: " + 
        wikidataOrganization.getOrganization().getHomepage().getResource().toString());    
    assertNotNull(wikidataOrganization.getOrganization().getCountry());
    assertNotNull(wikidataOrganization.getOrganization().getHomepage().getResource().toString());
  }
    
//  @Test
//  public void mergeZohoAndWikidataOrganizationObjectsTest() 
//      throws WikidataAccessException, ZohoAccessException, ParseException, IOException, JAXBException {
//    
//    Organization zohoOrganization = zohoAccessService.getOrganization(TEST_ORGANIZATION_ID);
//    WikidataOrganization wikidataOrganization = 
//        wikidataAccessService.parseWikidataFromXsltXmlFile(WIKIDATA_TEST_OUTPUT_FILE);
//    Organization mergedOrganization = wikidataAccessService.mergeZohoAndWikidataOrganizationObjects(
//        zohoOrganization, wikidataOrganization);
//    assertNotNull(mergedOrganization);
//  }
    
}
