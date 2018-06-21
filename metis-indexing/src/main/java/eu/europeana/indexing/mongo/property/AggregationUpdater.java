package eu.europeana.indexing.mongo.property;

import java.util.ArrayList;
import java.util.List;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.storage.MongoServer;

public class AggregationUpdater implements PropertyMongoUpdater<AggregationImpl> {

  @Override
  public AggregationImpl update(AggregationImpl mongoEntity, AggregationImpl newEntity,
      MongoServer mongoServer) {

    Query<AggregationImpl> updateQuery = mongoServer.getDatastore()
        .createQuery(AggregationImpl.class).field("about").equal(mongoEntity.getAbout());
    UpdateOperations<AggregationImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(AggregationImpl.class);

    boolean update = false;

    update = FieldUpdateUtils.updateString(mongoEntity, newEntity, "aggregatedCHO", ops,
        AggregationImpl::getAggregatedCHO, AggregationImpl::setAggregatedCHO) || update;
    update = FieldUpdateUtils.updateString(mongoEntity, newEntity, "edmIsShownAt", ops,
        AggregationImpl::getEdmIsShownAt, AggregationImpl::setEdmIsShownAt) || update;
    update = FieldUpdateUtils.updateString(mongoEntity, newEntity, "edmIsShownBy", ops,
        AggregationImpl::getEdmIsShownBy, AggregationImpl::setEdmIsShownBy) || update;
    update = FieldUpdateUtils.updateString(mongoEntity, newEntity, "edmObject", ops,
        AggregationImpl::getEdmObject, AggregationImpl::setEdmObject) || update;
    update = FieldUpdateUtils.updateString(mongoEntity, newEntity, "edmUgc", ops,
        AggregationImpl::getEdmUgc, AggregationImpl::setEdmUgc) || update;
    update = FieldUpdateUtils.updateMap(mongoEntity, newEntity, "edmDataProvider", ops,
        AggregationImpl::getEdmDataProvider, AggregationImpl::setEdmDataProvider) || update;
    update = FieldUpdateUtils.updateMap(mongoEntity, newEntity, "edmProvider", ops,
        AggregationImpl::getEdmProvider, AggregationImpl::setEdmProvider) || update;
    update = FieldUpdateUtils.updateMap(mongoEntity, newEntity, "dcRights", ops,
        AggregationImpl::getDcRights, AggregationImpl::setDcRights) || update;
    update = FieldUpdateUtils.updateMap(mongoEntity, newEntity, "edmRights", ops,
        AggregationImpl::getEdmRights, AggregationImpl::setEdmRights) || update;
    update = FieldUpdateUtils.updateArray(mongoEntity, newEntity, "hasView", ops,
        AggregationImpl::getHasView, AggregationImpl::setHasView) || update;
    update = FieldUpdateUtils.updateArray(mongoEntity, newEntity, "aggregates", ops,
        AggregationImpl::getAggregates, AggregationImpl::setAggregates) || update;
    if (newEntity.getEdmPreviewNoDistribute() != null) {
      if (mongoEntity.getEdmPreviewNoDistribute() == null
          || mongoEntity.getEdmPreviewNoDistribute() != newEntity.getEdmPreviewNoDistribute()) {
        ops.set("edmPreviewNoDistribute", newEntity.getEdmPreviewNoDistribute());
        mongoEntity.setEdmPreviewNoDistribute(newEntity.getEdmPreviewNoDistribute());
        update = true;
      }
    } else {
      if (mongoEntity.getEdmPreviewNoDistribute() != null) {
        ops.unset("edmPreviewNoDistribute");
        mongoEntity.setEdmPreviewNoDistribute(null);
        update = true;
      }
    }

    List<WebResource> webResources = new ArrayList<WebResource>();
    for (WebResource wr : mongoEntity.getWebResources()) {
      webResources.add(new WebResourceUpdater().saveWebResource(wr, mongoServer));
    }
    mongoEntity.setWebResources(webResources);

    if (update) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }
    return mongoEntity;
  }
}
