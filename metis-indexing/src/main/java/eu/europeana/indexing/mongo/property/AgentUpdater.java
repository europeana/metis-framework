package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.solr.entity.AgentImpl;

/**
 * Field updater for instances of {@link AgentImpl}.
 */
public class AgentUpdater extends AbstractIsolatedEdmEntityUpdater<AgentImpl> {

  @Override
  protected Class<AgentImpl> getObjectClass() {
    return AgentImpl.class;
  }

  @Override
  protected void update(MongoPropertyUpdater<AgentImpl> propertyUpdater) {
    propertyUpdater.updateMap("begin", AgentImpl::getBegin);
    propertyUpdater.updateMap("dcDate", AgentImpl::getDcDate);
    propertyUpdater.updateMap("dcIdentifier", AgentImpl::getDcIdentifier);
    propertyUpdater.updateMap("rdaGr2BiographicalInformation",
        AgentImpl::getRdaGr2BiographicalInformation);
    propertyUpdater.updateMap("rdaGr2DateOfBirth", AgentImpl::getRdaGr2DateOfBirth);
    propertyUpdater.updateMap("rdaGr2DateOfDeath", AgentImpl::getRdaGr2DateOfDeath);
    propertyUpdater.updateMap("rdaGr2PlaceOfBirth", AgentImpl::getRdaGr2PlaceOfBirth);
    propertyUpdater.updateMap("rdaGr2PlaceOfDeath", AgentImpl::getRdaGr2PlaceOfDeath);
    propertyUpdater.updateMap("rdaGr2DateOfEstablishment", AgentImpl::getRdaGr2DateOfEstablishment);
    propertyUpdater.updateMap("rdaGr2DateOfTermination", AgentImpl::getRdaGr2DateOfTermination);
    propertyUpdater.updateMap("rdaGr2Gender", AgentImpl::getRdaGr2Gender);
    propertyUpdater.updateMap("rdaGr2ProfessionOrOccupation",
        AgentImpl::getRdaGr2ProfessionOrOccupation);
    propertyUpdater.updateMap("edmHasMet", AgentImpl::getEdmHasMet);
    propertyUpdater.updateMap("edmIsRelatedTo", AgentImpl::getEdmIsRelatedTo);
    propertyUpdater.updateMap("foafName", AgentImpl::getFoafName);
    propertyUpdater.updateArray("owlSameAs", AgentImpl::getOwlSameAs);
    propertyUpdater.updateMap("end", AgentImpl::getEnd);
    propertyUpdater.updateMap("note", AgentImpl::getNote);
    propertyUpdater.updateMap("altLabel", AgentImpl::getAltLabel);
    propertyUpdater.updateMap("prefLabel", AgentImpl::getPrefLabel);
  }
}
