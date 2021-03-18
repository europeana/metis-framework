package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.internal.AggregationFieldType;
import eu.europeana.enrichment.api.internal.ProxyFieldType;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.api.internal.SearchTermContext;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.EuropeanaType;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.RDF;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Lang;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for {@link ProxyType} operations in the {@link RDF}
 */
public final class RdfProxyUtils {

  private RdfProxyUtils() {
  }

  /**
   * Add a link to the specified {@link AboutType} to the EuropeanaProxy.
   *
   * @param rdf the rdf to append to
   * @param link the about value to link
   * @param linkTypes the types of the link to add in the europeana proxy.
   */
  public static void appendLinkToEuropeanaProxy(RDF rdf, String link, Set<ProxyFieldType> linkTypes) {
    final Map<ProxyFieldType, Set<String>> allProxyLinksPerType = getAllProxyLinksPerType(rdf);
    final ProxyType europeanaProxy = getEuropeanaProxy(rdf);
    for (ProxyFieldType linkType : linkTypes) {
      final boolean alreadyExists = Optional.ofNullable(allProxyLinksPerType.get(linkType))
          .orElseGet(Collections::emptySet).contains(link);
      if (!alreadyExists) {
        final List<EuropeanaType.Choice> choices = Optional
            .ofNullable(europeanaProxy.getChoiceList()).orElseGet(ArrayList::new);
        choices.add(linkType.createChoice(link));
        europeanaProxy.setChoiceList(choices);
      }
    }
    replaceProxy(rdf, europeanaProxy);
  }

  /**
   * Replace matching aggregation values with their found corresponding links.
   *
   * @param rdf the rdf to update
   * @param link the about value to use
   * @param searchTermAggregation the aggregation search term to use for finding the matched values
   */
  public static void replaceValueWithLinkInAggregation(RDF rdf, String link,
      SearchTermContext searchTermAggregation) {
    final List<Aggregation> aggregationList = rdf.getAggregationList();
    for (AggregationFieldType aggregationFieldType : searchTermAggregation.getFieldTypes().stream()
        .map(AggregationFieldType.class::cast).collect(Collectors.toList())) {
      aggregationList.stream().flatMap(aggregationFieldType::extractFields).filter(
          resourceOrLiteralType -> resourceOrLiteralAndSearchTermEquality(resourceOrLiteralType,
              searchTermAggregation)).forEach(resourceOrLiteralType -> {
        final Resource resource = new Resource();
        resource.setResource(link);
        resourceOrLiteralType.setResource(resource);
        resourceOrLiteralType.setLang(new Lang());
        resourceOrLiteralType.setString("");
      });
    }
  }

  private static boolean resourceOrLiteralAndSearchTermEquality(
      ResourceOrLiteralType resourceOrLiteralType, SearchTerm searchTerm) {

    boolean areEqual = false;
    //Check literal values
    if (resourceOrLiteralType.getString() != null && resourceOrLiteralType.getString()
        .equals(searchTerm.getTextValue())) {
      //Check if both languages are blank
      if ((resourceOrLiteralType.getLang() == null || StringUtils
          .isBlank(resourceOrLiteralType.getLang().getLang())) && StringUtils
          .isBlank(searchTerm.getLanguage())) {
        areEqual = true;
      } else if (resourceOrLiteralType.getLang() != null
          && resourceOrLiteralType.getLang().getLang() != null) {
        //If not blank check language equality
        areEqual = resourceOrLiteralType.getLang().getLang().equals(searchTerm.getLanguage());
      }
    }
    return areEqual;
  }

  private static Map<ProxyFieldType, Set<String>> getAllProxyLinksPerType(RDF rdf) {
    final List<EuropeanaType.Choice> allChoices = Optional.ofNullable(rdf.getProxyList()).stream()
        .flatMap(Collection::stream).filter(Objects::nonNull).map(ProxyType::getChoiceList)
        .filter(Objects::nonNull).flatMap(List::stream).filter(Objects::nonNull)
        .collect(Collectors.toList());
    final Map<ProxyFieldType, Set<String>> result = new EnumMap<>(ProxyFieldType.class);
    for (ProxyFieldType linkType : ProxyFieldType.values()) {
      final Set<String> links = allChoices.stream().map(linkType::getResourceIfRightChoice)
          .filter(Objects::nonNull).collect(Collectors.toSet());
      if (!links.isEmpty()) {
        result.put(linkType, links);
      }
    }
    return result;
  }

  /**
   * Retrieve all Provider proxy from the proxy list in the {@link RDF}
   *
   * @param rdf the rdf used to search for the proxy
   * @return the Provider proxy list. Could be empty, but is not null.
   */
  public static List<ProxyType> getProviderProxies(RDF rdf) {
    return Optional.ofNullable(rdf.getProxyList()).stream().flatMap(Collection::stream)
        .filter(Objects::nonNull).filter(proxy -> !isEuropeanaProxy(proxy))
        .collect(Collectors.toList());
  }

  private static boolean isEuropeanaProxy(ProxyType proxy) {
    return proxy.getEuropeanaProxy() != null && proxy.getEuropeanaProxy().isEuropeanaProxy();
  }

  /**
   * Retrieve the Europeana proxy from the proxy list in the {@link RDF}
   *
   * @param rdf the rdf used to search for the proxy
   * @return the Europeana proxy. Is not null.
   * @throws IllegalArgumentException in case the RDF does not have a Europeana proxy.
   */
  public static ProxyType getEuropeanaProxy(RDF rdf) {
    return Optional.ofNullable(rdf.getProxyList()).stream().flatMap(Collection::stream)
        .filter(Objects::nonNull).filter(RdfProxyUtils::isEuropeanaProxy).findAny()
        .orElseThrow(() -> new IllegalArgumentException("Could not find Europeana proxy."));
  }

  private static void replaceProxy(RDF rdf, ProxyType europeanaProxy) {
    List<ProxyType> proxyTypeList = new ArrayList<>();
    proxyTypeList.add(europeanaProxy);
    for (ProxyType proxyType : rdf.getProxyList()) {
      if (!StringUtils.equals(proxyType.getAbout(), europeanaProxy.getAbout())) {
        proxyTypeList.add(proxyType);
      }
    }
    rdf.setProxyList(proxyTypeList);
  }
}
