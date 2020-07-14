package eu.europeana.enrichment.api.internal;

import dev.morphia.annotations.Entity;
import eu.europeana.corelib.solr.entity.PlaceImpl;

/**
 * PlaceImpl specific MongoTermList
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
@Entity("TermList")
public class PlaceTermList extends MongoTermList<PlaceImpl> {

  @Override
  public PlaceImpl getRepresentation() {
    return representation;
  }

  @Override
  public void setRepresentation(PlaceImpl representation) {
    this.representation = representation;
  }

}
