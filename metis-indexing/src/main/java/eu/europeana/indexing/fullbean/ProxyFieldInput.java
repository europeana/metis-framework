package eu.europeana.indexing.fullbean;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import eu.europeana.corelib.definitions.jibx.EdmType;
import eu.europeana.corelib.definitions.jibx.EuropeanaProxy;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.IsNextInSequence;
import eu.europeana.corelib.definitions.jibx.LiteralType;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
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
    mongoProxy.setEuropeanaProxy(Optional.ofNullable(proxy.getEuropeanaProxy())
        .map(EuropeanaProxy::isEuropeanaProxy).orElse(false));
    mongoProxy.setEdmCurrentLocation(
        FieldInputUtils.createResourceOrLiteralMapFromString(proxy.getCurrentLocation()));

    List<IsNextInSequence> seqList = proxy.getIsNextInSequenceList();

    if (seqList != null) {
      mongoProxy.setEdmIsNextInSequence(
          seqList.stream().map(IsNextInSequence::getResource).toArray(String[]::new));
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

  private static <T extends LiteralType> void applyToLiteralChoiceOption(BooleanSupplier condition,
      Supplier<T> valueGetter, Supplier<Map<String, List<String>>> mapGetter,
      Consumer<Map<String, List<String>>> mapSetter) {
    if (condition.getAsBoolean()) {
      mapSetter.accept(FieldInputUtils.mergeMaps(mapGetter.get(),
          FieldInputUtils.createLiteralMapFromString(valueGetter.get())));
    }
  }

  private static <T extends ResourceOrLiteralType> void applyToResourceOrLiteralChoiceOption(
      BooleanSupplier condition, Supplier<T> valueGetter,
      Supplier<Map<String, List<String>>> mapGetter,
      Consumer<Map<String, List<String>>> mapSetter) {
    if (condition.getAsBoolean()) {
      mapSetter.accept(FieldInputUtils.mergeMaps(mapGetter.get(),
          FieldInputUtils.createResourceOrLiteralMapFromString(valueGetter.get())));
    }
  }

  private static void applyToChoice(Choice choice, ProxyImpl proxy) {
    applyToLiteralChoiceOption(choice::ifAlternative, choice::getAlternative,
        proxy::getDctermsAlternative, proxy::setDctermsAlternative);
    applyToResourceOrLiteralChoiceOption(choice::ifConformsTo, choice::getConformsTo,
        proxy::getDctermsConformsTo, proxy::setDctermsConformsTo);
    applyToResourceOrLiteralChoiceOption(choice::ifCreated, choice::getCreated,
        proxy::getDctermsCreated, proxy::setDctermsCreated);
    applyToResourceOrLiteralChoiceOption(choice::ifExtent, choice::getExtent,
        proxy::getDctermsExtent, proxy::setDctermsExtent);
    applyToResourceOrLiteralChoiceOption(choice::ifHasFormat, choice::getHasFormat,
        proxy::getDctermsHasFormat, proxy::setDctermsHasFormat);
    applyToResourceOrLiteralChoiceOption(choice::ifHasPart, choice::getHasPart,
        proxy::getDctermsHasPart, proxy::setDctermsHasPart);
    applyToResourceOrLiteralChoiceOption(choice::ifHasVersion, choice::getHasVersion,
        proxy::getDctermsHasVersion, proxy::setDctermsHasVersion);
    applyToResourceOrLiteralChoiceOption(choice::ifIsFormatOf, choice::getIsFormatOf,
        proxy::getDctermsIsFormatOf, proxy::setDctermsIsFormatOf);
    applyToResourceOrLiteralChoiceOption(choice::ifIsPartOf, choice::getIsPartOf,
        proxy::getDctermsIsPartOf, proxy::setDctermsIsPartOf);
    applyToResourceOrLiteralChoiceOption(choice::ifIsReferencedBy, choice::getIsReferencedBy,
        proxy::getDctermsIsReferencedBy, proxy::setDctermsIsReferencedBy);
    applyToResourceOrLiteralChoiceOption(choice::ifIsReplacedBy, choice::getIsReplacedBy,
        proxy::getDctermsIsReplacedBy, proxy::setDctermsIsReplacedBy);
    applyToResourceOrLiteralChoiceOption(choice::ifIsRequiredBy, choice::getIsRequiredBy,
        proxy::getDctermsIsRequiredBy, proxy::setDctermsIsRequiredBy);
    applyToResourceOrLiteralChoiceOption(choice::ifIssued, choice::getIssued,
        proxy::getDctermsIssued, proxy::setDctermsIssued);
    applyToResourceOrLiteralChoiceOption(choice::ifIsVersionOf, choice::getIsVersionOf,
        proxy::getDctermsIsVersionOf, proxy::setDctermsIsVersionOf);
    applyToResourceOrLiteralChoiceOption(choice::ifMedium, choice::getMedium,
        proxy::getDctermsMedium, proxy::setDctermsMedium);
    applyToResourceOrLiteralChoiceOption(choice::ifProvenance, choice::getProvenance,
        proxy::getDctermsProvenance, proxy::setDctermsProvenance);
    applyToResourceOrLiteralChoiceOption(choice::ifReferences, choice::getReferences,
        proxy::getDctermsReferences, proxy::setDctermsReferences);
    applyToResourceOrLiteralChoiceOption(choice::ifReplaces, choice::getReplaces,
        proxy::getDctermsReplaces, proxy::setDctermsReplaces);
    applyToResourceOrLiteralChoiceOption(choice::ifRequires, choice::getRequires,
        proxy::getDctermsRequires, proxy::setDctermsRequires);
    applyToResourceOrLiteralChoiceOption(choice::ifSpatial, choice::getSpatial,
        proxy::getDctermsSpatial, proxy::setDctermsSpatial);
    applyToResourceOrLiteralChoiceOption(choice::ifTableOfContents, choice::getTableOfContents,
        proxy::getDctermsTOC, proxy::setDctermsTOC);
    applyToResourceOrLiteralChoiceOption(choice::ifTemporal, choice::getTemporal,
        proxy::getDctermsTemporal, proxy::setDctermsTemporal);

    applyToResourceOrLiteralChoiceOption(choice::ifContributor, choice::getContributor,
        proxy::getDcContributor, proxy::setDcContributor);
    applyToResourceOrLiteralChoiceOption(choice::ifCoverage, choice::getCoverage,
        proxy::getDcCoverage, proxy::setDcCoverage);
    applyToResourceOrLiteralChoiceOption(choice::ifCreator, choice::getCreator, proxy::getDcCreator,
        proxy::setDcCreator);
    applyToResourceOrLiteralChoiceOption(choice::ifDate, choice::getDate, proxy::getDcDate,
        proxy::setDcDate);
    applyToResourceOrLiteralChoiceOption(choice::ifDescription, choice::getDescription,
        proxy::getDcDescription, proxy::setDcDescription);
    applyToResourceOrLiteralChoiceOption(choice::ifFormat, choice::getFormat, proxy::getDcFormat,
        proxy::setDcFormat);
    applyToLiteralChoiceOption(choice::ifIdentifier, choice::getIdentifier, proxy::getDcIdentifier,
        proxy::setDcIdentifier);
    applyToLiteralChoiceOption(choice::ifLanguage, choice::getLanguage, proxy::getDcLanguage,
        proxy::setDcLanguage);
    applyToResourceOrLiteralChoiceOption(choice::ifPublisher, choice::getPublisher,
        proxy::getDcPublisher, proxy::setDcPublisher);
    applyToResourceOrLiteralChoiceOption(choice::ifRelation, choice::getRelation,
        proxy::getDcRelation, proxy::setDcRelation);
    applyToResourceOrLiteralChoiceOption(choice::ifRights, choice::getRights, proxy::getDcRights,
        proxy::setDcRights);
    applyToResourceOrLiteralChoiceOption(choice::ifSource, choice::getSource, proxy::getDcSource,
        proxy::setDcSource);
    applyToResourceOrLiteralChoiceOption(choice::ifSubject, choice::getSubject, proxy::getDcSubject,
        proxy::setDcSubject);
    applyToLiteralChoiceOption(choice::ifTitle, choice::getTitle, proxy::getDcTitle,
        proxy::setDcTitle);
    applyToResourceOrLiteralChoiceOption(choice::ifType, choice::getType, proxy::getDcType,
        proxy::setDcType);
  }
}
