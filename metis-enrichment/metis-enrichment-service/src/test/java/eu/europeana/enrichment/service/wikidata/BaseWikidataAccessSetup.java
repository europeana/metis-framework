package eu.europeana.enrichment.service.wikidata;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import eu.europeana.enrichment.service.WikidataAccessService;
import eu.europeana.enrichment.service.dao.WikidataAccessDao;
import eu.europeana.enrichment.service.exception.WikidataAccessException;
import eu.europeana.enrichment.service.zoho.BaseZohoAccessSetup;

public class BaseWikidataAccessSetup extends BaseZohoAccessSetup {

  protected WikidataAccessService wikidataAccessService;
  protected WikidataAccessDao wikidataAccessDao;

  /**
   * This method initializes classes needed for Wikidata related activities
   * 
   * @throws WikidataAccessException
   */
  protected void initWikidataAccessService()
      throws WikidataAccessException {
    wikidataAccessDao = new WikidataAccessDao();
    wikidataAccessService = new WikidataAccessService(wikidataAccessDao);
  }
  
  protected InputStream getDerefStream(String testAcronym) throws URISyntaxException, IOException {
    return BaseWikidataAccessSetup.class.getResourceAsStream(CONTENT_DIR+ testAcronym + ".deref.xml");
  }
 
}
