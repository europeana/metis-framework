package eu.europeana.enrichment.service.zoho;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.metis.authentication.dao.ZohoAccessClientDao;

public abstract class BaseZohoAccessTest {

	ZohoAccessService zohoAccessService;
	ZohoAccessClientDao zohoAccessClientDao;

	final Logger LOGGER = LoggerFactory.getLogger(getClass());
	final String TEST_ORGANIZATION_ID = "1482250000002112001";

	public void setUp() throws Exception {
		// TODO use constant for authentication properties, if possible in a
		// common interface an reuse it everywhere
		String propertiesFile = "/authentication.properties";
		Properties appProps = loadProperties(propertiesFile);
		// TODO use constants for property keys
		zohoAccessClientDao = new ZohoAccessClientDao(
				appProps.getProperty("zoho.base.url"),
				appProps.getProperty("zoho.authentication.token"));
		zohoAccessService = new ZohoAccessService(zohoAccessClientDao);
	}

	protected Properties loadProperties(String propertiesFile)
			throws URISyntaxException, IOException, FileNotFoundException {
		Properties appProps = new Properties();
		URI propLocation = getClass().getResource(propertiesFile).toURI();
		appProps.load(new FileInputStream(new File(propLocation)));
		return appProps;
	}
}
