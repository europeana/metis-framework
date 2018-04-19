package eu.europeana.enrichment.service.zoho;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.enrichment.service.EntityConverterUtils;
import eu.europeana.enrichment.service.exception.EntityConverterException;
import eu.europeana.metis.authentication.dao.ZohoAccessClientDao;

public abstract class BaseZohoAccessTest {

  public final String CONTENT_DIR = "/content/";
  public final String ZOHO_TEST_INPUT_FILE = CONTENT_DIR + "bnf-zoho.json";
  public final String WIKIDATA_TEST_INPUT_FILE = CONTENT_DIR + "bnf-wikidata.xml";
  public final String ORGANIZATION_IMPL_TEST_OUTPUT_FILE = CONTENT_DIR + "bnf-organization-impl-output.json";
  public final String ORGANIZATION_IMPL_TEST_EXPECTED_FILE = CONTENT_DIR + "bnf-organization-impl-expected.json";
  public final String WIKIDATA_TEST_OUTPUT_FILE = CONTENT_DIR + "test.out";
  public final String WIKIDATA_ORGANIZATION_XSLT_TEMPLATE = CONTENT_DIR + "wkd2org.xsl";
  public final String TEST_WIKIDATA_ORGANIZATION_ID = "193563";

  
  protected ZohoAccessService zohoAccessService;
  ZohoAccessClientDao zohoAccessClientDao;

  EntityConverterUtils entityConverterUtils = new EntityConverterUtils();
  
  public EntityConverterUtils getEntityConverterUtils() {
    return entityConverterUtils;
  }
  
  final Logger LOGGER = LoggerFactory.getLogger(getClass());
  protected final String TEST_ORGANIZATION_ID = "1482250000002112001";

  public void setUp() throws Exception {
    // TODO use constant for authentication properties, if possible in a
    // common interface an reuse it everywhere
    String propertiesFile = "/authentication.properties";
    Properties appProps = loadProperties(propertiesFile);
    // TODO use constants for property keys
    zohoAccessClientDao = new ZohoAccessClientDao(appProps.getProperty("zoho.base.url"),
        appProps.getProperty("zoho.authentication.token"));
    zohoAccessService = new ZohoAccessService(zohoAccessClientDao);
  }

  protected Properties loadProperties(String propertiesFile)
      throws URISyntaxException, IOException, FileNotFoundException {
    Properties appProps = new Properties();
    
    File propsFile = getClasspathFile(propertiesFile);
    //fall back to template file for travis tests
    if(!propsFile.exists()){
    	String templateFile = propertiesFile + ".template";
		LOGGER.warn("{}", "The peroperties file is not available, using template: " + templateFile);
    	propsFile = getClasspathFile(templateFile);
    }	
    		
	appProps.load(new FileInputStream(propsFile));
    return appProps;
  }

  /**
   * This method loads content from a file for given file name.
   * @param fileName
   * @return the content file
   * @throws URISyntaxException
   * @throws IOException
   * @throws FileNotFoundException
   */
  protected File getClasspathFile(String fileName)
      throws URISyntaxException, IOException, FileNotFoundException {
    URI fileLocation = getClass().getResource(fileName).toURI();
    return (new File(fileLocation));
  }

  /**
   * This method saves organization content to a given file.
   * 
   * @param content
   * @param contentFile
   * @throws IOException
   * @throws EntityConverterException
   */
  public void writeToFile(String content, File contentFile)
      throws IOException, EntityConverterException {
    BufferedWriter out = new BufferedWriter(new FileWriter(contentFile));
    try {
      out.write(content);
    } catch (IOException e) {
      throw new EntityConverterException(
          EntityConverterException.COULD_NOT_BE_WRITTEN_TO_FILE_ERROR, e);
    } finally {
      out.close();
    }
  }
}
