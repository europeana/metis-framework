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
      newEntity.setDcTermsConformsTo(mongoEntity.getDctermsConformsTo());
      if (newEntity.getDctermsConformsTo() == null) {
        ops.unset("dctermsConformsTo");
      } else {
        ops.set("dctermsConformsTo", mongoEntity.getDctermsConformsTo());
      }
      updateTrigger.triggerUpdate();
    }
    if (!FieldUpdateUtils.arrayEquals(mongoEntity.getDoapImplements(),
        newEntity.getDoapImplements())) {
      newEntity.setDoapImplements(mongoEntity.getDoapImplements());
      if (newEntity.getDoapImplements() == null) {
        ops.unset("doapImplements");
      } else {
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
