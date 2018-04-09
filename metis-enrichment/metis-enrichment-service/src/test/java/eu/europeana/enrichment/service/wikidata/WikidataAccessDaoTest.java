package eu.europeana.enrichment.service.wikidata;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import java.io.IOException;
import java.text.ParseException;
import java.util.Locale;
import javax.xml.bind.JAXBException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.enrichment.service.dao.WikidataAccessDao;
import eu.europeana.enrichment.service.dao.WikidataAccessService;
import eu.europeana.enrichment.service.dao.WikidataOrganization;
import eu.europeana.enrichment.service.exception.WikidataAccessException;
import eu.europeana.enrichment.service.exception.ZohoAccessException;

/**
 * Test class for Wikidata Access Dao.
 * 
 * @author GrafR
 *
 */
// @Ignore
public class WikidataAccessDaoTest { 

  final String WIKIDATA_TEST_OUTPUT_FILE = "test.out";
  final String TEST_WIKIDATA_ORGANIZATION_ID = "193563";
  final String TEST_WIKIDATA_URL = "http://www.wikidata.org/entity/Q" + TEST_WIKIDATA_ORGANIZATION_ID;
  final String TEST_ACRONYM = "BNF";
  final String TEST_COUNTRY = "FR";

  WikidataAccessService wikidataAccessService;

  final Logger LOGGER = LoggerFactory.getLogger(getClass());


  @Before
  public void setUp() throws Exception {
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

  @Test
  public void dereferenceInsertTest() throws WikidataAccessException, ZohoAccessException,
      ParseException, JAXBException, IOException {

    /** create Wikidata URI from ID */
    String uri =
        wikidataAccessService.buildOrganizationUriById(TEST_WIKIDATA_ORGANIZATION_ID).toString();
    
    /** dereference Wikidata URI */
    String wikidataXml =
        wikidataAccessService.getWikidataAccessDao().dereference(uri).toString();
    assertNotNull(wikidataXml);
    
    /** write XML to a file */
    boolean isWritten = wikidataAccessService.saveXmlToFile(wikidataXml, WIKIDATA_TEST_OUTPUT_FILE);
    assertTrue(isWritten == true);
    
    /** insert XML in Wikidata organization object */
    WikidataOrganization wikidataOrganization =
        wikidataAccessService.getWikidataAccessDao().parse(wikidataXml);
    assertNotNull(wikidataOrganization);
    assertEquals(wikidataOrganization.getOrganization().getAbout(), TEST_WIKIDATA_URL);
    assertEquals(wikidataOrganization.getOrganization().getCountry(), TEST_COUNTRY);
    
    /** read organization XML from file */
    String savedWikidataXml = wikidataAccessService.readXmlFile(WIKIDATA_TEST_OUTPUT_FILE);
    assertEquals(wikidataXml, savedWikidataXml);
    
    /** convert Wikidata organization to OrganizationImpl object */
    Organization organizationImpl = wikidataAccessService.toOrganizationImpl(wikidataOrganization);
    assertNotNull(organizationImpl);
    assertTrue(organizationImpl.getPrefLabel().values().size() == 37);
    assertEquals(organizationImpl.getAbout(), TEST_WIKIDATA_URL);
    assertTrue(organizationImpl.getEdmAcronym().get(Locale.FRENCH.getLanguage()).get(0).equals(TEST_ACRONYM));
  }

}
