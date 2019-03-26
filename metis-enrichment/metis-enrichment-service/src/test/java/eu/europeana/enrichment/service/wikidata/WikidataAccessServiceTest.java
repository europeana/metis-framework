package eu.europeana.enrichment.service.wikidata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.Locale;
import javax.xml.bind.JAXBException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.enrichment.api.external.model.WikidataOrganization;
import eu.europeana.enrichment.service.exception.WikidataAccessException;
import eu.europeana.enrichment.service.exception.ZohoAccessException;
import eu.europeana.enrichment.service.zoho.BaseZohoAccessSetup;

public class WikidataAccessServiceTest extends BaseZohoAccessSetup{

  @Override
  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
  }
  
  @Test
  public void dereferenceBnfTest() throws WikidataAccessException, ZohoAccessException,
      ParseException, JAXBException, IOException, URISyntaxException {

    String bnfDerefFile = CONTENT_DIR + TEST_ACRONYM + ".deref.v1.xml"; 
    InputStream wikidataOrgAsStream = getClass().getResourceAsStream(bnfDerefFile);
    // create Wikidata URI from ID
    
    WikidataOrganization wikidataOrganization = wikidataAccessDao.parse(wikidataOrgAsStream);
    assertNotNull(wikidataOrganization);
    
    Organization organizationImpl = wikidataAccessService.toOrganizationImpl(wikidataOrganization);
    assertNotNull(organizationImpl);
    
    // verify correct parsing of wikidata organization
    assertEquals(WIKIDATA_URL_BNF, wikidataOrganization.getOrganization().getAbout());
    assertEquals(TEST_COUNTRY, wikidataOrganization.getOrganization().getCountry());

    // verify correct conversion to Core Organization
    // in the future more labels might be available, therefore use in comparison and not equality
    assertEquals(39, organizationImpl.getPrefLabel().values().size());
    assertEquals(WIKIDATA_URL_BNF, organizationImpl.getAbout());
    assertEquals(TEST_ACRONYM,
        organizationImpl.getEdmAcronym().get(Locale.FRENCH.getLanguage()).get(0));

    assertNotNull(organizationImpl.getFoafDepiction());
    assertNotNull(organizationImpl.getAddress());
    assertEquals("geo:48.833611111,2.375833333", organizationImpl.getAddress().getVcardHasGeo());
    // disabled address until the locality issue is solved
    // assertNotNull(organizationImpl.getAddress());
    // assertNotNull(organizationImpl.getAddress().getVcardStreetAddress());
  }
}
