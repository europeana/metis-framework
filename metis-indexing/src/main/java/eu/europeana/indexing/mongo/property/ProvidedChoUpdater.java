package eu.europeana.indexing.mongo.property;

import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.storage.MongoServer;

public class ProvidedChoUpdater implements PropertyMongoUpdater<ProvidedCHOImpl> {

  @Override
  public ProvidedCHOImpl update(ProvidedCHOImpl mongoEntity, ProvidedCHOImpl newEntity,
      MongoServer mongoServer) {
    Query<ProvidedCHOImpl> updateQuery = mongoServer.getDatastore()
        .createQuery(ProvidedCHOImpl.class).field("about").equal(mongoEntity.getAbout());
    UpdateOperations<ProvidedCHOImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(ProvidedCHOImpl.class);
    boolean update = false;
    update = FieldUpdateUtils.updateArray(mongoEntity, newEntity, "owlSameAs", ops,
        ProvidedCHOImpl::getOwlSameAs, ProvidedCHOImpl::setOwlSameAs) || update;

    if (update) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }
    return mongoEntity;
  }

}
