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

  @Before
  public void setUp() throws Exception {
    super.initWikidataAccessService();
  }

  @After
  public void tearDown() throws Exception {}

  @Test
  public void dereferenceInsertTest() throws WikidataAccessException, ZohoAccessException,
      ParseException, JAXBException, IOException, URISyntaxException {

    /** create Wikidata URI from ID */
    String uri =
        wikidataAccessService.buildOrganizationUri(TEST_WIKIDATA_ORGANIZATION_ID).toString();

    /** dereference Wikidata URI */
    String wikidataXml = wikidataAccessDao.getEntity(uri).toString();
    assertNotNull(wikidataXml);

    /** write XML to a file */
    File wikidataOutputFile = getClasspathFile(WIKIDATA_TEST_OUTPUT_FILE);
    boolean isWritten = wikidataAccessService.saveXmlToFile(wikidataXml, wikidataOutputFile);
    assertTrue(isWritten == true);

    /** insert XML in Wikidata organization object */
    WikidataOrganization wikidataOrganization =
        wikidataAccessDao.parse(wikidataXml);
    assertNotNull(wikidataOrganization);
    assertEquals(wikidataOrganization.getOrganization().getAbout(), TEST_WIKIDATA_URL);
    assertEquals(wikidataOrganization.getOrganization().getCountry(), TEST_COUNTRY);

    /** read organization XML from file */
    File wikidataTestOutputFile = getClasspathFile(WIKIDATA_TEST_OUTPUT_FILE);
    String savedWikidataXml = FileUtils.readFileToString(wikidataTestOutputFile, "UTF-8"); 
    assertEquals(wikidataXml, savedWikidataXml);

    /** convert Wikidata organization to OrganizationImpl object */
    Organization organizationImpl = wikidataAccessService.toOrganizationImpl(wikidataOrganization);
    assertNotNull(organizationImpl);
    assertTrue(organizationImpl.getPrefLabel().values().size() == 37);
    assertEquals(organizationImpl.getAbout(), TEST_WIKIDATA_URL);
    assertTrue(organizationImpl.getEdmAcronym().get(Locale.FRENCH.getLanguage()).get(0)
        .equals(TEST_ACRONYM));
  }

}
