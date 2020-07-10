package eu.europeana.enrichment.api.internal;

import dev.morphia.annotations.Entity;
import eu.europeana.corelib.solr.entity.OrganizationImpl;

/**
 * OrganizationImpl specific MongoTermList
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-15
 */
@Entity("TermList")
public class OrganizationTermList extends MongoTermList<OrganizationImpl> {

  @Override
  public OrganizationImpl getRepresentation() {
    return representation;
  }

  @Override
  public void setRepresentation(OrganizationImpl representation) {
    this.representation = representation;
  }

}
