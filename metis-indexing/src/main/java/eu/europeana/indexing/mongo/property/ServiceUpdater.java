package eu.europeana.indexing.mongo.property;

import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.solr.entity.ServiceImpl;
import eu.europeana.corelib.storage.MongoServer;

/**
 * Field updater for instances of {@link ServiceImpl}.
 */
public class ServiceUpdater implements PropertyMongoUpdater<ServiceImpl> {

  @Override
  public ServiceImpl update(ServiceImpl mongoEntity, ServiceImpl newEntity,
      MongoServer mongoServer) {
    Query<ServiceImpl> updateQuery = mongoServer.getDatastore().createQuery(ServiceImpl.class)
        .field("about").equal(mongoEntity.getAbout());
    UpdateOperations<ServiceImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(ServiceImpl.class);

    final UpdateTrigger updateTrigger = new UpdateTrigger();
    if (!FieldUpdateUtils.arrayEquals(mongoEntity.getDctermsConformsTo(),
        newEntity.getDctermsConformsTo())) {
      if (mongoEntity.getDctermsConformsTo() == null) {
        newEntity.setDcTermsConformsTo(null);
        ops.unset("dctermsConformsTo");
      } else {
        newEntity.setDcTermsConformsTo(mongoEntity.getDctermsConformsTo());
        ops.set("dctermsConformsTo", mongoEntity.getDctermsConformsTo());
      }
      updateTrigger.triggerUpdate();
    }
    if (!FieldUpdateUtils.arrayEquals(mongoEntity.getDoapImplements(),
        newEntity.getDoapImplements())) {
      if (mongoEntity.getDoapImplements() == null) {
        newEntity.setDoapImplements(null);
        ops.unset("doapImplements");
      } else {
        newEntity.setDoapImplements(mongoEntity.getDoapImplements());
        ops.set("doapImplements", mongoEntity.getDoapImplements());
      }
      updateTrigger.triggerUpdate();
    }
    if (updateTrigger.isUpdateTriggered()) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }
    return mongoEntity;
  }
}
