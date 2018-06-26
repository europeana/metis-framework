package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.definitions.edm.entity.WebResource;

/**
 * Field updater for instances of {@link WebResource}.
 */
public class WebResourceUpdater extends AbstractEdmEntityUpdater<WebResource> {

  @Override
  protected Class<WebResource> getObjectClass() {
    return WebResource.class;
  }

  @Override
  protected void update(MongoPropertyUpdater<WebResource> propertyUpdater) {
    propertyUpdater.updateMap("dcDescription", WebResource::getDcDescription);
    propertyUpdater.updateMap("dcFormat", WebResource::getDcFormat);
    propertyUpdater.updateMap("dcCreator", WebResource::getDcCreator);
    propertyUpdater.updateMap("dcSource", WebResource::getDcSource);
    propertyUpdater.updateMap("dctermsConformsTo", WebResource::getDctermsConformsTo);
    propertyUpdater.updateMap("dctermsCreated", WebResource::getDctermsCreated);
    propertyUpdater.updateMap("dctermsExtent", WebResource::getDctermsExtent);
    propertyUpdater.updateMap("dctermsHasPart", WebResource::getDctermsHasPart);
    propertyUpdater.updateMap("dctermsIsFormatOf", WebResource::getDctermsIsFormatOf);
    propertyUpdater.updateMap("dctermsIssued", WebResource::getDctermsIssued);
    propertyUpdater.updateString("isNextInSequence", WebResource::getIsNextInSequence);
    propertyUpdater.updateMap("webResourceDcRights", WebResource::getWebResourceDcRights);
    propertyUpdater.updateMap("webResourceEdmRights", WebResource::getWebResourceEdmRights);
    propertyUpdater.updateArray("owlSameAs", WebResource::getOwlSameAs);
    propertyUpdater.updateString("edmPreview", WebResource::getEdmPreview);
    propertyUpdater.updateArray("svcsHasService", WebResource::getSvcsHasService);
    propertyUpdater.updateArray("dctermsIsReferencedBy", WebResource::getDctermsIsReferencedBy);
  }
}
