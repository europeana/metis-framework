package eu.europeana.indexing.mongo.property;

import java.util.Objects;
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
    if (!Objects.equals(mongoEntity.getCcDeprecatedOn(), newEntity.getCcDeprecatedOn())) {
      newEntity.setCcDeprecatedOn(mongoEntity.getCcDeprecatedOn());
      if (newEntity.getCcDeprecatedOn() == null) {
        ops.unset("ccDeprecatedOn");
      } else {
        ops.set("ccDeprecatedOn", mongoEntity.getCcDeprecatedOn());
      }
      updateTrigger.triggerUpdate();
    }
    if (!Objects.equals(mongoEntity.getOdrlInheritFrom(), newEntity.getOdrlInheritFrom())) {
      newEntity.setOdrlInheritFrom(mongoEntity.getOdrlInheritFrom());
      if (newEntity.getOdrlInheritFrom() == null) {
        ops.unset("odrlInheritFrom");
      } else {
        ops.set("odrlInheritFrom", mongoEntity.getOdrlInheritFrom());
      }
      updateTrigger.triggerUpdate();
    }
    if (updateTrigger.isUpdateTriggered()) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }
    return mongoEntity;
  }

}
