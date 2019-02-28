package eu.europeana.indexing.mongo.property;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import org.junit.jupiter.api.Test;

class ProxyUpdaterTest extends MongoEntityUpdaterTest<ProxyImpl> {

  @Override
  ProxyImpl createEmptyMongoEntity() {
    return new ProxyImpl();
  }

  @Test
  void testGetObjectClass() {
    assertEquals(ProxyImpl.class, new ProxyUpdater().getObjectClass());
  }

  @Test
  void testUpdate() {

    // Craete objects for execution
    final ProxyUpdater updater = new ProxyUpdater();
    @SuppressWarnings("unchecked") final MongoPropertyUpdater<ProxyImpl> propertyUpdater = mock(
        MongoPropertyUpdater.class);

    // Make the call
    updater.update(propertyUpdater);

    // Test all the values
    testMapPropertyUpdate(propertyUpdater, "dcContributor", ProxyImpl::setDcContributor);
    testMapPropertyUpdate(propertyUpdater, "dcCoverage", ProxyImpl::setDcCoverage);
    testMapPropertyUpdate(propertyUpdater, "dcCreator", ProxyImpl::setDcCreator);
    testMapPropertyUpdate(propertyUpdater, "dcDate", ProxyImpl::setDcDate);
    testMapPropertyUpdate(propertyUpdater, "dcDescription", ProxyImpl::setDcDescription);
    testMapPropertyUpdate(propertyUpdater, "dcFormat", ProxyImpl::setDcFormat);
    testMapPropertyUpdate(propertyUpdater, "dcIdentifier", ProxyImpl::setDcIdentifier);
    testMapPropertyUpdate(propertyUpdater, "dcLanguage", ProxyImpl::setDcLanguage);
    testMapPropertyUpdate(propertyUpdater, "dcPublisher", ProxyImpl::setDcPublisher);
    testMapPropertyUpdate(propertyUpdater, "dcRelation", ProxyImpl::setDcRelation);
    testMapPropertyUpdate(propertyUpdater, "dcRights", ProxyImpl::setDcRights);
    testMapPropertyUpdate(propertyUpdater, "dcSource", ProxyImpl::setDcSource);
    testMapPropertyUpdate(propertyUpdater, "dcSubject", ProxyImpl::setDcSubject);
    testMapPropertyUpdate(propertyUpdater, "dcTitle", ProxyImpl::setDcTitle);
    testMapPropertyUpdate(propertyUpdater, "dcType", ProxyImpl::setDcType);
    testMapPropertyUpdate(propertyUpdater, "dctermsAlternative", ProxyImpl::setDctermsAlternative);
    testMapPropertyUpdate(propertyUpdater, "dctermsConformsTo", ProxyImpl::setDctermsConformsTo);
    testMapPropertyUpdate(propertyUpdater, "dctermsCreated", ProxyImpl::setDctermsCreated);
    testMapPropertyUpdate(propertyUpdater, "dctermsExtent", ProxyImpl::setDctermsExtent);
    testMapPropertyUpdate(propertyUpdater, "dctermsHasFormat", ProxyImpl::setDctermsHasFormat);
    testMapPropertyUpdate(propertyUpdater, "dctermsHasPart", ProxyImpl::setDctermsHasPart);
    testMapPropertyUpdate(propertyUpdater, "dctermsHasVersion", ProxyImpl::setDctermsHasVersion);
    testMapPropertyUpdate(propertyUpdater, "dctermsIsFormatOf", ProxyImpl::setDctermsIsFormatOf);
    testMapPropertyUpdate(propertyUpdater, "dctermsIsPartOf", ProxyImpl::setDctermsIsPartOf);
    testMapPropertyUpdate(propertyUpdater, "dctermsIsReferencedBy",
        ProxyImpl::setDctermsIsReferencedBy);
    testMapPropertyUpdate(propertyUpdater, "dctermsIsReplacedBy",
        ProxyImpl::setDctermsIsReplacedBy);
    testMapPropertyUpdate(propertyUpdater, "dctermsIsRequiredBy",
        ProxyImpl::setDctermsIsRequiredBy);
    testMapPropertyUpdate(propertyUpdater, "dctermsIssued", ProxyImpl::setDctermsIssued);
    testMapPropertyUpdate(propertyUpdater, "dctermsIsVersionOf", ProxyImpl::setDctermsIsVersionOf);
    testMapPropertyUpdate(propertyUpdater, "dctermsMedium", ProxyImpl::setDctermsMedium);
    testMapPropertyUpdate(propertyUpdater, "dctermsProvenance", ProxyImpl::setDctermsProvenance);
    testMapPropertyUpdate(propertyUpdater, "dctermsReferences", ProxyImpl::setDctermsReferences);
    testMapPropertyUpdate(propertyUpdater, "dctermsRequires", ProxyImpl::setDctermsRequires);
    testMapPropertyUpdate(propertyUpdater, "dctermsSpatial", ProxyImpl::setDctermsSpatial);
    testMapPropertyUpdate(propertyUpdater, "dctermsTOC", ProxyImpl::setDctermsTOC);
    testMapPropertyUpdate(propertyUpdater, "dctermsTemporal", ProxyImpl::setDctermsTemporal);
    testObjectPropertyUpdate(propertyUpdater, "edmType", ProxyImpl::setEdmType, DocType.VIDEO);
    testMapPropertyUpdate(propertyUpdater, "edmCurrentLocation", ProxyImpl::setEdmCurrentLocation);
    testMapPropertyUpdate(propertyUpdater, "edmRights", ProxyImpl::setEdmRights);
    testMapPropertyUpdate(propertyUpdater, "edmHasMet", ProxyImpl::setEdmHasMet);
    testMapPropertyUpdate(propertyUpdater, "edmHasType", ProxyImpl::setEdmHasType);
    testArrayPropertyUpdate(propertyUpdater, "edmIncorporates", ProxyImpl::setEdmIncorporates);
    testMapPropertyUpdate(propertyUpdater, "dctermsReplaces", ProxyImpl::setDctermsReplaces);
    testMapPropertyUpdate(propertyUpdater, "year", ProxyImpl::setYear);
    testMapPropertyUpdate(propertyUpdater, "edmIsRelatedTo", ProxyImpl::setEdmIsRelatedTo);
    testArrayPropertyUpdate(propertyUpdater, "edmIsDerivativeOf", ProxyImpl::setEdmIsDerivativeOf);
    testArrayPropertyUpdate(propertyUpdater, "edmIsNextInSequence",
        ProxyImpl::setEdmIsNextInSequence);
    testArrayPropertyUpdate(propertyUpdater, "edmIsSimilarTo", ProxyImpl::setEdmIsSimilarTo);
    testArrayPropertyUpdate(propertyUpdater, "edmIsSuccessorOf", ProxyImpl::setEdmIsSuccessorOf);
    testArrayPropertyUpdate(propertyUpdater, "edmRealizes", ProxyImpl::setEdmRealizes);
    testArrayPropertyUpdate(propertyUpdater, "edmWasPresentAt", ProxyImpl::setEdmWasPresentAt);
    testArrayPropertyUpdate(propertyUpdater, "proxyIn", ProxyImpl::setProxyIn);
    testStringPropertyUpdate(propertyUpdater, "proxyFor", ProxyImpl::setProxyFor);
    testStringPropertyUpdate(propertyUpdater, "edmIsRepresentationOf",
        ProxyImpl::setEdmIsRepresentationOf);
    testObjectPropertyUpdate(propertyUpdater, "europeanaProxy", ProxyImpl::setEuropeanaProxy,
        Boolean.TRUE);

    // And that should be it.
    verifyNoMoreInteractions(propertyUpdater);
  }
}
