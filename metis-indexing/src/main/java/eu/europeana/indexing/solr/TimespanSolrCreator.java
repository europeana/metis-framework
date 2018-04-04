package eu.europeana.indexing.solr;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Timespan;

/**
 * Generate Timespan SOLR fields from Mongo
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
public class TimespanSolrCreator {
  
  /**
   * Create SOLR fields from a Mongo concept
   * 
   * @param doc The solr document to modify
   * @param ts The timespan mongo entity to append
   */
  public void create(SolrInputDocument doc, Timespan ts) {
    SolrUtils.addValue(doc, EdmLabel.EDM_TIMESPAN, ts.getAbout());
    SolrUtils.addValues(doc, EdmLabel.TS_SKOS_PREF_LABEL, ts.getPrefLabel());
    SolrUtils.addValues(doc, EdmLabel.TS_SKOS_ALT_LABEL, ts.getAltLabel());
    SolrUtils.addValues(doc, EdmLabel.TS_SKOS_NOTE, ts.getNote());
    SolrUtils.addValues(doc, EdmLabel.TS_SKOS_HIDDENLABEL, ts.getHiddenLabel());
    SolrUtils.addValues(doc, EdmLabel.TS_SKOS_PREF_LABEL, ts.getOwlSameAs());
    SolrUtils.addValues(doc, EdmLabel.TS_DCTERMS_HASPART, ts.getDctermsHasPart());
    SolrUtils.addValues(doc, EdmLabel.TS_DCTERMS_ISPART_OF, ts.getIsPartOf());
    SolrUtils.addValues(doc, EdmLabel.TS_EDM_BEGIN, ts.getBegin());
    SolrUtils.addValues(doc, EdmLabel.TS_EDM_END, ts.getEnd());
  }
}
