package eu.europeana.indexing.mongo.property;

import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.corelib.storage.MongoServer;

/**
 * Field updater for instances of {@link TimespanImpl}.
 */
public class TimespanUpdater implements PropertyMongoUpdater<TimespanImpl> {

  @Override
  public TimespanImpl update(TimespanImpl mongoTimespan, TimespanImpl timeSpan,
      MongoServer mongoServer) {
    Query<TimespanImpl> updateQuery = mongoServer.getDatastore().createQuery(TimespanImpl.class)
        .field("about").equal(mongoTimespan.getAbout());
    UpdateOperations<TimespanImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(TimespanImpl.class);
    final UpdateTrigger updateTrigger = new UpdateTrigger();
    FieldUpdateUtils.updateMap(updateTrigger, mongoTimespan, timeSpan, "begin", ops,
        TimespanImpl::getBegin, TimespanImpl::setBegin);
    FieldUpdateUtils.updateMap(updateTrigger, mongoTimespan, timeSpan, "end", ops,
        TimespanImpl::getEnd, TimespanImpl::setEnd);
    FieldUpdateUtils.updateMap(updateTrigger, mongoTimespan, timeSpan, "note", ops,
        TimespanImpl::getNote, TimespanImpl::setNote);
    FieldUpdateUtils.updateMap(updateTrigger, mongoTimespan, timeSpan, "altLabel", ops,
        TimespanImpl::getAltLabel, TimespanImpl::setAltLabel);
    FieldUpdateUtils.updateMap(updateTrigger, mongoTimespan, timeSpan, "prefLabel", ops,
        TimespanImpl::getPrefLabel, TimespanImpl::setPrefLabel);
    FieldUpdateUtils.updateMap(updateTrigger, mongoTimespan, timeSpan, "dctermsHasPart", ops,
        TimespanImpl::getDctermsHasPart, TimespanImpl::setDctermsHasPart);
    FieldUpdateUtils.updateArray(updateTrigger, mongoTimespan, timeSpan, "owlSameAs", ops,
        TimespanImpl::getOwlSameAs, TimespanImpl::setOwlSameAs);

    if (updateTrigger.isUpdateTriggered()) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }
    return mongoTimespan;
  }
}
