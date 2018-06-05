package eu.europeana.enrichment.service.wikidata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.File;
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

  final String TEST_WIKIDATA_URL =
      "http://www.wikidata.org/entity/Q" + TEST_WIKIDATA_ORGANIZATION_ID;
  final String TEST_ACRONYM = "BNF";
  final String TEST_COUNTRY = "FR";

  @Override
  @Before
  public void setUp() throws Exception {
    super.initWikidataAccessService();
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void dereferenceInsertTest() throws WikidataAccessException, ZohoAccessException,
      ParseException, JAXBException, IOException, URISyntaxException {

    //create Wikidata URI from ID 
    String uri =
        wikidataAccessService.buildOrganizationUri(TEST_WIKIDATA_ORGANIZATION_ID).toString();

    //dereference Wikidata URI
    String wikidataXml = wikidataAccessDao.getEntity(uri).toString();
    assertNotNull(wikidataXml);

    //write XML to a file (if it fails, it throws an exception). 
    File wikidataOutputFile = getClasspathFile(WIKIDATA_TEST_OUTPUT_FILE);
    wikidataAccessService.saveXmlToFile(wikidataXml, wikidataOutputFile);

    //insert XML in Wikidata organization object 
    WikidataOrganization wikidataOrganization =
        wikidataAccessDao.parse(wikidataXml);
    assertNotNull(wikidataOrganization);
    assertEquals(wikidataOrganization.getOrganization().getAbout(), TEST_WIKIDATA_URL);
    assertEquals(wikidataOrganization.getOrganization().getCountry(), TEST_COUNTRY);

    //read organization XML from file
    File wikidataTestOutputFile = getClasspathFile(WIKIDATA_TEST_OUTPUT_FILE);
    String savedWikidataXml = FileUtils.readFileToString(wikidataTestOutputFile, "UTF-8"); 
    assertEquals(wikidataXml, savedWikidataXml);

    //convert Wikidata organization to OrganizationImpl object
    Organization organizationImpl = wikidataAccessService.toOrganizationImpl(wikidataOrganization);
    assertNotNull(organizationImpl);
    //assertTrue(organizationImpl.getPrefLabel().values().size() == 37);
    assertEquals(organizationImpl.getAbout(), TEST_WIKIDATA_URL);
    assertTrue(organizationImpl.getEdmAcronym().get(Locale.FRENCH.getLanguage()).get(0)
        .equals(TEST_ACRONYM));
  }

  @Test
  public void dereferenceInsertFromFileTest() throws WikidataAccessException, ZohoAccessException,
      ParseException, JAXBException, IOException, URISyntaxException {

    // dereference Wikidata URI
    File wikidataTestManualInputFile = getClasspathFile(WIKIDATA_TEST_MANUAL_INPUT_FILE);
    String wikidataXml = FileUtils.readFileToString(wikidataTestManualInputFile, "UTF-8");
    assertNotNull(wikidataXml);

    //insert XML in Wikidata organization object 
    WikidataOrganization wikidataOrganization =
        wikidataAccessDao.parse(wikidataXml);
    assertNotNull(wikidataOrganization);
    assertEquals(TEST_WIKIDATA_URL, wikidataOrganization.getOrganization().getAbout());
    assertEquals(TEST_COUNTRY, wikidataOrganization.getOrganization().getCountry());

    // convert Wikidata organization to OrganizationImpl object
    Organization organizationImpl = wikidataAccessService.toOrganizationImpl(wikidataOrganization);
    assertNotNull(organizationImpl);
    assertEquals(37, organizationImpl.getPrefLabel().values().size());
    assertEquals(25, organizationImpl.getAltLabel().values().size());
    assertEquals(TEST_WIKIDATA_URL, organizationImpl.getAbout());
    assertEquals(TEST_ACRONYM, organizationImpl.getEdmAcronym().get(Locale.FRENCH.getLanguage()).get(0));
    assertEquals(2, organizationImpl.getEdmAcronym().size());
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
