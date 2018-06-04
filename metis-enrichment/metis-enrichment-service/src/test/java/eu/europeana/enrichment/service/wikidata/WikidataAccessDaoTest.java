package eu.europeana.enrichment.service.wikidata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Locale;
import javax.xml.bind.JAXBException;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.enrichment.api.external.model.WikidataOrganization;
import eu.europeana.enrichment.service.exception.WikidataAccessException;
import eu.europeana.enrichment.service.exception.ZohoAccessException;

/**
 * Test class for Wikidata Access Dao.
 * 
 * @author GrafR
 *
 */
public class WikidataAccessDaoTest extends BaseWikidataAccessSetup {

  final String WIKIDATA_URL_BNF =
      "http://www.wikidata.org/entity/Q" + TEST_WIKIDATA_ORGANIZATION_ID;
  final String WIKIDATA_URL_BL =
      "http://www.wikidata.org/entity/Q23308";
  final String WIKIDATA_URL_NISV =
      "http://www.wikidata.org/entity/Q1131877";
  final String TEST_ACRONYM = "BNF";
  final String TEST_COUNTRY = "FR";

  @Override
  @Before
  public void setUp() throws Exception {
    super.initWikidataAccessService();
  }

  @After
  public void tearDown() throws Exception {}

  // TODO JV it is not a good idea to test on real-time data that comes from a external source. This
  // can break our tests if the data is changed.
  // Answer SG: we can try to split the tests in unit tests and integration tests. However the data structure on wikidata is stable, 
  // adding new fields or values must not break the code. Otherwise it is clear that we have a bug in the implementation. 
  //It is good to see such bugs as soon as possible. We may consider to have the same tests implemented both as unit (using local files), and integration (using http requests)tests  
  /**
   * SG: This is an integration test accessing data directly from wikidata  
   * @throws WikidataAccessException
   * @throws ZohoAccessException
   * @throws ParseException
   * @throws JAXBException
   * @throws IOException
   * @throws URISyntaxException
   */
  @Test
  public void dereferenceBnfTest() throws WikidataAccessException, ZohoAccessException,
      ParseException, JAXBException, IOException, URISyntaxException {

    //create Wikidata URI from ID 
    String uri =
        wikidataAccessService.buildOrganizationUri(TEST_WIKIDATA_ORGANIZATION_ID).toString();
    String testAcronym = TEST_ACRONYM;
    
    WikidataOrganization wikidataOrganization = dereferenceWikidataOrg(testAcronym, uri);
    Organization organizationImpl = convertToCoreOrganization(wikidataOrganization);
    
    //verify correct parsing of wikidata organization
    assertEquals(WIKIDATA_URL_BNF, wikidataOrganization.getOrganization().getAbout());
    assertEquals(TEST_COUNTRY, wikidataOrganization.getOrganization().getCountry());

    //verify correct conversion to Core Organization
    //in the future more labels might be available, therefore use in comparison and not equality
    assertTrue(38 <= organizationImpl.getPrefLabel().values().size());
    assertEquals(WIKIDATA_URL_BNF, organizationImpl.getAbout());
    assertEquals(testAcronym, organizationImpl.getEdmAcronym().get(Locale.FRENCH.getLanguage()).get(0));
    //disabled address until the locality issue is solved
//    assertNotNull(organizationImpl.getAddress());
//    assertNotNull(organizationImpl.getAddress().getVcardStreetAddress()); 
  }
  
  @Test
  public void dereferenceBLTest() throws WikidataAccessException, ZohoAccessException,
      ParseException, JAXBException, IOException, URISyntaxException {

    String acronym = "BL";
    WikidataOrganization wikidataOrganization = dereferenceWikidataOrg(acronym, WIKIDATA_URL_BL);
    Organization organizationImpl = convertToCoreOrganization(wikidataOrganization);
    
    //verify correct parsing of wikidata organization
    assertEquals(WIKIDATA_URL_BL, wikidataOrganization.getOrganization().getAbout());
    assertEquals("GB", wikidataOrganization.getOrganization().getCountry());

    //verify correct conversion to Core Organization
    //in the future more labels might be available, therefore use in comparison and not equality
    assertTrue(37 <= organizationImpl.getPrefLabel().values().size());
    assertEquals(WIKIDATA_URL_BL, organizationImpl.getAbout());
    assertTrue(organizationImpl.getEdmAcronym().get(Locale.ENGLISH.getLanguage()).contains(acronym));
    //disabled address until the locality issue is solved   
    //assertNotNull(organizationImpl.getAddress());
    //assertNotNull(organizationImpl.getAddress().getVcardStreetAddress()); 
    //the locality was disabled in the current version of the mapping, but it might come back in the future 
    //assertNotNull(organizationImpl.getAddress().getVcardLocality());
//    assertNotNull(organizationImpl.getAddress().getVcardCountryName());
  }
  
  @Test
  public void dereferenceNisvTest() throws WikidataAccessException, ZohoAccessException,
      ParseException, JAXBException, IOException, URISyntaxException {

    String acronym = "NIBG";
    WikidataOrganization wikidataOrganization = dereferenceWikidataOrg(acronym, WIKIDATA_URL_NISV);
    Organization organizationImpl = convertToCoreOrganization(wikidataOrganization);
    
    //verify correct parsing of wikidata organization
    assertEquals(WIKIDATA_URL_NISV, wikidataOrganization.getOrganization().getAbout());
    assertEquals("NL", wikidataOrganization.getOrganization().getCountry());

    //verify correct conversion to Core Organization
    //in the future more labels might be available, therefore use in comparison and not equality
    assertTrue(6 <= organizationImpl.getPrefLabel().values().size());
    assertEquals(WIKIDATA_URL_NISV, organizationImpl.getAbout());
    assertTrue(organizationImpl.getEdmAcronym().get("nl").contains(acronym));
    //disabled address until the locality issue is solved
    //assertNotNull(organizationImpl.getAddress());
    //assertNotNull(organizationImpl.getAddress().getVcardStreetAddress()); 
    //the locality was disabled in the current version of the mapping, but it might come back in the future 
    //    assertNotNull(organizationImpl.getAddress().getVcardLocality());
    //assertNotNull(organizationImpl.getAddress().getVcardCountryName());
  }

  private Organization convertToCoreOrganization(WikidataOrganization wikidataOrganization) {
    //convert Wikidata organization to OrganizationImpl object
    Organization organizationImpl = wikidataAccessService.toOrganizationImpl(wikidataOrganization);
    assertNotNull(organizationImpl);
    return organizationImpl;
  }

  private WikidataOrganization dereferenceWikidataOrg(String acronym, String uri)
      throws WikidataAccessException, IOException, URISyntaxException, FileNotFoundException,
      JAXBException {
    //dereference Wikidata URI
    String wikidataXml = wikidataAccessDao.getEntity(uri).toString();
    assertNotNull(wikidataXml);

    //write XML to a file (if it fails, it throws an exception). 
    File wikidataOutputFile = getDerefFile(acronym);
    wikidataAccessService.saveXmlToFile(wikidataXml, wikidataOutputFile);

    //read organization XML from file
    String savedWikidataXml = FileUtils.readFileToString(wikidataOutputFile, "UTF-8");
    WikidataOrganization wikidataOrganization = wikidataAccessDao.parse(savedWikidataXml);
    assertNotNull(wikidataOrganization);
    return wikidataOrganization;
  }

  /**
   * SG: This is a unit test using local files
   * @throws WikidataAccessException
   * @throws ZohoAccessException
   * @throws ParseException
   * @throws JAXBException
   * @throws IOException
   * @throws URISyntaxException
   */
  @Test
  public void dereferenceInsertFromFileTest() throws WikidataAccessException, ZohoAccessException,
      ParseException, JAXBException, IOException, URISyntaxException {

    // dereference Wikidata URI
    File wikidataTestManualInputFile = getClasspathFile(WIKIDATA_TEST_MANUAL_INPUT_FILE);
    String wikidataXml = FileUtils.readFileToString(wikidataTestManualInputFile, "UTF-8");
    assertNotNull(wikidataXml);

    //insert XML in Wikidata organization object 
    WikidataOrganization wikidataOrganization = wikidataAccessDao.parse(wikidataXml);
    assertNotNull(wikidataOrganization);
    assertEquals(WIKIDATA_URL_BNF, wikidataOrganization.getOrganization().getAbout());
    assertEquals(TEST_COUNTRY, wikidataOrganization.getOrganization().getCountry());

    Organization organizationImpl = convertToCoreOrganization(wikidataOrganization);
    //in the future more labels might be available, therefore use in comparison and not equality
    assertTrue(37 <= organizationImpl.getPrefLabel().values().size());
    assertTrue(25 <= organizationImpl.getAltLabel().values().size());
    assertTrue(2 <= organizationImpl.getEdmAcronym().size());
    
    assertEquals(WIKIDATA_URL_BNF, organizationImpl.getAbout());
    assertEquals(TEST_ACRONYM, organizationImpl.getEdmAcronym().get(Locale.FRENCH.getLanguage()).get(0));
    assertNotNull(organizationImpl.getFoafHomepage());
    assertNotNull(organizationImpl.getFoafLogo());
    assertNotNull(organizationImpl.getFoafPhone());
    assertNotNull(organizationImpl.getFoafMbox());
    assertNotNull(organizationImpl.getAddress());
    assertNotNull(organizationImpl.getAddress().getVcardCountryName());
    assertNotNull(organizationImpl.getAddress().getVcardLocality());
    assertNotNull(organizationImpl.getAddress().getVcardPostalCode());
    assertNotNull(organizationImpl.getAddress().getVcardPostOfficeBox());
    assertNotNull(organizationImpl.getAddress().getVcardStreetAddress());
  }

}
