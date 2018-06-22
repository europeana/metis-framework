package eu.europeana.indexing.mongo.property;

import java.util.ArrayList;
import java.util.List;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.storage.MongoServer;

/**
 * Field updater for instances of {@link AggregationImpl}.
 */
public class AggregationUpdater implements PropertyMongoUpdater<AggregationImpl> {

  @Override
  public AggregationImpl update(AggregationImpl mongoEntity, AggregationImpl newEntity,
      MongoServer mongoServer) {

    Query<AggregationImpl> updateQuery = mongoServer.getDatastore()
        .createQuery(AggregationImpl.class).field("about").equal(mongoEntity.getAbout());
    UpdateOperations<AggregationImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(AggregationImpl.class);

    final UpdateTrigger updateTrigger = new UpdateTrigger();
    FieldUpdateUtils.updateString(updateTrigger, mongoEntity, newEntity, "aggregatedCHO", ops,
        AggregationImpl::getAggregatedCHO, AggregationImpl::setAggregatedCHO);
    FieldUpdateUtils.updateString(updateTrigger, mongoEntity, newEntity, "edmIsShownAt", ops,
        AggregationImpl::getEdmIsShownAt, AggregationImpl::setEdmIsShownAt);
    FieldUpdateUtils.updateString(updateTrigger, mongoEntity, newEntity, "edmIsShownBy", ops,
        AggregationImpl::getEdmIsShownBy, AggregationImpl::setEdmIsShownBy);
    FieldUpdateUtils.updateString(updateTrigger, mongoEntity, newEntity, "edmObject", ops,
        AggregationImpl::getEdmObject, AggregationImpl::setEdmObject);
    FieldUpdateUtils.updateString(updateTrigger, mongoEntity, newEntity, "edmUgc", ops,
        AggregationImpl::getEdmUgc, AggregationImpl::setEdmUgc);
    FieldUpdateUtils.updateMap(updateTrigger, mongoEntity, newEntity, "edmDataProvider", ops,
        AggregationImpl::getEdmDataProvider, AggregationImpl::setEdmDataProvider);
    FieldUpdateUtils.updateMap(updateTrigger, mongoEntity, newEntity, "edmProvider", ops,
        AggregationImpl::getEdmProvider, AggregationImpl::setEdmProvider);
    FieldUpdateUtils.updateMap(updateTrigger, mongoEntity, newEntity, "dcRights", ops,
        AggregationImpl::getDcRights, AggregationImpl::setDcRights);
    FieldUpdateUtils.updateMap(updateTrigger, mongoEntity, newEntity, "edmRights", ops,
        AggregationImpl::getEdmRights, AggregationImpl::setEdmRights);
    FieldUpdateUtils.updateArray(updateTrigger, mongoEntity, newEntity, "hasView", ops,
        AggregationImpl::getHasView, AggregationImpl::setHasView);
    FieldUpdateUtils.updateArray(updateTrigger, mongoEntity, newEntity, "aggregates", ops,
        AggregationImpl::getAggregates, AggregationImpl::setAggregates);
    if (newEntity.getEdmPreviewNoDistribute() != null) {
      if (mongoEntity.getEdmPreviewNoDistribute() == null || !mongoEntity
          .getEdmPreviewNoDistribute().equals(newEntity.getEdmPreviewNoDistribute())) {
        ops.set("edmPreviewNoDistribute", newEntity.getEdmPreviewNoDistribute());
        mongoEntity.setEdmPreviewNoDistribute(newEntity.getEdmPreviewNoDistribute());
        updateTrigger.triggerUpdate();
      }
    } else {
      if (mongoEntity.getEdmPreviewNoDistribute() != null) {
        ops.unset("edmPreviewNoDistribute");
        mongoEntity.setEdmPreviewNoDistribute(null);
        updateTrigger.triggerUpdate();
      }
    }

    List<WebResource> webResources = new ArrayList<>();
    for (WebResource wr : mongoEntity.getWebResources()) {
      webResources.add(new WebResourceUpdater().saveWebResource(wr, mongoServer));
    }
    mongoEntity.setWebResources(webResources);

    if (updateTrigger.isUpdateTriggered()) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }
    return mongoEntity;
  }
}
