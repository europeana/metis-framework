package eu.europeana.indexing.record.v2;

import eu.europeana.corelib.solr.entity.ServiceImpl;
import eu.europeana.indexing.record.v2.property.MongoPropertyUpdater;

/**
 * Field updater for instances of {@link ServiceImpl}.
 */
public class ServiceUpdater extends AbstractIsolatedEdmEntityUpdater<ServiceImpl> {

  @Override
  protected Class<ServiceImpl> getObjectClass() {
    return ServiceImpl.class;
  }

  @Override
  protected void update(MongoPropertyUpdater<ServiceImpl> propertyUpdater) {
    propertyUpdater.updateArray("dctermsConformsTo", ServiceImpl::getDctermsConformsTo);
    propertyUpdater.updateArray("doapImplements", ServiceImpl::getDoapImplements);
  }
}
