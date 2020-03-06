package eu.europeana.enrichment.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.corelib.definitions.jibx.AboutType;
import eu.europeana.corelib.definitions.jibx.EuropeanaType;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;

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
   * @param about the about value to use for the field
   * @param linkTypes the types of the link to add in the europeana proxy.
   */
  public static void appendToEuropeanaProxy(RDF rdf, AboutType about,
          Set<EnrichmentFields> linkTypes) {
    ProxyType europeanaProxy = getEuropeanaProxy(rdf);
    for (EnrichmentFields linkType : linkTypes) {
      appendToEuropeanaProxy(europeanaProxy, linkType, about.getAbout());
    }
    replaceProxy(rdf, europeanaProxy);
  }

  private static void appendToEuropeanaProxy(ProxyType europeanaProxy,
      EnrichmentFields enrichmentField, String about) {
    //Choice might be null. That probably happens because of jibx deserialization works.
    List<EuropeanaType.Choice> choices =
        europeanaProxy.getChoiceList() == null ? new ArrayList<>() : europeanaProxy.getChoiceList();
    choices.add(enrichmentField.createChoice(about));
    europeanaProxy.setChoiceList(choices);
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
