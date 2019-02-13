package eu.europeana.enrichment.service.zoho;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.InputStream;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.enrichment.api.external.model.zoho.ZohoOrganization;
import eu.europeana.enrichment.service.exception.ZohoAccessException;

public class ZohoAccessServiceTest extends BaseZohoAccessSetup {

  @BeforeEach
  public void setUp() throws Exception {
    super.setUp();
  }

  @AfterEach
  public void tearDown() {
  }

  @Test
  public void getOrganizationTest() throws ZohoAccessException {

    InputStream zohoResponseStream = getClass().getResourceAsStream(ZOHO_TEST_INPUT_FILE);
    ZohoOrganization bnfZoho = zohoAccessService.getOrganizationFromStream(zohoResponseStream);

    Organization bnf = zohoAccessService.toEdmOrganization(bnfZoho);

    assertEquals(TEST_BNF_URL, bnf.getAbout());
    
    assertNotNull(bnf.getEdmAcronym());
    List<String> acronyms = bnf.getEdmAcronym().get("fr");
    assertEquals("BnF", acronyms.get(0));

    assertTrue(bnf.getPrefLabel().size() > 0);
    assertTrue(bnf.getAltLabel().size() > 0);
    
    List<String> identifiers = bnf.getDcIdentifier().get("def");
    assertEquals(TEST_ORGANIZATION_ID, identifiers.get(0));

    LOGGER.info("BNF identifiers: " + identifiers);
    LOGGER.info("BNF about: " + bnf.getAbout());

    if (bnf.getEdmEuropeanaRole() != null) {
      Set<Entry<String, List<String>>> roles = bnf.getEdmEuropeanaRole().entrySet();
      List<String> roleList = roles.iterator().next().getValue();
      for (String role : roleList) {
        LOGGER.info("Role: " + role);
      }

      assertTrue(roleList.size() > 0);
      assertTrue(roleList.contains("Data provider"));
    }
    
    assertNotNull(bnf.getAddress());
    assertEquals("Paris", bnf.getAddress().getVcardLocality());
  }
}
