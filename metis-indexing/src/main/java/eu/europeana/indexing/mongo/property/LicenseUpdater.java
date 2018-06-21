package eu.europeana.indexing.mongo.property;

import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.storage.MongoServer;

public class LicenseUpdater implements PropertyMongoUpdater<LicenseImpl> {

  @Override
  public LicenseImpl update(LicenseImpl mongoEntity, LicenseImpl newEntity,
      MongoServer mongoServer) {
    Query<LicenseImpl> updateQuery = mongoServer.getDatastore().createQuery(LicenseImpl.class)
        .field("about").equal(mongoEntity.getAbout());
    UpdateOperations<LicenseImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(LicenseImpl.class);
    boolean update = false;
    if (mongoEntity.getCcDeprecatedOn() != newEntity.getCcDeprecatedOn()) {
      if (mongoEntity.getCcDeprecatedOn() == null) {
        newEntity.setCcDeprecatedOn(null);
        ops.unset("ccDeprecatedOn");
        update = true;
      } else {
        newEntity.setCcDeprecatedOn(mongoEntity.getCcDeprecatedOn());
        ops.set("ccDeprecatedOn", mongoEntity.getCcDeprecatedOn());
        update = true;
      }
    }
    if (!StringUtils.equals(mongoEntity.getOdrlInheritFrom(), newEntity.getOdrlInheritFrom())) {
      if (mongoEntity.getOdrlInheritFrom() == null) {
        newEntity.setOdrlInheritFrom(null);
        ops.unset("odrlInheritFrom");
        update = true;
      } else {
        newEntity.setOdrlInheritFrom(mongoEntity.getOdrlInheritFrom());
        ops.set("odrlInheritFrom", mongoEntity.getOdrlInheritFrom());
        update = true;
      }
    }
    if (update) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }
    return mongoEntity;
  }

}
