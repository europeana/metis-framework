package eu.europeana.enrichment.service.zoho;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.enrichment.service.exception.ZohoAccessException;
import eu.europeana.metis.authentication.dao.ZohoAccessClientDao;

public class ZohoAccessServiceTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(ZohoAccessServiceTest.class);

	ZohoAccessService zohoAccessService;
	private final String TEST_ORGANIZATION_ID = "1482250000002112001";
	private ZohoAccessClientDao zohoAccessClientDao;

	@Before
	public void setUp() throws Exception {
		Properties appProps = new Properties();
		//TODO use constant for authentication properties, if possible in a common interface an reuse it everywhere
		URI propLocation = getClass().getResource("/authentication.properties"). toURI();
		appProps.load(new FileInputStream(new File(propLocation)));
		//TODO use constants for property keys
		zohoAccessClientDao = new ZohoAccessClientDao(appProps.getProperty("zoho.base.url"),
				appProps.getProperty("zoho.base.authentication.token"));
		zohoAccessService = new ZohoAccessService(zohoAccessClientDao);
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
	}
	
	@Test
	public void getOrganizationsTest() throws ZohoAccessException {
		List<Organization> orgList = zohoAccessService.getOrganizations(1, 5);
		
		assertNotNull(orgList);
		assertEquals(5, orgList.size());
		
		Organization org = orgList.get(0);
		
		assertNotNull(org.getEdmAcronym());
		
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
