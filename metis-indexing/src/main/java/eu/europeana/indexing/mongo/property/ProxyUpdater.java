package eu.europeana.indexing.mongo.property;

import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.storage.MongoServer;

public class ProxyUpdater implements PropertyMongoUpdater<ProxyImpl> {

  @Override
  public ProxyImpl update(ProxyImpl retProxy, ProxyImpl proxy, MongoServer mongoServer) {
    Query<ProxyImpl> updateQuery = mongoServer.getDatastore().createQuery(ProxyImpl.class)
        .field("about").equal(proxy.getAbout());
    UpdateOperations<ProxyImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(ProxyImpl.class);
    boolean update = false;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dcContributor", ops,
        ProxyImpl::getDcContributor, ProxyImpl::setDcContributor) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dcCoverage", ops,
        ProxyImpl::getDcCoverage, ProxyImpl::setDcCoverage) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dcCreator", ops, ProxyImpl::getDcCreator,
        ProxyImpl::setDcCreator) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dcDate", ops, ProxyImpl::getDcDate,
        ProxyImpl::setDcDate) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dcDescription", ops,
        ProxyImpl::getDcDescription, ProxyImpl::setDcDescription) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dcFormat", ops, ProxyImpl::getDcFormat,
        ProxyImpl::setDcFormat) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dcIdentifier", ops,
        ProxyImpl::getDcIdentifier, ProxyImpl::setDcIdentifier) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dcLanguage", ops,
        ProxyImpl::getDcLanguage, ProxyImpl::setDcLanguage) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dcPublisher", ops,
        ProxyImpl::getDcPublisher, ProxyImpl::setDcPublisher) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dcRelation", ops,
        ProxyImpl::getDcRelation, ProxyImpl::setDcRelation) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dcRights", ops, ProxyImpl::getDcRights,
        ProxyImpl::setDcRights) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dcSource", ops, ProxyImpl::getDcSource,
        ProxyImpl::setDcSource) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dcSubject", ops, ProxyImpl::getDcSubject,
        ProxyImpl::setDcSubject) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dcTitle", ops, ProxyImpl::getDcTitle,
        ProxyImpl::setDcTitle) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dcType", ops, ProxyImpl::getDcType,
        ProxyImpl::setDcType) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsAlternative", ops,
        ProxyImpl::getDctermsAlternative, ProxyImpl::setDctermsAlternative) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsConformsTo", ops,
        ProxyImpl::getDctermsConformsTo, ProxyImpl::setDctermsConformsTo) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsCreated", ops,
        ProxyImpl::getDctermsCreated, ProxyImpl::setDctermsCreated) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsExtent", ops,
        ProxyImpl::getDctermsExtent, ProxyImpl::setDctermsExtent) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsHasFormat", ops,
        ProxyImpl::getDctermsHasFormat, ProxyImpl::setDctermsHasFormat) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsHasPart", ops,
        ProxyImpl::getDctermsHasPart, ProxyImpl::setDctermsHasPart) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsHasVersion", ops,
        ProxyImpl::getDctermsHasVersion, ProxyImpl::setDctermsHasVersion) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsIsFormatOf", ops,
        ProxyImpl::getDctermsIsFormatOf, ProxyImpl::setDctermsIsFormatOf) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsIsPartOf", ops,
        ProxyImpl::getDctermsIsPartOf, ProxyImpl::setDctermsIsPartOf) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsIsReferencedBy", ops,
        ProxyImpl::getDctermsIsReferencedBy, ProxyImpl::setDctermsIsReferencedBy) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsIsReplacedBy", ops,
        ProxyImpl::getDctermsIsReplacedBy, ProxyImpl::setDctermsIsReplacedBy) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsIsRequiredBy", ops,
        ProxyImpl::getDctermsIsRequiredBy, ProxyImpl::setDctermsIsRequiredBy) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsIssued", ops,
        ProxyImpl::getDctermsIssued, ProxyImpl::setDctermsIssued) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsIsVersionOf", ops,
        ProxyImpl::getDctermsIsVersionOf, ProxyImpl::setDctermsIsVersionOf) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsMedium", ops,
        ProxyImpl::getDctermsMedium, ProxyImpl::setDctermsMedium) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsProvenance", ops,
        ProxyImpl::getDctermsProvenance, ProxyImpl::setDctermsProvenance) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsReferences", ops,
        ProxyImpl::getDctermsReferences, ProxyImpl::setDctermsReferences) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsRequires", ops,
        ProxyImpl::getDctermsRequires, ProxyImpl::setDctermsRequires) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsSpatial", ops,
        ProxyImpl::getDctermsSpatial, ProxyImpl::setDctermsSpatial) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsTOC", ops,
        ProxyImpl::getDctermsTOC, ProxyImpl::setDctermsTOC) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "dctermsTemporal", ops,
        ProxyImpl::getDctermsTemporal, ProxyImpl::setDctermsTemporal) || update;
    if (proxy.getEdmType() != null) {
      if (retProxy.getEdmType() == null || !retProxy.getEdmType().equals(proxy.getEdmType())) {
        ops.set("edmType", proxy.getEdmType());
        retProxy.setEdmType(proxy.getEdmType());
        update = true;
      }
    } else {
      if (retProxy.getEdmType() != null) {
        ops.unset("edmType");
        update = true;
      }
    }
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "edmCurrentLocation", ops,
        ProxyImpl::getEdmCurrentLocation, ProxyImpl::setEdmCurrentLocation) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "edmRights", ops, ProxyImpl::getEdmRights,
        ProxyImpl::setEdmRights) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "edmHasMet", ops, ProxyImpl::getEdmHasMet,
        ProxyImpl::setEdmHasMet) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "edmHasType", ops,
        ProxyImpl::getEdmHasType, ProxyImpl::setEdmHasType) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "year", ops, ProxyImpl::getYear,
        ProxyImpl::setYear) || update;
    update = FieldUpdateUtils.updateMap(retProxy, proxy, "edmIsRelatedTo", ops,
        ProxyImpl::getEdmIsRelatedTo, ProxyImpl::setEdmIsRelatedTo) || update;
    update = FieldUpdateUtils.updateArray(retProxy, proxy, "edmIsDerivativeOf", ops,
        ProxyImpl::getEdmIsDerivativeOf, ProxyImpl::setEdmIsDerivativeOf) || update;
    update = FieldUpdateUtils.updateArray(retProxy, proxy, "edmIsNextInSequence", ops,
        ProxyImpl::getEdmIsNextInSequence, ProxyImpl::setEdmIsNextInSequence) || update;
    update = FieldUpdateUtils.updateArray(retProxy, proxy, "edmIsSimilarTo", ops,
        ProxyImpl::getEdmIsSimilarTo, ProxyImpl::setEdmIsSimilarTo) || update;
    update = FieldUpdateUtils.updateArray(retProxy, proxy, "edmIsSuccessorOf", ops,
        ProxyImpl::getEdmIsSuccessorOf, ProxyImpl::setEdmIsSuccessorOf) || update;
    update = FieldUpdateUtils.updateArray(retProxy, proxy, "edmWasPresentAt", ops,
        ProxyImpl::getEdmWasPresentAt, ProxyImpl::setEdmWasPresentAt) || update;
    update = FieldUpdateUtils.updateArray(retProxy, proxy, "proxyIn", ops, ProxyImpl::getProxyIn,
        ProxyImpl::setProxyIn) || update;
    update = FieldUpdateUtils.updateString(retProxy, proxy, "proxyFor", ops, ProxyImpl::getProxyFor,
        ProxyImpl::setProxyFor) || update;
    update = FieldUpdateUtils.updateString(retProxy, proxy, "edmIsRepresentationOf", ops,
        ProxyImpl::getEdmIsRepresentationOf, ProxyImpl::setEdmIsRepresentationOf) || update;

    if (update) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }

    return retProxy;
  }
}
