package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.solr.entity.TimespanImpl;

/**
 * Field updater for instances of {@link TimespanImpl}.
 */
public class TimespanUpdater extends AbstractIsolatedEdmEntityUpdater<TimespanImpl> {

  @Override
  protected Class<TimespanImpl> getObjectClass() {
    return TimespanImpl.class;
  }

  @Override
  protected void update(MongoPropertyUpdater<TimespanImpl> propertyUpdater) {
    propertyUpdater.updateMap("begin", TimespanImpl::getBegin);
    propertyUpdater.updateMap("end", TimespanImpl::getEnd);
    propertyUpdater.updateMap("note", TimespanImpl::getNote);
    propertyUpdater.updateMap("altLabel", TimespanImpl::getAltLabel);
    propertyUpdater.updateMap("prefLabel", TimespanImpl::getPrefLabel);
    propertyUpdater.updateMap("isPartOf", TimespanImpl::getIsPartOf);
    propertyUpdater.updateMap("dctermsHasPart", TimespanImpl::getDctermsHasPart);
    propertyUpdater.updateArray("owlSameAs", TimespanImpl::getOwlSameAs);
  }
}
