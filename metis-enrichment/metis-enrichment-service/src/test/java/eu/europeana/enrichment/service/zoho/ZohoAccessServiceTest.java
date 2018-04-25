package eu.europeana.enrichment.service.zoho;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.enrichment.api.external.model.zoho.ZohoOrganization;
import eu.europeana.enrichment.service.exception.ZohoAccessException;
import eu.europeana.metis.authentication.dao.ZohoApiFields;

/**
 * Disabled integration. Need to implement betamax with https connectivity for
 * Zoho
 * 
 * @author GordeaS
 *
 */
@Ignore
public class ZohoAccessServiceTest extends BaseZohoAccessSetup {

	@Before
	public void setUp() throws Exception {
		super.setUp();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void getOrganizationTest() throws ZohoAccessException {
		ZohoOrganization bnfZoho = zohoAccessService
				.getOrganization(TEST_ORGANIZATION_ID);

		Organization bnf = zohoAccessService.toEdmOrganization(bnfZoho);
		assertNotNull(bnf.getEdmAcronym());

		List<String> acronyms = bnf.getEdmAcronym().get("fr");
		assertEquals("BnF", acronyms.get(0));

		List<String> identifiers = bnf.getDcIdentifier().get("def");
		assertEquals(TEST_ORGANIZATION_ID, identifiers.get(0));

		LOGGER.info("BNF identifiers: " + identifiers);
		LOGGER.info("BNF about: " + bnf.getAbout());

		if (bnf.getEdmEuropeanaRole() != null) {
			Set<Entry<String, List<String>>> roles = bnf.getEdmEuropeanaRole()
					.entrySet();
			List<String> roleList = roles.iterator().next().getValue();
			for (String role : roleList) {
				LOGGER.info("Role: " + role);
			}
			
			assertTrue(roleList.size() > 0);
		}
		
	}

	@Test
	public void getOrganizationsTest() throws ZohoAccessException {
		List<ZohoOrganization> orgList = zohoAccessService.getOrganizations(1,
				5, null);

		assertNotNull(orgList);
		assertFalse(orgList.isEmpty());
		assertEquals(5, orgList.size());

		Organization org = zohoAccessService.toEdmOrganization(orgList.get(0));

		assertNotNull(org.getAbout());
		LOGGER.info("First entry about: " + org.getAbout());

		if (org.getEdmAcronym() != null) {
			Set<Entry<String, List<String>>> acronyms = org.getEdmAcronym()
					.entrySet();
			List<String> acronym = acronyms.iterator().next().getValue();
			LOGGER.info("First entry acronyms: " + acronym);
		}

		if (org.getPrefLabel() != null) {
			Set<Entry<String, List<String>>> labels = org.getPrefLabel()
					.entrySet();
			List<String> label = labels.iterator().next().getValue();
			LOGGER.info("First entry label: " + label);
		}

		Set<Entry<String, List<String>>> identifiers = org.getDcIdentifier()
				.entrySet();
		List<String> identifier = identifiers.iterator().next().getValue();
		LOGGER.info("First entry identifier: " + identifier);

	}

	@Test
	public void getFilteredOrganizationsTest() throws ZohoAccessException {

		Map<String, String> searchCriteria = new HashMap<String, String>();
		searchCriteria.put(ZohoApiFields.ORGANIZATION_ROLE, "Provider, Test, Aggregator");
		List<ZohoOrganization> orgList = zohoAccessService.getOrganizations(1,
				5, null, searchCriteria);

		assertNotNull(orgList);
		assertFalse(orgList.isEmpty());
		assertTrue(orgList.size() > 1);

		Organization org = zohoAccessService.toEdmOrganization(orgList.get(0));

		assertNotNull(org.getAbout());
		LOGGER.info("First entry about: " + org.getAbout());

		if (org.getEdmAcronym() != null) {
			Set<Entry<String, List<String>>> acronyms = org.getEdmAcronym()
					.entrySet();
			List<String> acronym = acronyms.iterator().next().getValue();
			LOGGER.info("First entry acronyms: " + acronym);
		}

		if (org.getPrefLabel() != null) {
			Set<Entry<String, List<String>>> labels = org.getPrefLabel()
					.entrySet();
			List<String> label = labels.iterator().next().getValue();
			LOGGER.info("First entry label: " + label);
		}

		Set<Entry<String, List<String>>> identifiers = org.getDcIdentifier()
				.entrySet();
		List<String> identifier = identifiers.iterator().next().getValue();
		LOGGER.info("First entry identifier: " + identifier);

	}

	@Test
	public void getOrganizationsSizeOneTest() throws ZohoAccessException {
		List<ZohoOrganization> orgList = zohoAccessService.getOrganizations(1,
				1, null);

		assertNotNull(orgList);
		assertFalse(orgList.isEmpty());
		assertEquals(1, orgList.size());

		ZohoOrganization org = orgList.get(0);
		assertNotNull(org.getZohoId());
		LOGGER.info("First entry about: " + org.getZohoId());
	}

	@Test
	public void getOrganizationsModifiedTest() throws ZohoAccessException {
		List<ZohoOrganization> orgList = zohoAccessService.getOrganizations(1,
				5, null);
		// by default it seems that the records are ordered by lastModified asc
		ZohoOrganization thirdOrg = orgList.get(2);

		Date modifiedDate = new Date(thirdOrg.getModified().getTime());

		orgList = zohoAccessService.getOrganizations(1, 5, modifiedDate);
		assertNotNull(orgList);
		assertFalse(orgList.isEmpty());

		ZohoOrganization org = orgList.get(0);
		assertEquals(org.getZohoId(), thirdOrg.getZohoId());
	}
}
