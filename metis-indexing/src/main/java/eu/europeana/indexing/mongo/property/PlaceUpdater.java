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
    boolean update = false;
    update = FieldUpdateUtils.updateMap(place, newPlace, "note", ops, PlaceImpl::getNote,
        PlaceImpl::setNote) || update;
    update = FieldUpdateUtils.updateMap(place, newPlace, "altLabel", ops, PlaceImpl::getAltLabel,
        PlaceImpl::setAltLabel) || update;
    update = FieldUpdateUtils.updateMap(place, newPlace, "prefLabel", ops, PlaceImpl::getPrefLabel,
        PlaceImpl::setPrefLabel) || update;
    update = FieldUpdateUtils.updateMap(place, newPlace, "isPartOf", ops, PlaceImpl::getIsPartOf,
        PlaceImpl::setIsPartOf) || update;
    update = FieldUpdateUtils.updateMap(place, newPlace, "dcTermsHasPart", ops,
        PlaceImpl::getDcTermsHasPart, PlaceImpl::setDcTermsHasPart) || update;
    update = FieldUpdateUtils.updateArray(place, newPlace, "owlSameAs", ops,
        PlaceImpl::getOwlSameAs, PlaceImpl::setOwlSameAs) || update;
    if (newPlace.getLatitude() != null) {
      if (place.getLatitude() == null || !place.getLatitude().equals(newPlace.getLatitude())) {
        ops.set("latitude", newPlace.getLatitude());
        place.setLatitude(newPlace.getLatitude());
        update = true;
      }
    } else {
      if (place.getLatitude() != null) {
        ops.unset("latitude");
        place.setLatitude(newPlace.getLatitude());
        update = true;
      }
    }

    if (newPlace.getLongitude() != null) {
      if (place.getLongitude() == null || !place.getLongitude().equals(newPlace.getLongitude())) {
        ops.set("longitude", newPlace.getLongitude());
        place.setLongitude(newPlace.getLongitude());
        update = true;
      }
    } else {
      if (place.getLongitude() != null) {
        ops.unset("longitude");
        place.setLongitude(null);
        update = true;
      }
    }

    if (newPlace.getAltitude() != null) {
      if (place.getAltitude() == null || !place.getAltitude().equals(newPlace.getAltitude())) {
        ops.set("altitude", newPlace.getAltitude());
        place.setAltitude(newPlace.getAltitude());
        update = true;
      }
    } else {
      if (place.getAltitude() != null) {
        ops.unset("altitude");
        place.setAltitude(newPlace.getAltitude());
        update = true;
      }
    }
    if (update) {
      mongoServer.getDatastore().update(query, ops);
    }

    return place;
  }
}
