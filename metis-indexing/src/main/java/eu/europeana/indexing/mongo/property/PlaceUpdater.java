package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.solr.entity.PlaceImpl;

/**
 * Field updater for instances of {@link PlaceImpl}.
 */
public class PlaceUpdater extends AbstractIsolatedEdmEntityUpdater<PlaceImpl> {

  @Override
  protected Class<PlaceImpl> getObjectClass() {
    return PlaceImpl.class;
  }

  @Override
  protected void update(MongoPropertyUpdater<PlaceImpl> propertyUpdater) {
    propertyUpdater.updateMap("note", PlaceImpl::getNote);
    propertyUpdater.updateMap("altLabel", PlaceImpl::getAltLabel);
    propertyUpdater.updateMap("prefLabel", PlaceImpl::getPrefLabel);
    propertyUpdater.updateMap("isPartOf", PlaceImpl::getIsPartOf);
    propertyUpdater.updateMap("dcTermsHasPart", PlaceImpl::getDcTermsHasPart);
    propertyUpdater.updateArray("owlSameAs", PlaceImpl::getOwlSameAs);
    propertyUpdater.updateObject("latitude", PlaceImpl::getLatitude);
    propertyUpdater.updateObject("longitude", PlaceImpl::getLongitude);
    propertyUpdater.updateObject("altitude", PlaceImpl::getAltitude);
  }
}
