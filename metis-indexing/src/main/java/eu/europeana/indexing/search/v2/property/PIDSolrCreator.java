package eu.europeana.indexing.search.v2.property;

import eu.europeana.corelib.definitions.edm.entity.PersistentIdentifier;
import eu.europeana.indexing.common.persistence.solr.v2.SolrV2Field;
import java.util.Collection;
import org.apache.solr.common.SolrInputDocument;

/**
 * The type Pid solr creator.
 */
public class PIDSolrCreator {

  /**
   * Add all to document.
   *
   * @param document the document
   * @param properties the properties
   */
  public void addAllToDocument(SolrInputDocument document, Collection<? extends PersistentIdentifier> properties) {
    if (properties != null) {
      SolrPropertyUtils.addValues(document,
          SolrV2Field.PID,
          properties.stream()
                    .map(PersistentIdentifier::getValue)
                    .toArray(String[]::new));
    }
  }

  /**
   * Add to document.
   *
   * @param doc the doc
   * @param persistentIdentifier the persistent identifier
   */
  public void addToDocument(SolrInputDocument doc, PersistentIdentifier persistentIdentifier) {
    SolrPropertyUtils.addValue(doc, SolrV2Field.PID, persistentIdentifier.getValue());
  }
}
