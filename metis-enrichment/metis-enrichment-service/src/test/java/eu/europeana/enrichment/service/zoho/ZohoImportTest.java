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
import eu.europeana.enrichment.api.external.model.zoho.ZohoOrganization;
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
public class ZohoImportTest extends BaseZohoAccessSetup {

	String mongoHost;
	int mongoPort;
	EntityService entityService;

	@Override
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
	    entityService.close();
	}

	@Test
	public void importOrganizationTest()
			throws ZohoAccessException, ParseException {
		
		ZohoOrganization org = zohoAccessService
				.getOrganization(TEST_ORGANIZATION_ID);
		assertNotNull(org);
		assertTrue(org.getCreated().getTime() > 0);
		assertTrue(org.getModified().getTime() > 0);

		Organization edmOrg = zohoAccessService.toEdmOrganization(org);
		OrganizationTermList termList = entityService.storeOrganization(edmOrg,
				org.getCreated(), org.getModified());
		assertNotNull(termList);

		assertEquals(edmOrg.getAbout(), termList.getCodeUri());
		assertEquals(termList.getCreated(), org.getCreated());
		assertEquals(termList.getModified(), org.getModified());
	}

	@Test
	public void getLastImportedDateTest() throws ZohoAccessException {
		ZohoOrganization org = zohoAccessService
				.getOrganization(TEST_ORGANIZATION_ID);
		assertNotNull(org);
		assertNotNull(org.getOrganizationName());
		
		OrganizationTermList termList = entityService
				.storeOrganization(zohoAccessService.toEdmOrganization(org), org.getCreated(), org.getModified());
		assertNotNull(termList);

		Date lastImportedDate = entityService.getLastOrganizationImportDate();
		assertNotNull(lastImportedDate);
		LOGGER.info("Last imported date: " + lastImportedDate.toString());
		assertEquals(lastImportedDate, termList.getModified());

	}
}
