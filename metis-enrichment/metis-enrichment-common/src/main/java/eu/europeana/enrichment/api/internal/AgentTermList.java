package eu.europeana.enrichment.api.internal;

import dev.morphia.annotations.Entity;
import eu.europeana.corelib.solr.entity.AgentImpl;

/**
 * AgentImpl specific MongoTermList
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
@Entity("TermList")
public class AgentTermList extends MongoTermList<AgentImpl> {

  @Override
  public AgentImpl getRepresentation() {
    return representation;
  }

  @Override
  public void setRepresentation(AgentImpl representation) {
    this.representation = representation;
  }

}
