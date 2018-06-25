package eu.europeana.indexing.mongo.property;

import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.storage.MongoServer;

/**
 * Field updater for instances of {@link AgentImpl}.
 */
public class AgentUpdater implements PropertyMongoUpdater<AgentImpl> {

  @Override
  public AgentImpl update(AgentImpl agent, AgentImpl newAgent, MongoServer mongoServer) {
    Query<AgentImpl> updateQuery = mongoServer.getDatastore().createQuery(AgentImpl.class)
        .field("about").equal(agent.getAbout());
    UpdateOperations<AgentImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(AgentImpl.class);

    final UpdateTrigger updateTrigger = new UpdateTrigger();
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "begin", ops, AgentImpl::getBegin,
        AgentImpl::setBegin);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "dcDate", ops, AgentImpl::getDcDate,
        AgentImpl::setDcDate);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "dcIdentifier", ops,
        AgentImpl::getDcIdentifier, AgentImpl::setDcIdentifier);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "rdaGr2BiographicalInformation", ops,
        AgentImpl::getRdaGr2BiographicalInformation, AgentImpl::setRdaGr2BiographicalInformation);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "rdaGr2DateOfBirth", ops,
        AgentImpl::getRdaGr2DateOfBirth, AgentImpl::setRdaGr2DateOfBirth);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "rdaGr2DateOfDeath", ops,
        AgentImpl::getRdaGr2DateOfDeath, AgentImpl::setRdaGr2DateOfDeath);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "rdaGr2PlaceOfBirth", ops,
        AgentImpl::getRdaGr2PlaceOfBirth, AgentImpl::setRdaGr2PlaceOfBirth);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "rdaGr2PlaceOfDeath", ops,
        AgentImpl::getRdaGr2PlaceOfDeath, AgentImpl::setRdaGr2PlaceOfDeath);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "rdaGr2DateOfEstablishment", ops,
        AgentImpl::getRdaGr2DateOfEstablishment, AgentImpl::setRdaGr2DateOfEstablishment);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "rdaGr2DateOfTermination", ops,
        AgentImpl::getRdaGr2DateOfTermination, AgentImpl::setRdaGr2DateOfTermination);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "rdaGr2Gender", ops,
        AgentImpl::getRdaGr2Gender, AgentImpl::setRdaGr2Gender);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "rdaGr2ProfessionOrOccupation", ops,
        AgentImpl::getRdaGr2ProfessionOrOccupation, AgentImpl::setRdaGr2ProfessionOrOccupation);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "edmHasMet", ops,
        AgentImpl::getEdmHasMet, AgentImpl::setEdmHasMet);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "edmIsRelatedTo", ops,
        AgentImpl::getEdmIsRelatedTo, AgentImpl::setEdmIsRelatedTo);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "foafName", ops,
        AgentImpl::getFoafName, AgentImpl::setFoafName);
    FieldUpdateUtils.updateArray(updateTrigger, agent, newAgent, "owlSameAs", ops,
        AgentImpl::getOwlSameAs, AgentImpl::setOwlSameAs);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "end", ops, AgentImpl::getEnd,
        AgentImpl::setEnd);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "note", ops, AgentImpl::getNote,
        AgentImpl::setNote);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "altLabel", ops,
        AgentImpl::getAltLabel, AgentImpl::setAltLabel);
    FieldUpdateUtils.updateMap(updateTrigger, agent, newAgent, "prefLabel", ops,
        AgentImpl::getPrefLabel, AgentImpl::setPrefLabel);
    if (updateTrigger.isUpdateTriggered()) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }
    return agent;
  }
}
