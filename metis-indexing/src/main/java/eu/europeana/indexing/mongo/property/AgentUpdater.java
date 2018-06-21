package eu.europeana.indexing.mongo.property;

import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.storage.MongoServer;

public class AgentUpdater implements PropertyMongoUpdater<AgentImpl> {

  @Override
  public AgentImpl update(AgentImpl agent, AgentImpl newAgent, MongoServer mongoServer) {
    Query<AgentImpl> updateQuery = mongoServer.getDatastore().createQuery(AgentImpl.class)
        .field("about").equal(agent.getAbout());
    UpdateOperations<AgentImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(AgentImpl.class);
    boolean update = false;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "begin", ops, AgentImpl::getBegin,
        AgentImpl::setBegin) || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "dcDate", ops, AgentImpl::getDcDate,
        AgentImpl::setDcDate) || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "dcIdentifier", ops,
        AgentImpl::getDcIdentifier, AgentImpl::setDcIdentifier) || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "rdaGr2BiographicalInformation", ops,
        AgentImpl::getRdaGr2BiographicalInformation, AgentImpl::setRdaGr2BiographicalInformation)
        || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "rdaGr2DateOfBirth", ops,
        AgentImpl::getRdaGr2DateOfBirth, AgentImpl::setRdaGr2DateOfBirth) || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "rdaGr2DateOfDeath", ops,
        AgentImpl::getRdaGr2DateOfDeath, AgentImpl::setRdaGr2DateOfDeath) || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "rdaGr2PlaceOfBirth", ops,
        AgentImpl::getRdaGr2PlaceOfBirth, AgentImpl::setRdaGr2PlaceOfBirth) || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "rdaGr2PlaceOfDeath", ops,
        AgentImpl::getRdaGr2PlaceOfDeath, AgentImpl::setRdaGr2PlaceOfDeath) || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "rdaGr2DateOfEstablishment", ops,
        AgentImpl::getRdaGr2DateOfEstablishment, AgentImpl::setRdaGr2DateOfEstablishment) || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "rdaGr2DateOfTermination", ops,
        AgentImpl::getRdaGr2DateOfTermination, AgentImpl::setRdaGr2DateOfTermination) || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "rdaGr2Gender", ops,
        AgentImpl::getRdaGr2Gender, AgentImpl::setRdaGr2Gender) || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "rdaGr2ProfessionOrOccupation", ops,
        AgentImpl::getRdaGr2ProfessionOrOccupation, AgentImpl::setRdaGr2ProfessionOrOccupation)
        || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "edmHasMet", ops, AgentImpl::getEdmHasMet,
        AgentImpl::setEdmHasMet) || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "edmIsRelatedTo", ops,
        AgentImpl::getEdmIsRelatedTo, AgentImpl::setEdmIsRelatedTo) || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "foafName", ops, AgentImpl::getFoafName,
        AgentImpl::setFoafName) || update;
    update = FieldUpdateUtils.updateArray(agent, newAgent, "owlSameAs", ops,
        AgentImpl::getOwlSameAs, AgentImpl::setOwlSameAs) || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "end", ops, AgentImpl::getEnd,
        AgentImpl::setEnd) || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "note", ops, AgentImpl::getNote,
        AgentImpl::setNote) || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "altLabel", ops, AgentImpl::getAltLabel,
        AgentImpl::setAltLabel) || update;
    update = FieldUpdateUtils.updateMap(agent, newAgent, "prefLabel", ops, AgentImpl::getPrefLabel,
        AgentImpl::setPrefLabel) || update;
    if (update) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }
    return agent;
  }
}
