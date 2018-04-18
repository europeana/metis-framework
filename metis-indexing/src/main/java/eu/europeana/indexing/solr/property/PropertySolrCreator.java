package eu.europeana.indexing.solr.property;

import java.util.Collection;
import org.apache.solr.common.SolrInputDocument;
import eu.europeana.corelib.definitions.edm.entity.AbstractEdmEntity;

/**
 * Implementations of this interface create Solr properties for a given EDM property.
 * 
 * @author jochen
 *
 * @param <T> The type of the EDM property.
 */
public interface PropertySolrCreator<T extends AbstractEdmEntity> {

  /**
   * Convenience method that allows the data of multiple EMD properties to be added to the Solr
   * document. Calls {@link #addToDocument(SolrInputDocument, AbstractEdmEntity)} for each EDM
   * property.
   * 
   * @param document The Solr document to which to add the data.
   * @param properties The EDM properties from which to extract the data.
   */
  public default void addAllToDocument(SolrInputDocument document,
      Collection<? extends T> properties) {
    if (properties != null) {
      for (T property : properties) {
        addToDocument(document, property);
      }
    }
  }

  /**
   * This method takes an EDM property, converts it into Solr properties and adds those to the Solr
   * document.
   * 
   * @param document The Solr document to which to add the data.
   * @param property The EDM property from which to extract the data.
   */
  public abstract void addToDocument(SolrInputDocument document, T property);

}
