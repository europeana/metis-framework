package eu.europeana.indexing.mongo.property;

import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.storage.MongoServer;

/**
 * Field updater for instances of {@link PlaceImpl}.
 */
public class PlaceUpdater implements PropertyMongoUpdater<PlaceImpl> {

  @Override
  public PlaceImpl update(PlaceImpl place, PlaceImpl newPlace, MongoServer mongoServer) {
    Query<PlaceImpl> query = mongoServer.getDatastore().createQuery(PlaceImpl.class).field("about")
        .equal(place.getAbout());
    UpdateOperations<PlaceImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(PlaceImpl.class);
    final UpdateTrigger updateTrigger = new UpdateTrigger();
    FieldUpdateUtils.updateMap(updateTrigger, place, newPlace, "note", ops, PlaceImpl::getNote,
        PlaceImpl::setNote);
    FieldUpdateUtils.updateMap(updateTrigger, place, newPlace, "altLabel", ops,
        PlaceImpl::getAltLabel, PlaceImpl::setAltLabel);
    FieldUpdateUtils.updateMap(updateTrigger, place, newPlace, "prefLabel", ops,
        PlaceImpl::getPrefLabel, PlaceImpl::setPrefLabel);
    FieldUpdateUtils.updateMap(updateTrigger, place, newPlace, "isPartOf", ops,
        PlaceImpl::getIsPartOf, PlaceImpl::setIsPartOf);
    FieldUpdateUtils.updateMap(updateTrigger, place, newPlace, "dcTermsHasPart", ops,
        PlaceImpl::getDcTermsHasPart, PlaceImpl::setDcTermsHasPart);
    FieldUpdateUtils.updateArray(updateTrigger, place, newPlace, "owlSameAs", ops,
        PlaceImpl::getOwlSameAs, PlaceImpl::setOwlSameAs);
    if (newPlace.getLatitude() != null) {
      if (place.getLatitude() == null || !place.getLatitude().equals(newPlace.getLatitude())) {
        ops.set("latitude", newPlace.getLatitude());
        place.setLatitude(newPlace.getLatitude());
        updateTrigger.triggerUpdate();
      }
    } else {
      if (place.getLatitude() != null) {
        ops.unset("latitude");
        place.setLatitude(newPlace.getLatitude());
        updateTrigger.triggerUpdate();
      }
    }

    if (newPlace.getLongitude() != null) {
      if (place.getLongitude() == null || !place.getLongitude().equals(newPlace.getLongitude())) {
        ops.set("longitude", newPlace.getLongitude());
        place.setLongitude(newPlace.getLongitude());
        updateTrigger.triggerUpdate();
      }
    } else {
      if (place.getLongitude() != null) {
        ops.unset("longitude");
        place.setLongitude(null);
        updateTrigger.triggerUpdate();
      }
    }

    if (newPlace.getAltitude() != null) {
      if (place.getAltitude() == null || !place.getAltitude().equals(newPlace.getAltitude())) {
        ops.set("altitude", newPlace.getAltitude());
        place.setAltitude(newPlace.getAltitude());
        updateTrigger.triggerUpdate();
      }
    } else {
      if (place.getAltitude() != null) {
        ops.unset("altitude");
        place.setAltitude(newPlace.getAltitude());
        updateTrigger.triggerUpdate();
      }
    }
    if (updateTrigger.isUpdateTriggered()) {
      mongoServer.getDatastore().update(query, ops);
    }

    return place;
  }
}
