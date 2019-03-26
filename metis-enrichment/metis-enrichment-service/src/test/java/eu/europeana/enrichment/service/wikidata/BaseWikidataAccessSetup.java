package eu.europeana.enrichment.service.wikidata;

import eu.europeana.enrichment.service.WikidataAccessService;
import eu.europeana.enrichment.service.dao.WikidataAccessDao;
import eu.europeana.enrichment.service.exception.WikidataAccessException;

public class BaseWikidataAccessSetup{

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
 
}
