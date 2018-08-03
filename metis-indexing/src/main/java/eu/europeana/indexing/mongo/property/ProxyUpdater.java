package eu.europeana.indexing.mongo.property;

import eu.europeana.corelib.solr.entity.ProxyImpl;

/**
 * Field updater for instances of {@link ProxyImpl}.
 */
public class ProxyUpdater extends AbstractIsolatedEdmEntityUpdater<ProxyImpl> {

  @Override
  protected Class<ProxyImpl> getObjectClass() {
    return ProxyImpl.class;
  }

  @Override
  protected void update(MongoPropertyUpdater<ProxyImpl> propertyUpdater) {
    propertyUpdater.updateMap("dcContributor", ProxyImpl::getDcContributor);
    propertyUpdater.updateMap("dcCoverage", ProxyImpl::getDcCoverage);
    propertyUpdater.updateMap("dcCreator", ProxyImpl::getDcCreator);
    propertyUpdater.updateMap("dcDate", ProxyImpl::getDcDate);
    propertyUpdater.updateMap("dcDescription", ProxyImpl::getDcDescription);
    propertyUpdater.updateMap("dcFormat", ProxyImpl::getDcFormat);
    propertyUpdater.updateMap("dcIdentifier", ProxyImpl::getDcIdentifier);
    propertyUpdater.updateMap("dcLanguage", ProxyImpl::getDcLanguage);
    propertyUpdater.updateMap("dcPublisher", ProxyImpl::getDcPublisher);
    propertyUpdater.updateMap("dcRelation", ProxyImpl::getDcRelation);
    propertyUpdater.updateMap("dcRights", ProxyImpl::getDcRights);
    propertyUpdater.updateMap("dcSource", ProxyImpl::getDcSource);
    propertyUpdater.updateMap("dcSubject", ProxyImpl::getDcSubject);
    propertyUpdater.updateMap("dcTitle", ProxyImpl::getDcTitle);
    propertyUpdater.updateMap("dcType", ProxyImpl::getDcType);
    propertyUpdater.updateMap("dctermsAlternative", ProxyImpl::getDctermsAlternative);
    propertyUpdater.updateMap("dctermsConformsTo", ProxyImpl::getDctermsConformsTo);
    propertyUpdater.updateMap("dctermsCreated", ProxyImpl::getDctermsCreated);
    propertyUpdater.updateMap("dctermsExtent", ProxyImpl::getDctermsExtent);
    propertyUpdater.updateMap("dctermsHasFormat", ProxyImpl::getDctermsHasFormat);
    propertyUpdater.updateMap("dctermsHasPart", ProxyImpl::getDctermsHasPart);
    propertyUpdater.updateMap("dctermsHasVersion", ProxyImpl::getDctermsHasVersion);
    propertyUpdater.updateMap("dctermsIsFormatOf", ProxyImpl::getDctermsIsFormatOf);
    propertyUpdater.updateMap("dctermsIsPartOf", ProxyImpl::getDctermsIsPartOf);
    propertyUpdater.updateMap("dctermsIsReferencedBy", ProxyImpl::getDctermsIsReferencedBy);
    propertyUpdater.updateMap("dctermsIsReplacedBy", ProxyImpl::getDctermsIsReplacedBy);
    propertyUpdater.updateMap("dctermsIsRequiredBy", ProxyImpl::getDctermsIsRequiredBy);
    propertyUpdater.updateMap("dctermsIssued", ProxyImpl::getDctermsIssued);
    propertyUpdater.updateMap("dctermsIsVersionOf", ProxyImpl::getDctermsIsVersionOf);
    propertyUpdater.updateMap("dctermsMedium", ProxyImpl::getDctermsMedium);
    propertyUpdater.updateMap("dctermsProvenance", ProxyImpl::getDctermsProvenance);
    propertyUpdater.updateMap("dctermsReferences", ProxyImpl::getDctermsReferences);
    propertyUpdater.updateMap("dctermsRequires", ProxyImpl::getDctermsRequires);
    propertyUpdater.updateMap("dctermsSpatial", ProxyImpl::getDctermsSpatial);
    propertyUpdater.updateMap("dctermsTOC", ProxyImpl::getDctermsTOC);
    propertyUpdater.updateMap("dctermsTemporal", ProxyImpl::getDctermsTemporal);
    propertyUpdater.updateObject("edmType", ProxyImpl::getEdmType);
    propertyUpdater.updateMap("edmCurrentLocation", ProxyImpl::getEdmCurrentLocation);
    propertyUpdater.updateMap("edmRights", ProxyImpl::getEdmRights);
    propertyUpdater.updateMap("edmHasMet", ProxyImpl::getEdmHasMet);
    propertyUpdater.updateMap("edmHasType", ProxyImpl::getEdmHasType);
    propertyUpdater.updateArray("edmIncorporates", ProxyImpl::getEdmIncorporates);
    propertyUpdater.updateMap("dctermsReplaces", ProxyImpl::getDctermsReplaces);
    propertyUpdater.updateMap("year", ProxyImpl::getYear);
    propertyUpdater.updateMap("edmIsRelatedTo", ProxyImpl::getEdmIsRelatedTo);
    propertyUpdater.updateArray("edmIsDerivativeOf", ProxyImpl::getEdmIsDerivativeOf);
    propertyUpdater.updateArray("edmIsNextInSequence", ProxyImpl::getEdmIsNextInSequence);
    propertyUpdater.updateArray("edmIsSimilarTo", ProxyImpl::getEdmIsSimilarTo);
    propertyUpdater.updateArray("edmIsSuccessorOf", ProxyImpl::getEdmIsSuccessorOf);
    propertyUpdater.updateArray("edmRealizes", ProxyImpl::getEdmRealizes);
    propertyUpdater.updateArray("edmWasPresentAt", ProxyImpl::getEdmWasPresentAt);
    propertyUpdater.updateArray("proxyIn", ProxyImpl::getProxyIn);
    propertyUpdater.updateString("proxyFor", ProxyImpl::getProxyFor);
    propertyUpdater.updateString("edmIsRepresentationOf", ProxyImpl::getEdmIsRepresentationOf);
    propertyUpdater.updateObject("europeanaProxy", ProxyImpl::isEuropeanaProxy);
  }
}
