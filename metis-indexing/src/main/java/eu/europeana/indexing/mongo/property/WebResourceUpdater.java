package eu.europeana.indexing.mongo.property;

import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.mongo.server.EdmMongoServer;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.corelib.storage.MongoServer;

/**
 * Field updater for instances of {@link WebResource}.
 */
public class WebResourceUpdater {

  /**
   * Update a web resource.
   * 
   * @param wr The new entity (to take the new values from).
   * @param mongoServer The mongo server.
   * @return The updated entity.
   */
  public WebResource saveWebResource(WebResource wr, MongoServer mongoServer) {
    WebResourceImpl wrMongo =
        ((EdmMongoServer) mongoServer).searchByAbout(WebResourceImpl.class, wr.getAbout());
    if (wrMongo != null) {
      return update(wrMongo, wr, mongoServer);
    }

    mongoServer.getDatastore().save(wr);
    return wr;
  }

  private WebResource update(WebResource wrMongo, WebResource wr, MongoServer mongoServer) {
    Query<WebResourceImpl> updateQuery = mongoServer.getDatastore()
        .createQuery(WebResourceImpl.class).field("about").equal(wr.getAbout());
    UpdateOperations<WebResourceImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(WebResourceImpl.class);
    final UpdateTrigger updateTrigger = new UpdateTrigger();
    FieldUpdateUtils.updateMap(updateTrigger, wrMongo, wr, "dcDescription", ops,
        WebResource::getDcDescription, WebResource::setDcDescription);
    FieldUpdateUtils.updateMap(updateTrigger, wrMongo, wr, "dcFormat", ops,
        WebResource::getDcFormat, WebResource::setDcFormat);
    FieldUpdateUtils.updateMap(updateTrigger, wrMongo, wr, "dcCreator", ops,
        WebResource::getDcCreator, WebResource::setDcCreator);
    FieldUpdateUtils.updateMap(updateTrigger, wrMongo, wr, "dcSource", ops,
        WebResource::getDcSource, WebResource::setDcSource);
    FieldUpdateUtils.updateMap(updateTrigger, wrMongo, wr, "dctermsConformsTo", ops,
        WebResource::getDctermsConformsTo, WebResource::setDctermsConformsTo);
    FieldUpdateUtils.updateMap(updateTrigger, wrMongo, wr, "dctermsCreated", ops,
        WebResource::getDctermsCreated, WebResource::setDctermsCreated);
    FieldUpdateUtils.updateMap(updateTrigger, wrMongo, wr, "dctermsExtent", ops,
        WebResource::getDctermsExtent, WebResource::setDctermsExtent);
    FieldUpdateUtils.updateMap(updateTrigger, wrMongo, wr, "dctermsHasPart", ops,
        WebResource::getDctermsHasPart, WebResource::setDctermsHasPart);
    FieldUpdateUtils.updateMap(updateTrigger, wrMongo, wr, "dctermsIsFormatOf", ops,
        WebResource::getDctermsIsFormatOf, WebResource::setDctermsIsFormatOf);
    FieldUpdateUtils.updateMap(updateTrigger, wrMongo, wr, "dctermsIssued", ops,
        WebResource::getDctermsIssued, WebResource::setDctermsIssued);
    FieldUpdateUtils.updateString(updateTrigger, wrMongo, wr, "isNextInSequence", ops,
        WebResource::getIsNextInSequence, WebResource::setIsNextInSequence);
    FieldUpdateUtils.updateMap(updateTrigger, wrMongo, wr, "webResourceDcRights", ops,
        WebResource::getWebResourceDcRights, WebResource::setWebResourceDcRights);
    FieldUpdateUtils.updateMap(updateTrigger, wrMongo, wr, "webResourceEdmRights", ops,
        WebResource::getWebResourceEdmRights, WebResource::setWebResourceEdmRights);
    FieldUpdateUtils.updateArray(updateTrigger, wrMongo, wr, "owlSameAs", ops,
        WebResource::getOwlSameAs, WebResource::setOwlSameAs);
    FieldUpdateUtils.updateString(updateTrigger, wrMongo, wr, "edmPreview", ops,
        WebResource::getEdmPreview, WebResource::setEdmPreview);
    FieldUpdateUtils.updateArray(updateTrigger, wrMongo, wr, "svcsHasService", ops,
        WebResource::getSvcsHasService, WebResource::setSvcsHasService);
    FieldUpdateUtils.updateArray(updateTrigger, wrMongo, wr, "dctermsIsReferencedBy", ops,
        WebResource::getDctermsIsReferencedBy, WebResource::setDctermsIsReferencedBy);
    if (updateTrigger.isUpdateTriggered()) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }
    return wrMongo;
  }
}
