package eu.europeana.enrichment.service.zoho;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.text.ParseException;
import java.util.Date;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.enrichment.api.internal.OrganizationTermList;
import eu.europeana.enrichment.service.EntityService;
import eu.europeana.enrichment.service.exception.ZohoAccessException;

/**
 * Disabled integration. Need to implement betamax with https connectivity for
 * Zoho
 * 
 * @author GordeaS
 *
 */
@Ignore
public class ZohoImportTest extends BaseZohoAccessTest {

	String mongoHost;
	int mongoPort;
	EntityService entityService;

	@Before
	public void setUp() throws Exception {
		super.setUp();
		Properties props = loadProperties("/metis.properties");
		mongoHost = props.getProperty("mongo.hosts");
		mongoPort = Integer.valueOf(props.getProperty("mongo.port"));
		entityService = new EntityService(mongoHost, mongoPort);
	}

	@After
	public void tearDown() throws Exception {
	}

	// @Test
	public void importOrganizationTest()
			throws ZohoAccessException, ParseException {
		Date now = new Date();

		Organization org = zohoAccessService
				.getOrganization(TEST_ORGANIZATION_ID);
		assertNotNull(org);
		assertTrue(org.getCreated().getTime() > 0);
		assertTrue(org.getModified().getTime() > 0);
		OrganizationTermList termList = entityService.storeOrganization(org);
		assertNotNull(termList);

		assertEquals(org.getAbout(), termList.getCodeUri());
		assertEquals(termList.getCreated(), termList.getModified());
		assertTrue(termList.getCreated().getTime() > now.getTime());
	}

	@Test
	public void getLastImportedDateTest() throws ZohoAccessException {
		Date now = new Date();
		Organization org = zohoAccessService
				.getOrganization(TEST_ORGANIZATION_ID);
		assertNotNull(org);
		OrganizationTermList termList = entityService.storeOrganization(org);
		assertNotNull(termList);

		Date lastImportedDate = entityService.getLastOrganizationImportDate();
		assertNotNull(lastImportedDate);
		LOGGER.info("Last imported date: " + lastImportedDate.toString());
		assertTrue(lastImportedDate.getTime() > now.getTime());
		assertEquals(lastImportedDate, termList.getModified());

	}
}
