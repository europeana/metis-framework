package eu.europeana.indexing.mongo.property;

import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.storage.MongoServer;

/**
 * Field updater for instances of {@link ConceptImpl}.
 */
public class ConceptUpdater implements PropertyMongoUpdater<ConceptImpl> {

  @Override
  public ConceptImpl update(ConceptImpl conceptMongo, ConceptImpl concept,
      MongoServer mongoServer) {
    Query<ConceptImpl> updateQuery = mongoServer.getDatastore().createQuery(ConceptImpl.class)
        .field("about").equal(conceptMongo.getAbout());
    UpdateOperations<ConceptImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(ConceptImpl.class);

    final UpdateTrigger updateTrigger = new UpdateTrigger();
    FieldUpdateUtils.updateMap(updateTrigger, conceptMongo, concept, "altLabel", ops,
        ConceptImpl::getAltLabel, ConceptImpl::setAltLabel);
    FieldUpdateUtils.updateMap(updateTrigger, conceptMongo, concept, "prefLabel", ops,
        ConceptImpl::getPrefLabel, ConceptImpl::setPrefLabel);
    FieldUpdateUtils.updateMap(updateTrigger, conceptMongo, concept, "hiddenLabel", ops,
        ConceptImpl::getHiddenLabel, ConceptImpl::setHiddenLabel);
    FieldUpdateUtils.updateMap(updateTrigger, conceptMongo, concept, "notation", ops,
        ConceptImpl::getNotation, ConceptImpl::setNotation);
    FieldUpdateUtils.updateMap(updateTrigger, conceptMongo, concept, "note", ops,
        ConceptImpl::getNote, ConceptImpl::setNote);
    FieldUpdateUtils.updateArray(updateTrigger, conceptMongo, concept, "broader", ops,
        ConceptImpl::getBroader, ConceptImpl::setBroader);
    FieldUpdateUtils.updateArray(updateTrigger, conceptMongo, concept, "broadMatch", ops,
        ConceptImpl::getBroadMatch, ConceptImpl::setBroadMatch);
    FieldUpdateUtils.updateArray(updateTrigger, conceptMongo, concept, "closeMatch", ops,
        ConceptImpl::getCloseMatch, ConceptImpl::setCloseMatch);
    FieldUpdateUtils.updateArray(updateTrigger, conceptMongo, concept, "exactMatch", ops,
        ConceptImpl::getExactMatch, ConceptImpl::setExactMatch);
    FieldUpdateUtils.updateArray(updateTrigger, conceptMongo, concept, "inScheme", ops,
        ConceptImpl::getInScheme, ConceptImpl::setInScheme);
    FieldUpdateUtils.updateArray(updateTrigger, conceptMongo, concept, "narrower", ops,
        ConceptImpl::getNarrower, ConceptImpl::setNarrower);
    FieldUpdateUtils.updateArray(updateTrigger, conceptMongo, concept, "narrowMatch", ops,
        ConceptImpl::getNarrowMatch, ConceptImpl::setNarrowMatch);
    FieldUpdateUtils.updateArray(updateTrigger, conceptMongo, concept, "relatedMatch", ops,
        ConceptImpl::getRelatedMatch, ConceptImpl::setRelatedMatch);
    FieldUpdateUtils.updateArray(updateTrigger, conceptMongo, concept, "related", ops,
        ConceptImpl::getRelated, ConceptImpl::setRelated);

    if (updateTrigger.isUpdateTriggered()) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }
    return conceptMongo;
  }

}
