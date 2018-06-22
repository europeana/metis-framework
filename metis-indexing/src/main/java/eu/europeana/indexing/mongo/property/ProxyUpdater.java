package eu.europeana.indexing.mongo.property;

import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.storage.MongoServer;

/**
 * Field updater for instances of {@link ProxyImpl}.
 */
public class ProxyUpdater implements PropertyMongoUpdater<ProxyImpl> {

  @Override
  public ProxyImpl update(ProxyImpl retProxy, ProxyImpl proxy, MongoServer mongoServer) {
    Query<ProxyImpl> updateQuery = mongoServer.getDatastore().createQuery(ProxyImpl.class)
        .field("about").equal(proxy.getAbout());
    UpdateOperations<ProxyImpl> ops =
        mongoServer.getDatastore().createUpdateOperations(ProxyImpl.class);
    final UpdateTrigger updateTrigger = new UpdateTrigger();
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dcContributor", ops,
        ProxyImpl::getDcContributor, ProxyImpl::setDcContributor);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dcCoverage", ops,
        ProxyImpl::getDcCoverage, ProxyImpl::setDcCoverage);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dcCreator", ops,
        ProxyImpl::getDcCreator, ProxyImpl::setDcCreator);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dcDate", ops, ProxyImpl::getDcDate,
        ProxyImpl::setDcDate);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dcDescription", ops,
        ProxyImpl::getDcDescription, ProxyImpl::setDcDescription);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dcFormat", ops,
        ProxyImpl::getDcFormat, ProxyImpl::setDcFormat);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dcIdentifier", ops,
        ProxyImpl::getDcIdentifier, ProxyImpl::setDcIdentifier);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dcLanguage", ops,
        ProxyImpl::getDcLanguage, ProxyImpl::setDcLanguage);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dcPublisher", ops,
        ProxyImpl::getDcPublisher, ProxyImpl::setDcPublisher);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dcRelation", ops,
        ProxyImpl::getDcRelation, ProxyImpl::setDcRelation);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dcRights", ops,
        ProxyImpl::getDcRights, ProxyImpl::setDcRights);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dcSource", ops,
        ProxyImpl::getDcSource, ProxyImpl::setDcSource);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dcSubject", ops,
        ProxyImpl::getDcSubject, ProxyImpl::setDcSubject);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dcTitle", ops,
        ProxyImpl::getDcTitle, ProxyImpl::setDcTitle);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dcType", ops, ProxyImpl::getDcType,
        ProxyImpl::setDcType);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsAlternative", ops,
        ProxyImpl::getDctermsAlternative, ProxyImpl::setDctermsAlternative);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsConformsTo", ops,
        ProxyImpl::getDctermsConformsTo, ProxyImpl::setDctermsConformsTo);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsCreated", ops,
        ProxyImpl::getDctermsCreated, ProxyImpl::setDctermsCreated);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsExtent", ops,
        ProxyImpl::getDctermsExtent, ProxyImpl::setDctermsExtent);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsHasFormat", ops,
        ProxyImpl::getDctermsHasFormat, ProxyImpl::setDctermsHasFormat);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsHasPart", ops,
        ProxyImpl::getDctermsHasPart, ProxyImpl::setDctermsHasPart);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsHasVersion", ops,
        ProxyImpl::getDctermsHasVersion, ProxyImpl::setDctermsHasVersion);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsIsFormatOf", ops,
        ProxyImpl::getDctermsIsFormatOf, ProxyImpl::setDctermsIsFormatOf);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsIsPartOf", ops,
        ProxyImpl::getDctermsIsPartOf, ProxyImpl::setDctermsIsPartOf);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsIsReferencedBy", ops,
        ProxyImpl::getDctermsIsReferencedBy, ProxyImpl::setDctermsIsReferencedBy);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsIsReplacedBy", ops,
        ProxyImpl::getDctermsIsReplacedBy, ProxyImpl::setDctermsIsReplacedBy);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsIsRequiredBy", ops,
        ProxyImpl::getDctermsIsRequiredBy, ProxyImpl::setDctermsIsRequiredBy);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsIssued", ops,
        ProxyImpl::getDctermsIssued, ProxyImpl::setDctermsIssued);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsIsVersionOf", ops,
        ProxyImpl::getDctermsIsVersionOf, ProxyImpl::setDctermsIsVersionOf);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsMedium", ops,
        ProxyImpl::getDctermsMedium, ProxyImpl::setDctermsMedium);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsProvenance", ops,
        ProxyImpl::getDctermsProvenance, ProxyImpl::setDctermsProvenance);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsReferences", ops,
        ProxyImpl::getDctermsReferences, ProxyImpl::setDctermsReferences);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsRequires", ops,
        ProxyImpl::getDctermsRequires, ProxyImpl::setDctermsRequires);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsSpatial", ops,
        ProxyImpl::getDctermsSpatial, ProxyImpl::setDctermsSpatial);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsTOC", ops,
        ProxyImpl::getDctermsTOC, ProxyImpl::setDctermsTOC);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "dctermsTemporal", ops,
        ProxyImpl::getDctermsTemporal, ProxyImpl::setDctermsTemporal);
    if (proxy.getEdmType() != null) {
      if (retProxy.getEdmType() == null || !retProxy.getEdmType().equals(proxy.getEdmType())) {
        ops.set("edmType", proxy.getEdmType());
        retProxy.setEdmType(proxy.getEdmType());
        updateTrigger.triggerUpdate();
      }
    } else {
      if (retProxy.getEdmType() != null) {
        ops.unset("edmType");
        updateTrigger.triggerUpdate();
      }
    }
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "edmCurrentLocation", ops,
        ProxyImpl::getEdmCurrentLocation, ProxyImpl::setEdmCurrentLocation);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "edmRights", ops,
        ProxyImpl::getEdmRights, ProxyImpl::setEdmRights);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "edmHasMet", ops,
        ProxyImpl::getEdmHasMet, ProxyImpl::setEdmHasMet);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "edmHasType", ops,
        ProxyImpl::getEdmHasType, ProxyImpl::setEdmHasType);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "year", ops, ProxyImpl::getYear,
        ProxyImpl::setYear);
    FieldUpdateUtils.updateMap(updateTrigger, retProxy, proxy, "edmIsRelatedTo", ops,
        ProxyImpl::getEdmIsRelatedTo, ProxyImpl::setEdmIsRelatedTo);
    FieldUpdateUtils.updateArray(updateTrigger, retProxy, proxy, "edmIsDerivativeOf", ops,
        ProxyImpl::getEdmIsDerivativeOf, ProxyImpl::setEdmIsDerivativeOf);
    FieldUpdateUtils.updateArray(updateTrigger, retProxy, proxy, "edmIsNextInSequence", ops,
        ProxyImpl::getEdmIsNextInSequence, ProxyImpl::setEdmIsNextInSequence);
    FieldUpdateUtils.updateArray(updateTrigger, retProxy, proxy, "edmIsSimilarTo", ops,
        ProxyImpl::getEdmIsSimilarTo, ProxyImpl::setEdmIsSimilarTo);
    FieldUpdateUtils.updateArray(updateTrigger, retProxy, proxy, "edmIsSuccessorOf", ops,
        ProxyImpl::getEdmIsSuccessorOf, ProxyImpl::setEdmIsSuccessorOf);
    FieldUpdateUtils.updateArray(updateTrigger, retProxy, proxy, "edmWasPresentAt", ops,
        ProxyImpl::getEdmWasPresentAt, ProxyImpl::setEdmWasPresentAt);
    FieldUpdateUtils.updateArray(updateTrigger, retProxy, proxy, "proxyIn", ops,
        ProxyImpl::getProxyIn, ProxyImpl::setProxyIn);
    FieldUpdateUtils.updateString(updateTrigger, retProxy, proxy, "proxyFor", ops,
        ProxyImpl::getProxyFor, ProxyImpl::setProxyFor);
    FieldUpdateUtils.updateString(updateTrigger, retProxy, proxy, "edmIsRepresentationOf", ops,
        ProxyImpl::getEdmIsRepresentationOf, ProxyImpl::setEdmIsRepresentationOf);

    if (updateTrigger.isUpdateTriggered()) {
      mongoServer.getDatastore().update(updateQuery, ops);
    }

    return retProxy;
  }
}
