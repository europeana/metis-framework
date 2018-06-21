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
    boolean update = false;
    update = FieldUpdateUtils.updateMap(wrMongo, wr, "dcDescription", ops,
        WebResource::getDcDescription, WebResource::setDcDescription) || update;
    update = FieldUpdateUtils.updateMap(wrMongo, wr, "dcFormat", ops, WebResource::getDcFormat,
        WebResource::setDcFormat) || update;
    update = FieldUpdateUtils.updateMap(wrMongo, wr, "dcCreator", ops, WebResource::getDcCreator,
        WebResource::setDcCreator) || update;
    update = FieldUpdateUtils.updateMap(wrMongo, wr, "dcSource", ops, WebResource::getDcSource,
        WebResource::setDcSource) || update;
    update = FieldUpdateUtils.updateMap(wrMongo, wr, "dctermsConformsTo", ops,
        WebResource::getDctermsConformsTo, WebResource::setDctermsConformsTo) || update;
    update = FieldUpdateUtils.updateMap(wrMongo, wr, "dctermsCreated", ops,
        WebResource::getDctermsCreated, WebResource::setDctermsCreated) || update;
    update = FieldUpdateUtils.updateMap(wrMongo, wr, "dctermsExtent", ops,
        WebResource::getDctermsExtent, WebResource::setDctermsExtent) || update;
    update = FieldUpdateUtils.updateMap(wrMongo, wr, "dctermsHasPart", ops,
        WebResource::getDctermsHasPart, WebResource::setDctermsHasPart) || update;
    update = FieldUpdateUtils.updateMap(wrMongo, wr, "dctermsIsFormatOf", ops,
        WebResource::getDctermsIsFormatOf, WebResource::setDctermsIsFormatOf) || update;
    update = FieldUpdateUtils.updateMap(wrMongo, wr, "dctermsIssued", ops,
        WebResource::getDctermsIssued, WebResource::setDctermsIssued) || update;
    update = FieldUpdateUtils.updateString(wrMongo, wr, "isNextInSequence", ops,
        WebResource::getIsNextInSequence, WebResource::setIsNextInSequence) || update;
    update = FieldUpdateUtils.updateMap(wrMongo, wr, "webResourceDcRights", ops,
        WebResource::getWebResourceDcRights, WebResource::setWebResourceDcRights) || update;
    update = FieldUpdateUtils.updateMap(wrMongo, wr, "webResourceEdmRights", ops,
        WebResource::getWebResourceEdmRights, WebResource::setWebResourceEdmRights) || update;
    update = FieldUpdateUtils.updateArray(wrMongo, wr, "owlSameAs", ops, WebResource::getOwlSameAs,
        WebResource::setOwlSameAs) || update;
    update = FieldUpdateUtils.updateString(wrMongo, wr, "edmPreview", ops,
        WebResource::getEdmPreview, WebResource::setEdmPreview) || update;
    update = FieldUpdateUtils.updateArray(wrMongo, wr, "svcsHasService", ops,
        WebResource::getSvcsHasService, WebResource::setSvcsHasService) || update;
    update = FieldUpdateUtils.updateArray(wrMongo, wr, "dctermsIsReferencedBy", ops,
        WebResource::getDctermsIsReferencedBy, WebResource::setDctermsIsReferencedBy) || update;
    if (update) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }
    return wrMongo;
  }
}
