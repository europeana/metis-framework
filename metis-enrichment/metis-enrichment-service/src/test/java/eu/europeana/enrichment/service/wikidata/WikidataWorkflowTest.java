package eu.europeana.enrichment.service.wikidata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import javax.xml.bind.JAXBException;
import org.apache.commons.collections.ListUtils;
import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.api.external.ObjectIdSerializer;
import eu.europeana.enrichment.api.external.model.WikidataOrganization;
import eu.europeana.enrichment.api.external.model.zoho.ZohoOrganization;
import eu.europeana.enrichment.service.EntityService;
import eu.europeana.enrichment.service.exception.WikidataAccessException;
import eu.europeana.enrichment.service.exception.ZohoAccessException;

/**
 * Test class for Wikidata workflow.
 * 
 * The Zoho/Wikidata worklfow: 1. Get organization from Zoho 2. Retrieve wikidata for given
 * organization 3. Store retrieved wikidata in XML format (applying XSLT) 4. Parse wikidata to
 * OrganizationWikidata object 5. Merge both organization objects 6. Store merged data in
 * OrganizationImpl object in Metis
 * 
 * @author GrafR
 *
 */
public class WikidataWorkflowTest extends BaseWikidataAccessSetup {

  EntityService entityService;
  final Logger LOGGER = LoggerFactory.getLogger(getClass());

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
    Properties props = loadProperties("/metis.properties");
    String mongoHost = props.getProperty("mongo.hosts");
    int mongoPort = Integer.valueOf(props.getProperty("mongo.port"));
    entityService = new EntityService(mongoHost, mongoPort);
    initWikidataAccessService();
  }

  @After
  public void tearDown() throws Exception {}
  
  @Test
  public void testMergeLists(){
    List<String> list1 = Arrays.asList( new String[]{"a", "b" , "c"}); 
    List<String> list2 = Arrays.asList( new String[]{"b" , "c", "d"});
    List<String> list3 = Arrays.asList( new String[]{"a", "b" , "c", "d"});
    @SuppressWarnings("unchecked")
    List<String> res = ListUtils.sum(list1, list2);
    assertTrue(list3.equals(res));
  }

  @Test
  public void mergeOrganizationsFromFilesTest() throws WikidataAccessException, ZohoAccessException,
      ParseException, IOException, JAXBException, URISyntaxException {

    /** 1. Get organization from Zoho file */
    File zohoTestInputFile = getClasspathFile(ZOHO_TEST_INPUT_FILE);
    ZohoOrganization org = zohoAccessService.getOrganizationFromFile(zohoTestInputFile);
    assertNotNull(org);
    assertNotNull(org.getSameAs());
    assertNotNull(org.getSameAs().get(0));

    /** 2. Get organization from Wikidata file */
    File wikidataTestInputFile = getClasspathFile(WIKIDATA_TEST_INPUT_FILE);
    WikidataOrganization wikidataOrganization = wikidataAccessDao
        .parseWikidataOrganization(wikidataTestInputFile);
    assertNotNull(wikidataOrganization);

    /** 3. Convert both organization objects to OrganizationImpl format */
    Organization zohoOrganizationImpl = zohoAccessService.toEdmOrganization(org);
    Organization wikidataOrganizationImpl =
        wikidataAccessService.toOrganizationImpl(wikidataOrganization);

    /** remove object IDs because they are always unique and are not needed for this test */
    zohoOrganizationImpl.setId(null);
    wikidataOrganizationImpl.setId(null);

    /** 4. Merge both organization objects */
    wikidataAccessService
        .mergePropsFromWikidata(zohoOrganizationImpl, wikidataOrganizationImpl);
    assertNotNull(zohoOrganizationImpl);

    /** 5. Store merged OrganizationImpl object in output file */
    String serializedOrganizationImpl = serialize((OrganizationImpl) zohoOrganizationImpl);
    assertNotNull(serializedOrganizationImpl);
    File organizationImplOutputFile = getClasspathFile(ORGANIZATION_IMPL_TEST_OUTPUT_FILE);
    FileUtils.write(organizationImplOutputFile, serializedOrganizationImpl, "UTF-8");

    /** 6. Compare output file with expected file */
    File organizationImplTestOutputFile = getClasspathFile(ORGANIZATION_IMPL_TEST_OUTPUT_FILE);
    File organizationImplTestExpectedFile = getClasspathFile(ORGANIZATION_IMPL_TEST_EXPECTED_FILE);
    String outputOrganizationImplStr =
        FileUtils.readFileToString(organizationImplTestOutputFile, "UTF-8");
    String expectedOrganizationImplStr =
        FileUtils.readFileToString(organizationImplTestExpectedFile, "UTF-8");

    // TODO try to switch to object comparison (eventually using Map deserialization)
    assertEquals(outputOrganizationImplStr, expectedOrganizationImplStr);
  }

  /**
   * This method serializes an OrganizationImpl object to a string.
   * 
   * @param organization
   * @return wikidata organization
   * @throws JAXBException
   * @throws IOException
   */
  private static String serialize(OrganizationImpl organization) throws JAXBException, IOException {
    ObjectMapper mapper = new ObjectMapper();
    SimpleModule sm = new SimpleModule("objId", Version.unknownVersion());
    sm.addSerializer(new ObjectIdSerializer());
    mapper.registerModule(sm);
    String res = mapper.writeValueAsString(organization);
    return res;
  }

  @Test
  public void mergeZohoAndWikidataOrganizationsTest() throws WikidataAccessException, ZohoAccessException,
      ParseException, IOException, JAXBException, URISyntaxException {

//     Get Zoho organization 
    File zohoTestInputFile = getClasspathFile(ZOHO_TEST_INPUT_FILE);
    ZohoOrganization org = zohoAccessService.getOrganizationFromFile(zohoTestInputFile);
    assertNotNull(org);
    assertTrue(org.getCreated().getTime() > 0);
    assertTrue(org.getModified().getTime() > 0);

    // Parse wikidata to OrganizationWikidata object 
    File wikidataTestOutputFile = getClasspathFile(WIKIDATA_TEST_OUTPUT_FILE);
    WikidataOrganization wikidataOrganization =
       wikidataAccessDao.parse(wikidataTestOutputFile);
    assertNotNull(wikidataOrganization);

    /** 5. Merge both organization objects */
    Organization zohoOrganizationImpl = zohoAccessService.toEdmOrganization(org);
    Organization wikidataOrganizationImpl =
        wikidataAccessService.toOrganizationImpl(wikidataOrganization);
    wikidataAccessService
        .mergePropsFromWikidata(zohoOrganizationImpl, wikidataOrganizationImpl);
    assertNotNull(zohoOrganizationImpl);

    /** 6. Store merged data in OrganizationImpl object in Metis */
    // Date now = new Date();
    // OrganizationTermList termList = entityService.storeOrganization(org);
    // assertNotNull(termList);
    // assertEquals(org.getAbout(), termList.getCodeUri());
    // assertEquals(termList.getCreated(), termList.getModified());
    // assertTrue(termList.getCreated().getTime() > now.getTime());
  }

  @Test
  public void retrieveWikidataOrganigationXmlTest()
      throws WikidataAccessException, ZohoAccessException, ParseException, IOException {

    String uri =
        wikidataAccessService.buildOrganizationUri(TEST_WIKIDATA_ORGANIZATION_ID).toString();
    String wikidataXml = wikidataAccessDao.getEntity(uri).toString();
    assertNotNull(wikidataXml);
  }

  @Test
  public void parseWikidataFromXmlFileTest() throws WikidataAccessException,
      ZohoAccessException, ParseException, IOException, JAXBException, URISyntaxException {

    File wikidataTestOutputFile = getClasspathFile(WIKIDATA_TEST_OUTPUT_FILE);
    WikidataOrganization wikidataOrganization = wikidataAccessDao
        .parseWikidataOrganization(wikidataTestOutputFile);
    assertNotNull(wikidataOrganization);
    LOGGER
        .info("wikidata organization about: " + wikidataOrganization.getOrganization().getAbout());
    LOGGER.info(
        "wikidata organization country: " + wikidataOrganization.getOrganization().getCountry());
    LOGGER.info("wikidata organization homepage: "
        + wikidataOrganization.getOrganization().getHomepage().getResource().toString());
    assertNotNull(wikidataOrganization.getOrganization().getCountry());
    assertNotNull(wikidataOrganization.getOrganization().getHomepage().getResource().toString());
  }

  @Test
  public void mergeZohoAndWikidataOrganizationFromFilesTest() throws WikidataAccessException,
      ZohoAccessException, ParseException, IOException, JAXBException, URISyntaxException {

//    ZohoOrganization zohoOrganization = zohoAccessService.getOrganization(TEST_ORGANIZATION_ID);
    File zohoTestInputFile = getClasspathFile(ZOHO_TEST_INPUT_FILE);
    ZohoOrganization zohoOrganization = zohoAccessService.getOrganizationFromFile(zohoTestInputFile);
    
    Organization zohoOrganizationImpl = zohoAccessService.toEdmOrganization(zohoOrganization);
    File wikidataTestOutputFile = getClasspathFile(WIKIDATA_TEST_OUTPUT_FILE);
    WikidataOrganization wikidataOrganization =
        wikidataAccessService.parseWikidataOrganization(wikidataTestOutputFile);
    Organization wikidataOrganizationImpl =
        wikidataAccessService.toOrganizationImpl(wikidataOrganization);
    wikidataAccessService
        .mergePropsFromWikidata(zohoOrganizationImpl, wikidataOrganizationImpl);
    assertNotNull(zohoOrganizationImpl);
  }

}
