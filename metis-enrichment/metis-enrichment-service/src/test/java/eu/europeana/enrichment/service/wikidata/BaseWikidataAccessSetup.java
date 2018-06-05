package eu.europeana.enrichment.service.wikidata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import eu.europeana.enrichment.service.WikidataAccessService;
import eu.europeana.enrichment.service.dao.WikidataAccessDao;
import eu.europeana.enrichment.service.exception.WikidataAccessException;
import eu.europeana.enrichment.service.zoho.BaseZohoAccessSetup;

public class BaseWikidataAccessSetup extends BaseZohoAccessSetup {

  WikidataAccessService wikidataAccessService;
  WikidataAccessDao wikidataAccessDao;

  /**
   * This method initializes classes needed for Wikidata related activities
   * 
   * @throws WikidataAccessException
   * @throws IOException
   * @throws URISyntaxException
   * @throws FileNotFoundException
   */
  protected void initWikidataAccessService()
      throws WikidataAccessException, FileNotFoundException, URISyntaxException, IOException {
    wikidataAccessDao = new WikidataAccessDao();
    wikidataAccessService = new WikidataAccessService(wikidataAccessDao);
  }
  
  File getDerefFile(String testAcronym) throws FileNotFoundException, URISyntaxException, IOException {
    File contentDir = getClasspathFile(CONTENT_DIR);
    File derefFile = new File(contentDir, testAcronym + ".deref.xml");
    if(!derefFile.exists())
      derefFile.createNewFile();
    
    return derefFile;
  }
}
