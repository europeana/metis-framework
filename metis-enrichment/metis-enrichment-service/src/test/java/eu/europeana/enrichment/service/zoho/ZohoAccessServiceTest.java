package eu.europeana.enrichment.service.zoho;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.enrichment.service.exception.ZohoAccessException;

public class ZohoAccessServiceTest extends BaseZohoAccessTest{

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void getOrganizationTest() throws ZohoAccessException {
		Organization bnf = zohoAccessService.getOrganization(TEST_ORGANIZATION_ID);
		assertNotNull(bnf.getEdmAcronym());
		
		List<String> acronyms = bnf.getEdmAcronym().get("EN(English)");
		assertEquals("BnF", acronyms.get(0));
		
		List<String> identifiers = bnf.getDcIdentifier().get("EN(English)");
		assertEquals(TEST_ORGANIZATION_ID, identifiers.get(0));
		
		LOGGER.info("BNF identifiers: "+identifiers);
		LOGGER.info("BNF about: "+bnf.getAbout());
	}
	
	@Test
	public void getOrganizationsTest() throws ZohoAccessException {
		List<Organization> orgList = zohoAccessService.getOrganizations(1, 5);
		
		assertNotNull(orgList);
		assertEquals(5, orgList.size());
		
		Organization org = orgList.get(0);
		
		assertNotNull(org.getAbout());
		LOGGER.info("First entry about: "+ org.getAbout());
		
		Set<Entry<String, List<String>>> acronyms = org.getEdmAcronym().entrySet();
		List<String> acronym = acronyms.iterator().next().getValue();
		LOGGER.info("First entry acronyms: "+acronym);
		
		Set<Entry<String, List<String>>> labels = org.getPrefLabel().entrySet();
		List<String> label = labels.iterator().next().getValue();
		LOGGER.info("First entry label: "+label);
		
		Set<Entry<String, List<String>>> identifiers = org.getDcIdentifier().entrySet();
		List<String> identifier = identifiers.iterator().next().getValue();
		LOGGER.info("First entry identifier: "+identifier);	
		
	}
}
