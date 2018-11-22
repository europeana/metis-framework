package eu.europeana.enrichment.service.wikidata;

import eu.europeana.enrichment.service.WikidataAccessService;
import eu.europeana.enrichment.service.dao.WikidataAccessDao;
import eu.europeana.enrichment.service.exception.WikidataAccessException;
import eu.europeana.enrichment.service.zoho.BaseZohoAccessSetup;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

public class BaseWikidataAccessSetup extends BaseZohoAccessSetup {

  WikidataAccessService wikidataAccessService;
  WikidataAccessDao wikidataAccessDao;

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
  
  File getDerefFile(String testAcronym) throws URISyntaxException, IOException {
    File contentDir = getClasspathFile(CONTENT_DIR);
    File derefFile = new File(contentDir, testAcronym + ".deref.xml");
    if(!derefFile.exists())
      derefFile.createNewFile();
    
    return derefFile;
  }
}
