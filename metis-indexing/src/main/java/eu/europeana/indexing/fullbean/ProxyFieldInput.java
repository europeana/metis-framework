package eu.europeana.indexing.fullbean;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import eu.europeana.corelib.definitions.jibx.EdmType;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.IsNextInSequence;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.ResourceType;
import eu.europeana.corelib.definitions.jibx.Type2;
import eu.europeana.corelib.definitions.solr.DocType;
import eu.europeana.corelib.solr.entity.ProxyImpl;

/**
 * Converts a {@link ProxyType} from an {@link eu.europeana.corelib.definitions.jibx.RDF} to a
 * {@link ProxyImpl} for a {@link eu.europeana.corelib.definitions.edm.beans.FullBean}.
 */
final class ProxyFieldInput implements Function<ProxyType, ProxyImpl> {

  @Override
  public ProxyImpl apply(ProxyType proxy) {

    final ProxyImpl mongoProxy = new ProxyImpl();

    mongoProxy.setAbout(proxy.getAbout());
    if (proxy.getEuropeanaProxy() != null) {
      mongoProxy.setEuropeanaProxy(proxy.getEuropeanaProxy().isEuropeanaProxy());
    }
    mongoProxy.setEdmCurrentLocation(
        FieldInputUtils.createResourceOrLiteralMapFromString(proxy.getCurrentLocation()));

    List<IsNextInSequence> seqList = proxy.getIsNextInSequenceList();

    if (seqList != null) {

      String[] seqarray = new String[seqList.size()];
      for (int i = 0; i < seqarray.length; i++) {
        seqarray[i] = seqList.get(i).getResource();
      }
      mongoProxy.setEdmIsNextInSequence(seqarray);
    }

    final String docType = Optional.ofNullable(proxy.getType()).map(Type2::getType)
        .map(EdmType::xmlValue).orElse(null);
    mongoProxy.setEdmType(DocType.safeValueOf(docType));

    mongoProxy.setProxyFor(
        Optional.ofNullable(proxy.getProxyFor()).map(ResourceType::getResource).orElse(null));
    mongoProxy.setProxyIn(FieldInputUtils.resourceListToArray(proxy.getProxyInList()));
    mongoProxy.setEdmHasMet(FieldInputUtils.createResourceMapFromList(proxy.getHasMetList()));
    mongoProxy.setYear(FieldInputUtils.createLiteralMapFromList(proxy.getYearList()));
    mongoProxy
        .setEdmHasType(FieldInputUtils.createResourceOrLiteralMapFromList(proxy.getHasTypeList()));
    mongoProxy
        .setEdmHasType(FieldInputUtils.createResourceOrLiteralMapFromList(proxy.getHasTypeList()));
    mongoProxy.setEdmIncorporates(FieldInputUtils.resourceListToArray(proxy.getIncorporateList()));
    mongoProxy
        .setEdmIsDerivativeOf(FieldInputUtils.resourceListToArray(proxy.getIsDerivativeOfList()));
    mongoProxy.setEdmIsRelatedTo(
        FieldInputUtils.createResourceOrLiteralMapFromList(proxy.getIsRelatedToList()));
    if (proxy.getIsRepresentationOf() != null) {
      mongoProxy.setEdmIsRepresentationOf(proxy.getIsRepresentationOf().getResource());
    }
    mongoProxy.setEdmIsSimilarTo(FieldInputUtils.resourceListToArray(proxy.getIsSimilarToList()));
    mongoProxy.setEdmRealizes(FieldInputUtils.resourceListToArray(proxy.getRealizeList()));
    mongoProxy
        .setEdmIsSuccessorOf(FieldInputUtils.resourceListToArray(proxy.getIsSuccessorOfList()));
    List<Choice> europeanaTypeList = proxy.getChoiceList();
    if (europeanaTypeList != null) {
      for (Choice europeanaType : europeanaTypeList) {
        applyToChoice(europeanaType, mongoProxy);
      }
    }
    return mongoProxy;
  }

  private static void applyToChoice(Choice europeanaType, ProxyImpl mongoProxy) {
    if (europeanaType.ifAlternative()) {
      mongoProxy.setDctermsAlternative(FieldInputUtils.mergeMaps(mongoProxy.getDctermsAlternative(),
          FieldInputUtils.createLiteralMapFromString(europeanaType.getAlternative())));
    }

    if (europeanaType.ifConformsTo()) {
      mongoProxy.setDctermsConformsTo(FieldInputUtils.mergeMaps(mongoProxy.getDctermsConformsTo(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getConformsTo())));
    }

    if (europeanaType.ifCreated()) {
      mongoProxy.setDctermsCreated(FieldInputUtils.mergeMaps(mongoProxy.getDctermsCreated(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getCreated())));
    }

    if (europeanaType.ifExtent()) {
      mongoProxy.setDctermsExtent(FieldInputUtils.mergeMaps(mongoProxy.getDctermsExtent(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getExtent())));
    }

    if (europeanaType.ifHasFormat()) {
      mongoProxy.setDctermsHasFormat(FieldInputUtils.mergeMaps(mongoProxy.getDctermsHasFormat(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getHasFormat())));
    }

    if (europeanaType.ifHasPart()) {
      mongoProxy.setDctermsHasPart(FieldInputUtils.mergeMaps(mongoProxy.getDctermsHasPart(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getHasPart())));
    }

    if (europeanaType.ifHasVersion()) {
      mongoProxy.setDctermsHasVersion(FieldInputUtils.mergeMaps(mongoProxy.getDctermsHasVersion(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getHasVersion())));
    }

    if (europeanaType.ifIsFormatOf()) {
      mongoProxy.setDctermsIsFormatOf(FieldInputUtils.mergeMaps(mongoProxy.getDctermsIsFormatOf(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getIsFormatOf())));
    }

    if (europeanaType.ifIsPartOf()) {
      mongoProxy.setDctermsIsPartOf(FieldInputUtils.mergeMaps(mongoProxy.getDctermsIsPartOf(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getIsPartOf())));
    }

    if (europeanaType.ifIsReferencedBy()) {
      mongoProxy.setDctermsIsReferencedBy(FieldInputUtils.mergeMaps(
          mongoProxy.getDctermsIsReferencedBy(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getIsReferencedBy())));
    }

    if (europeanaType.ifIsReplacedBy()) {
      mongoProxy.setDctermsIsReplacedBy(FieldInputUtils.mergeMaps(
          mongoProxy.getDctermsIsReplacedBy(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getIsReplacedBy())));
    }

    if (europeanaType.ifIsRequiredBy()) {
      mongoProxy.setDctermsIsRequiredBy(FieldInputUtils.mergeMaps(
          mongoProxy.getDctermsIsRequiredBy(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getIsRequiredBy())));
    }

    if (europeanaType.ifIssued()) {
      mongoProxy.setDctermsIssued(FieldInputUtils.mergeMaps(mongoProxy.getDctermsIssued(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getIssued())));
    }

    if (europeanaType.ifIsVersionOf()) {
      mongoProxy.setDctermsIsVersionOf(FieldInputUtils.mergeMaps(mongoProxy.getDctermsIsVersionOf(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getIsVersionOf())));
    }

    if (europeanaType.ifMedium()) {
      mongoProxy.setDctermsMedium(FieldInputUtils.mergeMaps(mongoProxy.getDctermsMedium(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getMedium())));
    }

    if (europeanaType.ifProvenance()) {
      mongoProxy.setDctermsProvenance(FieldInputUtils.mergeMaps(mongoProxy.getDctermsProvenance(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getProvenance())));
    }

    if (europeanaType.ifReferences()) {
      mongoProxy.setDctermsReferences(FieldInputUtils.mergeMaps(mongoProxy.getDctermsReferences(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getReferences())));
    }

    if (europeanaType.ifReplaces()) {
      mongoProxy.setDctermsReplaces(FieldInputUtils.mergeMaps(mongoProxy.getDctermsReplaces(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getReplaces())));
    }

    if (europeanaType.ifRequires()) {
      mongoProxy.setDctermsRequires(FieldInputUtils.mergeMaps(mongoProxy.getDctermsRequires(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getRequires())));
    }

    if (europeanaType.ifSpatial()) {
      mongoProxy.setDctermsSpatial(FieldInputUtils.mergeMaps(mongoProxy.getDctermsSpatial(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getSpatial())));
    }

    if (europeanaType.ifTableOfContents()) {
      mongoProxy.setDctermsTOC(FieldInputUtils.mergeMaps(mongoProxy.getDctermsTOC(), FieldInputUtils
          .createResourceOrLiteralMapFromString(europeanaType.getTableOfContents())));
    }

    if (europeanaType.ifTemporal()) {
      mongoProxy.setDctermsTemporal(FieldInputUtils.mergeMaps(mongoProxy.getDctermsTemporal(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getTemporal())));
    }

    if (europeanaType.ifContributor()) {
      mongoProxy.setDcContributor(FieldInputUtils.mergeMaps(mongoProxy.getDcContributor(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getContributor())));
    }

    if (europeanaType.ifCoverage()) {
      mongoProxy.setDcCoverage(FieldInputUtils.mergeMaps(mongoProxy.getDcCoverage(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getCoverage())));
    }

    if (europeanaType.ifCreator()) {
      mongoProxy.setDcCreator(FieldInputUtils.mergeMaps(mongoProxy.getDcCreator(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getCreator())));
    }

    if (europeanaType.ifDate()) {
      mongoProxy.setDcDate(FieldInputUtils.mergeMaps(mongoProxy.getDcDate(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getDate())));
    }

    if (europeanaType.ifDescription()) {
      mongoProxy.setDcDescription(FieldInputUtils.mergeMaps(mongoProxy.getDcDescription(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getDescription())));
    }

    if (europeanaType.ifFormat()) {
      mongoProxy.setDcFormat(FieldInputUtils.mergeMaps(mongoProxy.getDcFormat(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getFormat())));
    }

    if (europeanaType.ifIdentifier()) {
      mongoProxy.setDcIdentifier(FieldInputUtils.mergeMaps(mongoProxy.getDcIdentifier(),
          FieldInputUtils.createLiteralMapFromString(europeanaType.getIdentifier())));
    }

    if (europeanaType.ifLanguage()) {
      mongoProxy.setDcLanguage(FieldInputUtils.mergeMaps(mongoProxy.getDcLanguage(),
          FieldInputUtils.createLiteralMapFromString(europeanaType.getLanguage())));
    }

    if (europeanaType.ifPublisher()) {
      mongoProxy.setDcPublisher(FieldInputUtils.mergeMaps(mongoProxy.getDcPublisher(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getPublisher())));
    }

    if (europeanaType.ifRelation()) {
      mongoProxy.setDcRelation(FieldInputUtils.mergeMaps(mongoProxy.getDcRelation(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getRelation())));
    }

    if (europeanaType.ifRights()) {
      mongoProxy.setDcRights(FieldInputUtils.mergeMaps(mongoProxy.getDcRights(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getRights())));
    }

    if (europeanaType.ifSource()) {
      mongoProxy.setDcSource(FieldInputUtils.mergeMaps(mongoProxy.getDcSource(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getSource())));
    }

    if (europeanaType.ifSubject()) {
      mongoProxy.setDcSubject(FieldInputUtils.mergeMaps(mongoProxy.getDcSubject(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getSubject())));
    }

    if (europeanaType.ifTitle()) {
      mongoProxy.setDcTitle(FieldInputUtils.mergeMaps(mongoProxy.getDcTitle(),
          FieldInputUtils.createLiteralMapFromString(europeanaType.getTitle())));
    }

    if (europeanaType.ifType()) {
      mongoProxy.setDcType(FieldInputUtils.mergeMaps(mongoProxy.getDcType(),
          FieldInputUtils.createResourceOrLiteralMapFromString(europeanaType.getType())));
    }
  }
}
