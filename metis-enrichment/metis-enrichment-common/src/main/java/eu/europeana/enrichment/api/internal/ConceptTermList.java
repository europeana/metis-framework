package eu.europeana.enrichment.api.internal;

import dev.morphia.annotations.Entity;
import eu.europeana.corelib.solr.entity.ConceptImpl;

/**
 * ConceptImpl specific MongoTermList
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
@Entity("TermList")
public class ConceptTermList extends MongoTermList<ConceptImpl> {

  @Override
  public ConceptImpl getRepresentation() {
    return representation;
  }

  @Override
  public void setRepresentation(ConceptImpl representation) {
    this.representation = representation;
  }

}
