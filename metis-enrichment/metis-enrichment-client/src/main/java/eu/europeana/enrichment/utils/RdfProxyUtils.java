package eu.europeana.enrichment.utils;

import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.EuropeanaType;
import eu.europeana.metis.schema.jibx.ProxyType;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
  public static void appendLinkToEuropeanaProxy(RDF rdf, String link,
          Set<EnrichmentFields> linkTypes) {
    final Map<EnrichmentFields, Set<String>> allProxyLinksPerType = getAllProxyLinksPerType(rdf);
    final ProxyType europeanaProxy = getEuropeanaProxy(rdf);
    for (EnrichmentFields linkType : linkTypes) {
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

  private static Map<EnrichmentFields, Set<String>> getAllProxyLinksPerType(RDF rdf) {
    final List<EuropeanaType.Choice> allChoices = Optional.ofNullable(rdf.getProxyList())
            .map(List::stream).orElseGet(Stream::empty).filter(Objects::nonNull)
            .map(ProxyType::getChoiceList).filter(Objects::nonNull).flatMap(List::stream)
            .filter(Objects::nonNull).collect(Collectors.toList());
    final Map<EnrichmentFields, Set<String>> result = new EnumMap<>(EnrichmentFields.class);
    for (EnrichmentFields linkType : EnrichmentFields.values()) {
      final Set<String> links = allChoices.stream().map(linkType::getResourceIfRightChoice)
              .filter(Objects::nonNull).collect(Collectors.toSet());
      if (!links.isEmpty()) {
        result.put(linkType, links);
      }
    }
    return result;
  }
  
  /**
   * Retrieve the Provider proxy from the proxy list in the {@link RDF}
   *
   * @param rdf the rdf used to search for the proxy
   * @return the Provider proxy
   */
  public static ProxyType getProviderProxy(RDF rdf) {
    for (ProxyType proxyType : rdf.getProxyList()) {
      if (proxyType.getEuropeanaProxy() == null
          || !proxyType.getEuropeanaProxy().isEuropeanaProxy()) {
        return proxyType;
      }
    }
    throw new IllegalArgumentException("Could not find provider proxy.");
  }

  private static ProxyType getEuropeanaProxy(RDF rdf) {
    for (ProxyType proxyType : rdf.getProxyList()) {
      if (proxyType.getEuropeanaProxy() != null
          && proxyType.getEuropeanaProxy().isEuropeanaProxy()) {
        return proxyType;
      }
    }
    throw new IllegalArgumentException("Could not find Europeana proxy.");
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
