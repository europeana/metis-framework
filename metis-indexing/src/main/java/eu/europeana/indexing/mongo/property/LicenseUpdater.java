package eu.europeana.indexing.mongo.property;

import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.storage.MongoServer;

/**
 * Field updater for instances of {@link LicenseImpl}.
 */
public class LicenseUpdater implements PropertyMongoUpdater<LicenseImpl> {

  @Override
  public LicenseImpl update(LicenseImpl mongoEntity, LicenseImpl newEntity,
      MongoServer mongoServer) {
    Query<LicenseImpl> updateQuery = mongoServer.getDatastore().createQuery(LicenseImpl.class)
        .field("about").equal(mongoEntity.getAbout());
    UpdateOperations<LicenseImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(LicenseImpl.class);
    final UpdateTrigger updateTrigger = new UpdateTrigger();
    if (mongoEntity.getCcDeprecatedOn() != newEntity.getCcDeprecatedOn()) {
      if (mongoEntity.getCcDeprecatedOn() == null) {
        newEntity.setCcDeprecatedOn(null);
        ops.unset("ccDeprecatedOn");
        updateTrigger.triggerUpdate();
      } else {
        newEntity.setCcDeprecatedOn(mongoEntity.getCcDeprecatedOn());
        ops.set("ccDeprecatedOn", mongoEntity.getCcDeprecatedOn());
        updateTrigger.triggerUpdate();
      }
    }
    if (!StringUtils.equals(mongoEntity.getOdrlInheritFrom(), newEntity.getOdrlInheritFrom())) {
      if (mongoEntity.getOdrlInheritFrom() == null) {
        newEntity.setOdrlInheritFrom(null);
        ops.unset("odrlInheritFrom");
        updateTrigger.triggerUpdate();
      } else {
        newEntity.setOdrlInheritFrom(mongoEntity.getOdrlInheritFrom());
        ops.set("odrlInheritFrom", mongoEntity.getOdrlInheritFrom());
        updateTrigger.triggerUpdate();
      }
    }
    if (updateTrigger.isUpdateTriggered()) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }
    return mongoEntity;
  }

}
