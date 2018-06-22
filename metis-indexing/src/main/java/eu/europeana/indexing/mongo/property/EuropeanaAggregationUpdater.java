package eu.europeana.indexing.mongo.property;

import java.util.ArrayList;
import java.util.List;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.storage.MongoServer;

/**
 * Field updater for instances of {@link EuropeanaAggregationImpl}.
 */
public class EuropeanaAggregationUpdater implements PropertyMongoUpdater<EuropeanaAggregationImpl> {

  @Override
  public EuropeanaAggregationImpl update(EuropeanaAggregationImpl mongoEntity,
      EuropeanaAggregationImpl newEntity, MongoServer mongoServer) {
    Query<EuropeanaAggregationImpl> updateQuery = mongoServer.getDatastore()
        .createQuery(EuropeanaAggregationImpl.class).field("about").equal(mongoEntity.getAbout());
    UpdateOperations<EuropeanaAggregationImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(EuropeanaAggregationImpl.class);

    final UpdateTrigger updateTrigger = new UpdateTrigger();
    FieldUpdateUtils.updateString(updateTrigger, mongoEntity, newEntity, "aggregatedCHO", ops,
        EuropeanaAggregationImpl::getAggregatedCHO, EuropeanaAggregationImpl::setAggregatedCHO);
    newEntity.setEdmLandingPageFromAggregatedCHO();
    FieldUpdateUtils.updateString(updateTrigger, mongoEntity, newEntity, "edmLandingPage", ops,
        EuropeanaAggregationImpl::getEdmLandingPage, EuropeanaAggregationImpl::setEdmLandingPage);
    FieldUpdateUtils.updateString(updateTrigger, mongoEntity, newEntity, "edmIsShownBy", ops,
        EuropeanaAggregationImpl::getEdmIsShownBy, EuropeanaAggregationImpl::setEdmIsShownBy);
    FieldUpdateUtils.updateMap(updateTrigger, mongoEntity, newEntity, "edmRights", ops,
        EuropeanaAggregationImpl::getEdmRights, EuropeanaAggregationImpl::setEdmRights);
    FieldUpdateUtils.updateMap(updateTrigger, mongoEntity, newEntity, "edmCountry", ops,
        EuropeanaAggregationImpl::getEdmCountry, EuropeanaAggregationImpl::setEdmCountry);
    FieldUpdateUtils.updateMap(updateTrigger, mongoEntity, newEntity, "edmLanguage", ops,
        EuropeanaAggregationImpl::getEdmLanguage, EuropeanaAggregationImpl::setEdmLanguage);
    FieldUpdateUtils.updateMap(updateTrigger, mongoEntity, newEntity, "dcCreator", ops,
        EuropeanaAggregationImpl::getDcCreator, EuropeanaAggregationImpl::setDcCreator);
    FieldUpdateUtils.updateString(updateTrigger, mongoEntity, newEntity, "edmPreview", ops,
        EuropeanaAggregationImpl::getEdmPreview, EuropeanaAggregationImpl::setEdmPreview);
    FieldUpdateUtils.updateArray(updateTrigger, mongoEntity, newEntity, "aggregates", ops,
        EuropeanaAggregationImpl::getAggregates, EuropeanaAggregationImpl::setAggregates);

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
