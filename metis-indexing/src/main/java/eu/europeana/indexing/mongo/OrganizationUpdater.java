package eu.europeana.indexing.mongo;

import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.indexing.mongo.property.MongoPropertyUpdater;
import eu.europeana.indexing.mongo.property.RootAboutWrapper;

/**
 * Field updater for instances of {@link eu.europeana.corelib.solr.entity.OrganizationImpl}.
 */
public class OrganizationUpdater extends AbstractEdmEntityUpdater<OrganizationImpl, RootAboutWrapper> {

  @Override
  protected Class<OrganizationImpl> getObjectClass() {
    return OrganizationImpl.class;
  }

  @Override
  protected void update(MongoPropertyUpdater<OrganizationImpl> propertyUpdater,
      RootAboutWrapper ancestorInformation) {
    propertyUpdater.updateMap("prefLabel", OrganizationImpl::getPrefLabel);
  }
}
