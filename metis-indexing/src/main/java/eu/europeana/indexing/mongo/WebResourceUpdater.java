package eu.europeana.indexing.mongo;

import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import eu.europeana.indexing.mongo.property.MongoPropertyUpdater;
import eu.europeana.indexing.mongo.property.RootAboutWrapper;

/**
 * Field updater for instances of {@link WebResourceImpl}.
 */
public class WebResourceUpdater extends
    AbstractEdmEntityUpdater<WebResourceImpl, RootAboutWrapper> {

  @Override
  protected Class<WebResourceImpl> getObjectClass() {
    return WebResourceImpl.class;
  }

  @Override
  protected void update(MongoPropertyUpdater<WebResourceImpl> propertyUpdater,
      RootAboutWrapper ancestorInformation) {
    propertyUpdater.updateMap("dcDescription", WebResource::getDcDescription);
    propertyUpdater.updateMap("dcFormat", WebResource::getDcFormat);
    propertyUpdater.updateMap("dcCreator", WebResource::getDcCreator);
    propertyUpdater.updateMap("dcSource", WebResource::getDcSource);
    propertyUpdater.updateMap("dctermsConformsTo", WebResource::getDctermsConformsTo);
    propertyUpdater.updateMap("dctermsCreated", WebResource::getDctermsCreated);
    propertyUpdater.updateMap("dctermsExtent", WebResource::getDctermsExtent);
    propertyUpdater.updateMap("dctermsHasPart", WebResource::getDctermsHasPart);
    propertyUpdater.updateMap("dctermsIsFormatOf", WebResource::getDctermsIsFormatOf);
    propertyUpdater.updateMap("dctermsIsPartOf", WebResource::getDctermsIsPartOf);
    propertyUpdater.updateMap("dctermsIssued", WebResource::getDctermsIssued);
    propertyUpdater.updateString("isNextInSequence", WebResource::getIsNextInSequence);
    propertyUpdater.updateMap("webResourceDcRights", WebResource::getWebResourceDcRights);
    propertyUpdater.updateMap("webResourceEdmRights", WebResource::getWebResourceEdmRights);
    propertyUpdater.updateMap("dcType", WebResource::getDcType);
    propertyUpdater.updateArray("owlSameAs", WebResource::getOwlSameAs);
    propertyUpdater.updateString("edmPreview", WebResource::getEdmPreview);
    propertyUpdater.updateArray("svcsHasService", WebResource::getSvcsHasService);
    propertyUpdater.updateArray("dctermsIsReferencedBy", WebResource::getDctermsIsReferencedBy);

    propertyUpdater.updateWebResourceMetaInfo(WebResourceImpl::getWebResourceMetaInfo,
        webResource -> createWebResourceInfo(webResource, ancestorInformation),
        WebResourceMetaInfoUpdater::new);
  }

  private static WebResourceInformation createWebResourceInfo(WebResourceImpl webResource,
      RootAboutWrapper rootAbout) {
    return new WebResourceInformation(rootAbout.getRootAbout(), webResource.getAbout());
  }
}
