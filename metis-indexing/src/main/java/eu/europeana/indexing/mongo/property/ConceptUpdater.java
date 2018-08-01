package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.solr.entity.ConceptImpl;

/**
 * Field updater for instances of {@link ConceptImpl}.
 */
public class ConceptUpdater extends AbstractIsolatedEdmEntityUpdater<ConceptImpl> {

  @Override
  protected Class<ConceptImpl> getObjectClass() {
    return ConceptImpl.class;
  }

  @Override
  protected void update(MongoPropertyUpdater<ConceptImpl> propertyUpdater) {
    propertyUpdater.updateMap("altLabel", ConceptImpl::getAltLabel);
    propertyUpdater.updateMap("prefLabel", ConceptImpl::getPrefLabel);
    propertyUpdater.updateMap("hiddenLabel", ConceptImpl::getHiddenLabel);
    propertyUpdater.updateMap("notation", ConceptImpl::getNotation);
    propertyUpdater.updateMap("note", ConceptImpl::getNote);
    propertyUpdater.updateArray("broader", ConceptImpl::getBroader);
    propertyUpdater.updateArray("broadMatch", ConceptImpl::getBroadMatch);
    propertyUpdater.updateArray("closeMatch", ConceptImpl::getCloseMatch);
    propertyUpdater.updateArray("exactMatch", ConceptImpl::getExactMatch);
    propertyUpdater.updateArray("inScheme", ConceptImpl::getInScheme);
    propertyUpdater.updateArray("narrower", ConceptImpl::getNarrower);
    propertyUpdater.updateArray("narrowMatch", ConceptImpl::getNarrowMatch);
    propertyUpdater.updateArray("relatedMatch", ConceptImpl::getRelatedMatch);
    propertyUpdater.updateArray("related", ConceptImpl::getRelated);
  }
}
