package eu.europeana.indexing.common.fullbean;

import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.metis.schema.jibx.Organization;
import java.util.function.Function;

/**
 * Converts a {@link Organization} from an {@link eu.europeana.metis.schema.jibx.RDF} to a
 * {@link OrganizationImpl} for a {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
final class OrganizationFieldInput implements Function<Organization, OrganizationImpl> {

  @Override
  public OrganizationImpl apply(Organization organizationType) {
    OrganizationImpl organization = new OrganizationImpl();
    organization.setAbout(organizationType.getAbout());
    organization.setPrefLabel(
        FieldInputUtils.createLiteralMapFromList(organizationType.getPrefLabelList()));
    return organization;
  }

}
