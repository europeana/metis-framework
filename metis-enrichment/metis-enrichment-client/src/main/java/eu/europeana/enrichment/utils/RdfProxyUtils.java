package eu.europeana.enrichment.utils;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.corelib.definitions.jibx.AboutType;
import eu.europeana.corelib.definitions.jibx.EuropeanaType;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;

/**
 * Utility class for {@link ProxyType} operations in the {@link RDF}
 */
public final class RdfProxyUtils {

  private RdfProxyUtils() {}

  /**
   * Add a field with the specified {@link AboutType} to the EuropeanaProxy.
   * @param rdf the rdf to append to
   * @param about the about value to use for the field
   * @param fieldName the name of the field to add
   */
  public static void appendToProxy(RDF rdf, AboutType about, String fieldName) {
    ProxyType europeanaProxy = getEuropeanaProxy(rdf);
    appendToProxy(europeanaProxy, EnrichmentFields.valueOf(fieldName), about.getAbout());
    replaceProxy(rdf, europeanaProxy);
  }

  private static void appendToProxy(ProxyType europeanaProxy, EnrichmentFields enrichmentFields,
      String about) {
    List<EuropeanaType.Choice> choices = europeanaProxy.getChoiceList();
    choices.add(enrichmentFields.createChoice(about));
    europeanaProxy.setChoiceList(choices);
  }

  /**
   * Retrieve the Provider proxy from the proxy list in the {@link RDF}
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
