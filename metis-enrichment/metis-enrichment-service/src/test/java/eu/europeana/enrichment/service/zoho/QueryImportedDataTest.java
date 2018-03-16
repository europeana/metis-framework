package eu.europeana.enrichment.service.zoho;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.enrichment.api.internal.OrganizationTermList;
import eu.europeana.enrichment.service.EntityService;
import eu.europeana.enrichment.service.exception.ZohoAccessException;

/**
 * This is a test for retrieval of imported organizations by e.g. last
 * modified date.
 * @author GrafR
 *
 */
public class QueryImportedDataTest extends BaseZohoAccessTest{

	final String TEST_ORGANIZATION_ID_2 = "1482250000006050056";
	
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
	public void getLastImportedDateTest() throws ZohoAccessException{
		Date now = new Date();		
		Organization org = zohoAccessService.getOrganization(TEST_ORGANIZATION_ID_2);
		assertNotNull(org);
		assertTrue(org.getCreated().getTime() > 0);
		assertTrue(org.getCreated().getTime() > 0);
		OrganizationTermList termList = entityService.storeOrganization(org);
		assertNotNull(termList);
		
		Date lastImportedDate = entityService.getLastModifiedDate();
		assertNotNull(lastImportedDate);		
		LOGGER.info("Last imported date: " + lastImportedDate.toString());
		assertTrue(lastImportedDate.getTime() > now.getTime());
	}
}
