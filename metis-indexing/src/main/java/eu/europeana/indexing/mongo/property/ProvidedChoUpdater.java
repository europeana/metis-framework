package eu.europeana.indexing.mongo.property;

import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.storage.MongoServer;

/**
 * Field updater for instances of {@link ProvidedCHOImpl}.
 */
public class ProvidedChoUpdater implements PropertyMongoUpdater<ProvidedCHOImpl> {

  @Override
  public ProvidedCHOImpl update(ProvidedCHOImpl mongoEntity, ProvidedCHOImpl newEntity,
      MongoServer mongoServer) {
    Query<ProvidedCHOImpl> updateQuery = mongoServer.getDatastore()
        .createQuery(ProvidedCHOImpl.class).field("about").equal(mongoEntity.getAbout());
    UpdateOperations<ProvidedCHOImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(ProvidedCHOImpl.class);
    final UpdateTrigger updateTrigger = new UpdateTrigger();
    FieldUpdateUtils.updateArray(updateTrigger, mongoEntity, newEntity, "owlSameAs", ops,
        ProvidedCHOImpl::getOwlSameAs, ProvidedCHOImpl::setOwlSameAs);

    if (updateTrigger.isUpdateTriggered()) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }
    return mongoEntity;
  }

}
