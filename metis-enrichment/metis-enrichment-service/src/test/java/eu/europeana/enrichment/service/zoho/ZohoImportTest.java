package eu.europeana.enrichment.service.zoho;

import java.util.Date;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.enrichment.api.internal.OrganizationTermList;
import eu.europeana.enrichment.service.EntityService;
import eu.europeana.enrichment.service.exception.ZohoAccessException;
import static org.junit.Assert.*;

public class ZohoImportTest extends BaseZohoAccessTest{

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
	
	@Test
	public void importOrganizationTest() throws ZohoAccessException{
		Date now = new Date();
		Organization org = zohoAccessService.getOrganization(TEST_ORGANIZATION_ID);
		assertNotNull(org);
		assertTrue(org.getCreated().getTime() > 0);
		assertTrue(org.getCreated().getTime() > 0);
		OrganizationTermList termList = entityService.storeOrganization(org);
		assertNotNull(termList);
		
		assertEquals(org.getAbout(), termList.getCodeUri());
		assertEquals(termList.getCreated(), termList.getModified());
		assertTrue(termList.getCreated().getTime() > now.getTime());
	}
}
