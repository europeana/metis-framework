package eu.europeana.indexing.record.v2;

import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.indexing.record.v2.property.MongoPropertyUpdater;

/**
 * Field updater for instances of {@link ProvidedCHOImpl}.
 */
public class ProvidedChoUpdater extends AbstractIsolatedEdmEntityUpdater<ProvidedCHOImpl> {

  @Override
  protected Class<ProvidedCHOImpl> getObjectClass() {
    return ProvidedCHOImpl.class;
  }

  @Override
  protected void update(MongoPropertyUpdater<ProvidedCHOImpl> propertyUpdater) {
    propertyUpdater.updateArray("owlSameAs", ProvidedCHOImpl::getOwlSameAs);
  }
}
