package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;

/**
 * Field updater for instances of {@link EuropeanaAggregation}.
 */
public class EuropeanaAggregationUpdater
    extends AbstractEdmEntityUpdater<EuropeanaAggregationImpl> {

  @Override
  protected Class<EuropeanaAggregationImpl> getObjectClass() {
    return EuropeanaAggregationImpl.class;
  }

  @Override
  protected void preprocessEntity(EuropeanaAggregationImpl newEntity) {
    newEntity.setEdmLandingPageFromAggregatedCHO();
    super.preprocessEntity(newEntity);
  }

  @Override
  protected void update(MongoPropertyUpdater<EuropeanaAggregationImpl> propertyUpdater) {
    propertyUpdater.updateString("aggregatedCHO", EuropeanaAggregation::getAggregatedCHO);
    propertyUpdater.updateString("edmLandingPage", EuropeanaAggregation::getEdmLandingPage);
    propertyUpdater.updateString("edmIsShownBy", EuropeanaAggregation::getEdmIsShownBy);
    propertyUpdater.updateMap("edmRights", EuropeanaAggregation::getEdmRights);
    propertyUpdater.updateMap("edmCountry", EuropeanaAggregation::getEdmCountry);
    propertyUpdater.updateMap("edmLanguage", EuropeanaAggregation::getEdmLanguage);
    propertyUpdater.updateMap("dcCreator", EuropeanaAggregation::getDcCreator);
    propertyUpdater.updateString("edmPreview", EuropeanaAggregation::getEdmPreview);
    propertyUpdater.updateArray("aggregates", EuropeanaAggregation::getAggregates);
    propertyUpdater.updateWebResources("webResources", EuropeanaAggregation::getWebResources);
  }
}
