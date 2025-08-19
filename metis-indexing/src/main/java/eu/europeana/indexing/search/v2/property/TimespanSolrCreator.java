package eu.europeana.indexing.search.v2.property;

import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.Timespan;
import eu.europeana.indexing.common.persistence.solr.v2.SolrV2Field;

/**
 * Property Solr Creator for 'edm:TimeSpan' tags.
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
public class TimespanSolrCreator implements PropertySolrCreator<Timespan> {

  @Override
  public void addToDocument(SolrInputDocument doc, Timespan ts) {
    SolrPropertyUtils.addValue(doc, SolrV2Field.EDM_TIMESPAN, ts.getAbout());
    SolrPropertyUtils.addValues(doc, SolrV2Field.TS_SKOS_PREF_LABEL, ts.getPrefLabel());
    SolrPropertyUtils.addValues(doc, SolrV2Field.TS_SKOS_ALT_LABEL, ts.getAltLabel());
    SolrPropertyUtils.addValues(doc, SolrV2Field.TS_SKOS_PREF_LABEL, ts.getOwlSameAs());
  }
}
