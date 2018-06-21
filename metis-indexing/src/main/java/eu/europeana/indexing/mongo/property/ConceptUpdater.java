package eu.europeana.indexing.mongo.property;

import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.storage.MongoServer;

public class ConceptUpdater implements PropertyMongoUpdater<ConceptImpl> {

  @Override
  public ConceptImpl update(ConceptImpl conceptMongo, ConceptImpl concept,
      MongoServer mongoServer) {
    Query<ConceptImpl> updateQuery = mongoServer.getDatastore().createQuery(ConceptImpl.class)
        .field("about").equal(conceptMongo.getAbout());
    UpdateOperations<ConceptImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(ConceptImpl.class);
    boolean update = false;
    update = FieldUpdateUtils.updateMap(conceptMongo, concept, "altLabel", ops,
        ConceptImpl::getAltLabel, ConceptImpl::setAltLabel) || update;
    update = FieldUpdateUtils.updateMap(conceptMongo, concept, "prefLabel", ops,
        ConceptImpl::getPrefLabel, ConceptImpl::setPrefLabel) || update;
    update = FieldUpdateUtils.updateMap(conceptMongo, concept, "hiddenLabel", ops,
        ConceptImpl::getHiddenLabel, ConceptImpl::setHiddenLabel) || update;
    update = FieldUpdateUtils.updateMap(conceptMongo, concept, "notation", ops,
        ConceptImpl::getNotation, ConceptImpl::setNotation) || update;
    update = FieldUpdateUtils.updateMap(conceptMongo, concept, "note", ops, ConceptImpl::getNote,
        ConceptImpl::setNote) || update;
    update = FieldUpdateUtils.updateArray(conceptMongo, concept, "broader", ops,
        ConceptImpl::getBroader, ConceptImpl::setBroader) || update;
    update = FieldUpdateUtils.updateArray(conceptMongo, concept, "broadMatch", ops,
        ConceptImpl::getBroadMatch, ConceptImpl::setBroadMatch) || update;
    update = FieldUpdateUtils.updateArray(conceptMongo, concept, "closeMatch", ops,
        ConceptImpl::getCloseMatch, ConceptImpl::setCloseMatch) || update;
    update = FieldUpdateUtils.updateArray(conceptMongo, concept, "exactMatch", ops,
        ConceptImpl::getExactMatch, ConceptImpl::setExactMatch) || update;
    update = FieldUpdateUtils.updateArray(conceptMongo, concept, "inScheme", ops,
        ConceptImpl::getInScheme, ConceptImpl::setInScheme) || update;
    update = FieldUpdateUtils.updateArray(conceptMongo, concept, "narrower", ops,
        ConceptImpl::getNarrower, ConceptImpl::setNarrower) || update;
    update = FieldUpdateUtils.updateArray(conceptMongo, concept, "narrowMatch", ops,
        ConceptImpl::getNarrowMatch, ConceptImpl::setNarrowMatch) || update;
    update = FieldUpdateUtils.updateArray(conceptMongo, concept, "relatedMatch", ops,
        ConceptImpl::getRelatedMatch, ConceptImpl::setRelatedMatch) || update;
    update = FieldUpdateUtils.updateArray(conceptMongo, concept, "related", ops,
        ConceptImpl::getRelated, ConceptImpl::setRelated) || update;

    if (update) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }
    return conceptMongo;
  }

}
