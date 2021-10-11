package eu.europeana.indexing.fullbean;

import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.metis.schema.jibx.Organization;
import java.util.function.Function;

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
