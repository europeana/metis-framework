package eu.europeana.indexing.mongo.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.corelib.definitions.edm.entity.WebResource;
import eu.europeana.corelib.definitions.edm.model.metainfo.WebResourceMetaInfo;
import eu.europeana.corelib.edm.model.metainfo.WebResourceMetaInfoImpl;
import eu.europeana.corelib.solr.entity.WebResourceImpl;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class WebResourceUpdaterTest extends MongoEntityUpdaterTest<WebResourceImpl> {

  @Override
  WebResourceImpl createEmptyMongoEntity() {
    return new WebResourceImpl();
  }

  @Test
  void testGetObjectClass() {
    assertEquals(WebResourceImpl.class, new WebResourceUpdater().getObjectClass());
  }

  @Test
  void testUpdate() {

    // Craete objects for execution
    final WebResourceUpdater updater = new WebResourceUpdater();
    @SuppressWarnings("unchecked") final MongoPropertyUpdater<WebResourceImpl> propertyUpdater = mock(
        MongoPropertyUpdater.class);

    // Make the call
    final RootAboutWrapper rootAbout = new RootAboutWrapper("root about");
    updater.update(propertyUpdater, rootAbout);

    // Test all the values not in the web resource meta info.
    testMapPropertyUpdate(propertyUpdater, "dcDescription", WebResource::setDcDescription);
    testMapPropertyUpdate(propertyUpdater, "dcFormat", WebResource::setDcFormat);
    testMapPropertyUpdate(propertyUpdater, "dcCreator", WebResource::setDcCreator);
    testMapPropertyUpdate(propertyUpdater, "dcSource", WebResource::setDcSource);
    testMapPropertyUpdate(propertyUpdater, "dctermsConformsTo", WebResource::setDctermsConformsTo);
    testMapPropertyUpdate(propertyUpdater, "dctermsCreated", WebResource::setDctermsCreated);
    testMapPropertyUpdate(propertyUpdater, "dctermsExtent", WebResource::setDctermsExtent);
    testMapPropertyUpdate(propertyUpdater, "dctermsHasPart", WebResource::setDctermsHasPart);
    testMapPropertyUpdate(propertyUpdater, "dctermsIsFormatOf", WebResource::setDctermsIsFormatOf);
    testMapPropertyUpdate(propertyUpdater, "dctermsIsPartOf", WebResource::setDctermsIsPartOf);
    testMapPropertyUpdate(propertyUpdater, "dctermsIssued", WebResource::setDctermsIssued);
    testStringPropertyUpdate(propertyUpdater, "isNextInSequence", WebResource::setIsNextInSequence);
    testMapPropertyUpdate(propertyUpdater, "webResourceDcRights",
        WebResource::setWebResourceDcRights);
    testMapPropertyUpdate(propertyUpdater, "webResourceEdmRights",
        WebResource::setWebResourceEdmRights);
    testMapPropertyUpdate(propertyUpdater, "dcType", WebResource::setDcType);
    testArrayPropertyUpdate(propertyUpdater, "owlSameAs", WebResource::setOwlSameAs);
    testStringPropertyUpdate(propertyUpdater, "edmPreview", WebResource::setEdmPreview);
    testArrayPropertyUpdate(propertyUpdater, "svcsHasService", WebResource::setSvcsHasService);
    testArrayPropertyUpdate(propertyUpdater, "dctermsIsReferencedBy",
        WebResource::setDctermsIsReferencedBy);

    // Create a test object with the right value for testing the web resource meta info.
    final WebResourceImpl testEntity = createEmptyMongoEntity();
    final String aboutValue = "about value";
    final WebResourceMetaInfoImpl webResourceMetaInfo = new WebResourceMetaInfoImpl();
    testEntity.setAbout(aboutValue);
    testEntity.setWebResourceMetaInfo(webResourceMetaInfo);

    // Check that the updater was called with valid values for the web resource meta info.
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<WebResourceImpl, WebResourceMetaInfo>> getterCaptor = ArgumentCaptor
        .forClass(Function.class);
    @SuppressWarnings("unchecked") final ArgumentCaptor<Function<WebResourceImpl, WebResourceInformation>> ancestorInfoGetterCaptor = ArgumentCaptor
        .forClass(Function.class);
    verify(propertyUpdater, times(1))
        .updateWebResourceMetaInfo(getterCaptor.capture(), ancestorInfoGetterCaptor.capture(), any());
    assertSame(webResourceMetaInfo, getterCaptor.getValue().apply(testEntity));
    final WebResourceInformation info = ancestorInfoGetterCaptor.getValue().apply(testEntity);
    assertNotNull(info);
    assertEquals(aboutValue, info.getWebResourceAbout());
    assertEquals(rootAbout.getRootAbout(), info.getRootAbout());

    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }
}
