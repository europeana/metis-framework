package eu.europeana.indexing.tiers.metadata;

import eu.europeana.metis.schema.jibx.EuropeanaType.Choice;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource;
import eu.europeana.metis.schema.jibx.ResourceType;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/**
 * This enum lists all fields with resource links in {@link ProxyType}, and provides a way to
 * extract them, as well as a literal value that they might have.
 */
public enum ResourceLinkFromProxy {

  PROXY_FOR(createResourceLinkGetter(ProxyType::getProxyFor)),
  PROXY_IN(createResourceLinksGetter(ProxyType::getProxyInList)),
  SAME_AS(createResourceLinksGetter(ProxyType::getSameAList)),

  CURRENT_LOCATION(createResourceOrLiteralLinkGetter(ProxyType::getCurrentLocation)),
  HAS_MET(createResourceLinksGetter(ProxyType::getHasMetList)),
  HAS_TYPE(createResourceOrLiteralLinksGetter(ProxyType::getHasTypeList)),
  INCORPORATES(createResourceLinksGetter(ProxyType::getIncorporateList)),
  IS_DERIVATIVE_OF(createResourceLinksGetter(ProxyType::getIsDerivativeOfList)),
  IS_NEXT_IN_SEQUENCE(createResourceLinksGetter(ProxyType::getIsNextInSequenceList)),
  IS_RELATED_TO(createResourceOrLiteralLinksGetter(ProxyType::getIsRelatedToList)),
  IS_REPRESENTATION_OF(createResourceLinkGetter(ProxyType::getIsRepresentationOf)),
  IS_SIMILAR_TO(createResourceLinksGetter(ProxyType::getIsSimilarToList)),
  IS_SUCCESSOR_OF(createResourceLinksGetter(ProxyType::getIsSuccessorOfList)),
  REALIZES(createResourceLinksGetter(ProxyType::getRealizeList)),

  CONTRIBUTOR(createResourceOrLiteralLinksGetter(Choice::ifContributor, Choice::getContributor)),
  COVERAGE(createResourceOrLiteralLinksGetter(Choice::ifCoverage, Choice::getCoverage)),
  CREATOR(createResourceOrLiteralLinksGetter(Choice::ifCreator, Choice::getCreator)),
  DATE(createResourceOrLiteralLinksGetter(Choice::ifDate, Choice::getDate)),
  DESCRIPTION(createResourceOrLiteralLinksGetter(Choice::ifDescription, Choice::getDescription)),
  FORMAT(createResourceOrLiteralLinksGetter(Choice::ifFormat, Choice::getFormat)),
  PUBLISHER(createResourceOrLiteralLinksGetter(Choice::ifPublisher, Choice::getPublisher)),
  RELATION(createResourceOrLiteralLinksGetter(Choice::ifRelation, Choice::getRelation)),
  RIGHTS(createResourceOrLiteralLinksGetter(Choice::ifRights, Choice::getRights)),
  SOURCE(createResourceOrLiteralLinksGetter(Choice::ifSource, Choice::getSource)),
  SUBJECT(createResourceOrLiteralLinksGetter(Choice::ifSubject, Choice::getSubject)),
  TYPE(createResourceOrLiteralLinksGetter(Choice::ifType, Choice::getType)),
  CONFORMS_TO(createResourceOrLiteralLinksGetter(Choice::ifConformsTo, Choice::getConformsTo)),
  CREATED(createResourceOrLiteralLinksGetter(Choice::ifCreated, Choice::getCreated)),
  EXTENT(createResourceOrLiteralLinksGetter(Choice::ifExtent, Choice::getExtent)),
  HAS_FORMAT(createResourceOrLiteralLinksGetter(Choice::ifHasFormat, Choice::getHasFormat)),
  HAS_PART(createResourceOrLiteralLinksGetter(Choice::ifHasPart, Choice::getHasPart)),
  HAS_VERSION(createResourceOrLiteralLinksGetter(Choice::ifHasVersion, Choice::getHasVersion)),
  IS_FORMAT_OF(createResourceOrLiteralLinksGetter(Choice::ifIsFormatOf, Choice::getIsFormatOf)),
  IS_PART_OF(createResourceOrLiteralLinksGetter(Choice::ifIsPartOf, Choice::getIsPartOf)),
  IS_REFERENCED_BY(
      createResourceOrLiteralLinksGetter(Choice::ifIsReferencedBy, Choice::getIsReferencedBy)),
  IS_REPLACED_BY(
      createResourceOrLiteralLinksGetter(Choice::ifIsReplacedBy, Choice::getIsReplacedBy)),
  IS_REQUIRED_BY(
      createResourceOrLiteralLinksGetter(Choice::ifIsRequiredBy, Choice::getIsRequiredBy)),
  ISSUED(createResourceOrLiteralLinksGetter(Choice::ifIssued, Choice::getIssued)),
  IS_VERSION_OF(createResourceOrLiteralLinksGetter(Choice::ifIsVersionOf, Choice::getIsVersionOf)),
  MEDIUM(createResourceOrLiteralLinksGetter(Choice::ifMedium, Choice::getMedium)),
  PROVENANCE(createResourceOrLiteralLinksGetter(Choice::ifProvenance, Choice::getProvenance)),
  REFERENCES(createResourceOrLiteralLinksGetter(Choice::ifReferences, Choice::getReferences)),
  REPLACES(createResourceOrLiteralLinksGetter(Choice::ifReplaces, Choice::getReplaces)),
  REQUIRES(createResourceOrLiteralLinksGetter(Choice::ifRequires, Choice::getRequires)),
  SPATIAL(createResourceOrLiteralLinksGetter(Choice::ifSpatial, Choice::getSpatial)),
  TABLE_OF_CONTENTS(
      createResourceOrLiteralLinksGetter(Choice::ifTableOfContents, Choice::getTableOfContents)),
  TEMPORAL(createResourceOrLiteralLinksGetter(Choice::ifTemporal, Choice::getTemporal));

  private final LinkAndValueGetter linkAndValueGetter;

  ResourceLinkFromProxy(LinkAndValueGetter linkAndValueGetter) {
    this.linkAndValueGetter = linkAndValueGetter;
  }

  private static LinkAndValueGetter createResourceLinkGetter(
      Function<ProxyType, ResourceType> linkExtractor) {
    return createResourceLinksGetter(linkExtractor.andThen(Collections::singletonList));
  }

  private static LinkAndValueGetter createResourceLinksGetter(
      Function<ProxyType, List<? extends ResourceType>> linksExtractor) {
    final Function<ProxyType, Stream<String>> getLinks = proxy -> Optional.of(proxy)
        .map(linksExtractor).orElseGet(Collections::emptyList).stream().filter(Objects::nonNull)
        .map(ResourceType::getResource).filter(StringUtils::isNotBlank);
    return new LinkAndValueGetter(getLinks, proxy -> Stream.empty());
  }

  private static LinkAndValueGetter createResourceOrLiteralLinksGetter(
      Predicate<Choice> isRightChoice,
      Function<Choice, ResourceOrLiteralType> getResourceFromChoice) {
    return createResourceOrLiteralLinksGetter(
        getPredicatesFromChoice(isRightChoice, getResourceFromChoice));
  }

  private static LinkAndValueGetter createResourceOrLiteralLinkGetter(
      Function<ProxyType, ResourceOrLiteralType> linkExtractor) {
    return createResourceOrLiteralLinksGetter(linkExtractor.andThen(Collections::singletonList));
  }

  private static LinkAndValueGetter createResourceOrLiteralLinksGetter(
      Function<ProxyType, List<? extends ResourceOrLiteralType>> linksExtractor) {
    final Function<ProxyType, Stream<? extends ResourceOrLiteralType>> getObjects = proxy -> Optional
        .of(proxy).map(linksExtractor).orElseGet(Collections::emptyList).stream()
        .filter(Objects::nonNull);
    final Function<Stream<? extends ResourceOrLiteralType>, Stream<String>> getLinks = stream -> stream
        .map(ResourceOrLiteralType::getResource).filter(Objects::nonNull).map(Resource::getResource)
        .filter(StringUtils::isNotBlank);
    final Function<Stream<? extends ResourceOrLiteralType>, Stream<String>> getValues = stream -> stream
        .map(ResourceOrLiteralType::getString).filter(StringUtils::isNotBlank);
    return new LinkAndValueGetter(getObjects.andThen(getLinks), getObjects.andThen(getValues));
  }

  private static <T> Function<ProxyType, List<? extends T>> getPredicatesFromChoice(
      Predicate<Choice> isRightChoice, Function<Choice, T> getPredicateFromChoice) {
    return proxy -> Optional.of(proxy).map(ProxyType::getChoiceList).map(List::stream)
        .orElseGet(Stream::empty).filter(Objects::nonNull).filter(isRightChoice)
        .map(getPredicateFromChoice).filter(Objects::nonNull).collect(Collectors.toList());
  }

  LinkAndValueGetter getLinkAndValueGetter() {
    return linkAndValueGetter;
  }

  static class LinkAndValueGetter {

    private final Function<ProxyType, Stream<String>> getLinks;
    private final Function<ProxyType, Stream<String>> getValues;

    LinkAndValueGetter(Function<ProxyType, Stream<String>> getLinks,
        Function<ProxyType, Stream<String>> getValues) {
      this.getLinks = getLinks;
      this.getValues = getValues;
    }

    Stream<String> getLinks(ProxyType proxy) {
      return getLinks.apply(proxy);
    }

    Stream<String> getValues(ProxyType proxy) {
      return getValues.apply(proxy);
    }
  }
}
