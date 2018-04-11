package eu.europeana.indexing.solr.property;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Timespan;
import eu.europeana.indexing.solr.EdmLabel;

/**
 * Property Solr Creator for 'edm:TimeSpan' tags.
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
public class TimespanSolrCreator implements PropertySolrCreator<Timespan> {

  @Override
  public void addToDocument(SolrInputDocument doc, Timespan ts) {
    SolrPropertyUtils.addValue(doc, EdmLabel.EDM_TIMESPAN, ts.getAbout());
    SolrPropertyUtils.addValues(doc, EdmLabel.TS_SKOS_PREF_LABEL, ts.getPrefLabel());
    SolrPropertyUtils.addValues(doc, EdmLabel.TS_SKOS_ALT_LABEL, ts.getAltLabel());
    SolrPropertyUtils.addValues(doc, EdmLabel.TS_SKOS_NOTE, ts.getNote());
    SolrPropertyUtils.addValues(doc, EdmLabel.TS_SKOS_HIDDENLABEL, ts.getHiddenLabel());
    SolrPropertyUtils.addValues(doc, EdmLabel.TS_SKOS_PREF_LABEL, ts.getOwlSameAs());
    SolrPropertyUtils.addValues(doc, EdmLabel.TS_DCTERMS_HASPART, ts.getDctermsHasPart());
    SolrPropertyUtils.addValues(doc, EdmLabel.TS_DCTERMS_ISPART_OF, ts.getIsPartOf());
    SolrPropertyUtils.addValues(doc, EdmLabel.TS_EDM_BEGIN, ts.getBegin());
    SolrPropertyUtils.addValues(doc, EdmLabel.TS_EDM_END, ts.getEnd());
  }
}
