package eu.europeana.indexing.solr.property;

import java.util.Collection;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.AbstractEdmEntity;

public abstract class PropertySolrCreator<T extends AbstractEdmEntity> {

  public void addAllToDocument(SolrInputDocument document, Collection<? extends T> properties) {
    if (properties != null) {
      for (T property : properties) {
        addToDocument(document, property);
      }
    }
  }

  public abstract void addToDocument(SolrInputDocument document, T property);

}
