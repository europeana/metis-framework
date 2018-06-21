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
    boolean update = false;
    update = FieldUpdateUtils.updateString(mongoEntity, newEntity, "aggregatedCHO", ops,
        EuropeanaAggregationImpl::getAggregatedCHO, EuropeanaAggregationImpl::setAggregatedCHO)
        || update;
    newEntity.setEdmLandingPageFromAggregatedCHO();
    update = FieldUpdateUtils.updateString(mongoEntity, newEntity, "edmLandingPage", ops,
        EuropeanaAggregationImpl::getEdmLandingPage, EuropeanaAggregationImpl::setEdmLandingPage)
        || update;

    update = FieldUpdateUtils.updateString(mongoEntity, newEntity, "edmIsShownBy", ops,
        EuropeanaAggregationImpl::getEdmIsShownBy, EuropeanaAggregationImpl::setEdmIsShownBy)
        || update;
    update = FieldUpdateUtils.updateMap(mongoEntity, newEntity, "edmRights", ops,
        EuropeanaAggregationImpl::getEdmRights, EuropeanaAggregationImpl::setEdmRights) || update;
    update = FieldUpdateUtils.updateMap(mongoEntity, newEntity, "edmCountry", ops,
        EuropeanaAggregationImpl::getEdmCountry, EuropeanaAggregationImpl::setEdmCountry) || update;
    update = FieldUpdateUtils.updateMap(mongoEntity, newEntity, "edmLanguage", ops,
        EuropeanaAggregationImpl::getEdmLanguage, EuropeanaAggregationImpl::setEdmLanguage)
        || update;
    update = FieldUpdateUtils.updateMap(mongoEntity, newEntity, "dcCreator", ops,
        EuropeanaAggregationImpl::getDcCreator, EuropeanaAggregationImpl::setDcCreator) || update;
    update = FieldUpdateUtils.updateString(mongoEntity, newEntity, "edmPreview", ops,
        EuropeanaAggregationImpl::getEdmPreview, EuropeanaAggregationImpl::setEdmPreview) || update;
    update = FieldUpdateUtils.updateArray(mongoEntity, newEntity, "aggregates", ops,
        EuropeanaAggregationImpl::getAggregates, EuropeanaAggregationImpl::setAggregates) || update;

    List<WebResource> webResources = new ArrayList<>();
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
