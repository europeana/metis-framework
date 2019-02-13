package eu.europeana.enrichment.service.zoho;

//import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.enrichment.service.EntityConverterUtils;
import eu.europeana.enrichment.service.dao.ZohoV2AccessDao;
import eu.europeana.metis.authentication.dao.ZohoAccessClientDao;

public abstract class BaseZohoAccessSetup {

  public final String CONTENT_DIR = "/content/";
  public final String ZOHO_TEST_INPUT_FILE = CONTENT_DIR + "bnf-zoho.json";
  public final String WIKIDATA_TEST_INPUT_FILE = CONTENT_DIR + "bnf-wikidata.xml";
  public final String ORGANIZATION_IMPL_TEST_OUTPUT_FILE =
      CONTENT_DIR + "bnf-organization-impl-output.json";
  public final String ORGANIZATION_IMPL_TEST_EXPECTED_FILE =
      CONTENT_DIR + "bnf-organization-impl-expected.json";
  public final String WIKIDATA_TEST_OUTPUT_FILE = CONTENT_DIR + "test.out";
  public final String WIKIDATA_TEST_MANUAL_INPUT_FILE = CONTENT_DIR + "bnf-manual-test.xml";
  public final String TEST_WIKIDATA_ORGANIZATION_ID = "193563";
  protected final String TEST_ORGANIZATION_ID = "1482250000002112001";

  public final String TEST_BNF_URL = "http://data.europeana.eu/organization/1482250000002112001";
  public final String TEST_BNF_URL_TMP_1 =
      "http://data.europeana.eu/organization/1482250000002112222";
  public final String TEST_BNF_URL_TMP_2 =
      "http://data.europeana.eu/organization/1482250000002112223";
  public final String TEST_BNF_PREF_LABEL_1 = "Test BnF 1";
  public final String TEST_BNF_PREF_LABEL_2 = "Test BnF 2";

  public final String WIKIDATA_URL_BNF =
      "http://www.wikidata.org/entity/Q" + TEST_WIKIDATA_ORGANIZATION_ID;
  public final String WIKIDATA_URL_BL = "http://www.wikidata.org/entity/Q23308";

  public final String WIKIDATA_URL_SSA_REDIRECT = "http://www.wikidata.org/entity/Q27490300";
  public final String WIKIDATA_URL_SSA = "http://www.wikidata.org/entity/Q2256734";
  public final String WIKIDATA_URL_NISV = "http://www.wikidata.org/entity/Q1131877";
  public final String TEST_ACRONYM = "BNF";
  public final String TEST_COUNTRY = "FR";

  protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

  protected ZohoAccessService zohoAccessService;
  ZohoAccessClientDao zohoAccessClientDao;
  ZohoV2AccessDao zohoV2AccessDao;

  EntityConverterUtils entityConverterUtils = new EntityConverterUtils();

  public void setUp() throws Exception {
    // TODO use constant for authentication properties, if possible in a
    // common interface an reuse it everywhere
    String propertiesFile = "/authentication.properties";
    Properties appProps = loadProperties(propertiesFile);
    // TODO use constants for property keys
    zohoAccessClientDao = new ZohoAccessClientDao(appProps.getProperty("zoho.base.url"),
        appProps.getProperty("zoho.authentication.token"));
    zohoV2AccessDao = new ZohoV2AccessDao(appProps.getProperty("zoho.base.url.v2"),
        appProps.getProperty("zoho.authentication.token"));
    zohoAccessService = new ZohoAccessService(zohoAccessClientDao, zohoV2AccessDao);
  }

  public EntityConverterUtils getEntityConverterUtils() {
    return entityConverterUtils;
  }


  protected Properties loadProperties(String propertiesFile)
      throws URISyntaxException, IOException {

    Properties appProps = new Properties();
    URL propsResource = BaseZohoAccessSetup.class.getResource(propertiesFile);
    InputStream stream;

    if (propsResource != null) {
      stream = BaseZohoAccessSetup.class.getResourceAsStream(propertiesFile);
    } else {
      // fallback for travis tests
      String templateFile = propertiesFile + ".template";
      LOGGER.warn("{}", "The properties file is not available, using template: " + templateFile);
      stream = BaseZohoAccessSetup.class.getResourceAsStream(templateFile);
    }

    appProps.load(stream);
    return appProps;
  }

}
