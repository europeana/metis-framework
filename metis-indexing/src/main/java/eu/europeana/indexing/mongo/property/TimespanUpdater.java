package eu.europeana.indexing.mongo.property;

import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.corelib.storage.MongoServer;

public class TimespanUpdater implements PropertyMongoUpdater<TimespanImpl> {

  @Override
  public TimespanImpl update(TimespanImpl mongoTimespan, TimespanImpl timeSpan,
      MongoServer mongoServer) {
    Query<TimespanImpl> updateQuery = mongoServer.getDatastore().createQuery(TimespanImpl.class)
        .field("about").equal(mongoTimespan.getAbout());
    UpdateOperations<TimespanImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(TimespanImpl.class);
    boolean update = false;
    update = FieldUpdateUtils.updateMap(mongoTimespan, timeSpan, "begin", ops,
        TimespanImpl::getBegin, TimespanImpl::setBegin) || update;
    update = FieldUpdateUtils.updateMap(mongoTimespan, timeSpan, "end", ops, TimespanImpl::getEnd,
        TimespanImpl::setEnd) || update;
    update = FieldUpdateUtils.updateMap(mongoTimespan, timeSpan, "note", ops, TimespanImpl::getNote,
        TimespanImpl::setNote) || update;
    update = FieldUpdateUtils.updateMap(mongoTimespan, timeSpan, "altLabel", ops,
        TimespanImpl::getAltLabel, TimespanImpl::setAltLabel) || update;
    update = FieldUpdateUtils.updateMap(mongoTimespan, timeSpan, "prefLabel", ops,
        TimespanImpl::getPrefLabel, TimespanImpl::setPrefLabel) || update;
    update = FieldUpdateUtils.updateMap(mongoTimespan, timeSpan, "dctermsHasPart", ops,
        TimespanImpl::getDctermsHasPart, TimespanImpl::setDctermsHasPart) || update;
    update = FieldUpdateUtils.updateArray(mongoTimespan, timeSpan, "owlSameAs", ops,
        TimespanImpl::getOwlSameAs, TimespanImpl::setOwlSameAs) || update;

    if (update) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }
    return mongoTimespan;
  }
}
