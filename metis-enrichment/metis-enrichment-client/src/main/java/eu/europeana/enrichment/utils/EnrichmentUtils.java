package eu.europeana.enrichment.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import eu.europeana.corelib.definitions.jibx.EuropeanaProxy;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.ProxyFor;
import eu.europeana.corelib.definitions.jibx.ProxyIn;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
import eu.europeana.corelib.definitions.jibx.Type2;
import eu.europeana.corelib.definitions.jibx.Year;

/**
 * Utilities for enrichment and dereferencing Created by gmamakis on 8-3-17.
 */
public final class EnrichmentUtils {

  private EnrichmentUtils() {}

  /**
   * Extract the fields to enrich from an RDF file
   * 
   * @param rdf The RDF to extract from.
   * @return List<InputValue> The extracted fields that need to be enriched.
   */
  public static List<InputValue> extractFieldsForEnrichmentFromRDF(RDF rdf) {
    ProxyType providerProxy = RdfProxyUtils.getProviderProxy(rdf);
    List<InputValue> valuesForEnrichment = new ArrayList<>();
    for (EnrichmentFields field : EnrichmentFields.values()) {
      List<InputValue> values = field.extractFieldValuesForEnrichment(providerProxy);
      valuesForEnrichment.addAll(values);
    }
    return valuesForEnrichment;
  }

  /**
   * <p>
   * This method is executed at the end of the enrichment process and sets additional values/fields
   * in the RDF, based on all the data that has been collected.
   * </p>
   * <p>
   * Currently it creates a europeana proxy (if one is not already present) and then sets the
   * edm:year fields (obtained from the provider proxy). If no provider proxy is present, this
   * method has no effect.
   * </p>
   * 
   * @param rdf The RDF in which to set additional field values.
   */
  public static void setAdditionalData(RDF rdf) {

    // Get the provider proxy
    final ProxyType providerProxy = rdf.getProxyList().stream()
        .filter(proxy -> !isEuropeanaProxy(proxy)).findAny().orElse(null);
    if (providerProxy == null) {
      return;
    }

    // Ensure that there is a europeana proxy (create one if needed).
    // TODO: 15-6-18 The europeanaProxy is to be created during transformation including the proper identifiers therefore the following code should be removed and only the "filling" of it should exist here
    final ProxyType europeanaProxy = ensureEuropeanaProxy(rdf, providerProxy);

    // Obtain the date strings from the various proxy fields.
    final List<String> dateStrings =
        providerProxy.getChoiceList().stream().map(EnrichmentUtils::getDateFromChoice)
            .filter(Objects::nonNull).collect(Collectors.toList());

    // Parse them and set them in the europeana proxy.
    final List<Year> yearList = new YearParser().parse(dateStrings).stream()
        .map(EnrichmentUtils::createYear).collect(Collectors.toList());
    europeanaProxy.setYearList(yearList);
  }

  private static Year createYear(Integer year) {
    final Year result = new Year();
    result.setString(year.toString());
    return result;
  }

  private static String getDateFromChoice(Choice choice) {
    final ResourceOrLiteralType result;
    if (choice.ifDate()) {
      result = choice.getDate();
    } else if (choice.ifTemporal()) {
      result = choice.getTemporal();
    } else if (choice.ifCreated()) {
      result = choice.getCreated();
    } else if (choice.ifIssued()) {
      result = choice.getIssued();
    } else {
      result = null;
    }
    return result == null ? null : result.getString();
  }

  private static ProxyType ensureEuropeanaProxy(RDF rdf, ProxyType providerProxy) {

    // Try to find the Europeana proxy if it already exists.
    final ProxyType existingEuropeanaProxy = rdf.getProxyList().stream()
        .filter(EnrichmentUtils::isEuropeanaProxy).findAny().orElse(null);
    if (existingEuropeanaProxy != null) {
      return existingEuropeanaProxy;
    }

    // If it doesn't, create it.
    final ProxyType newEuropeanaProxy = new ProxyType();
    final EuropeanaProxy isEuropeanaProxy = new EuropeanaProxy();
    isEuropeanaProxy.setEuropeanaProxy(true);
    newEuropeanaProxy.setEuropeanaProxy(isEuropeanaProxy);

    // Set the type.
    if (providerProxy.getType() != null) {
      final Type2 type = new Type2();
      type.setType(providerProxy.getType().getType());
      newEuropeanaProxy.setType(type);
    }

    // Get the about
    final String providedCHOAbout = rdf.getProvidedCHOList().get(0).getAbout();

    // Set the about
    newEuropeanaProxy.setAbout("/proxy/europeana" + providedCHOAbout);
    final ProxyFor proxyFor = new ProxyFor();
    proxyFor.setResource(providedCHOAbout);
    newEuropeanaProxy.setProxyFor(proxyFor);
    final ProxyIn proxyIn = new ProxyIn();
    proxyIn.setResource("/aggregation/europeana" + providedCHOAbout);
    newEuropeanaProxy.setProxyInList(Stream.of(proxyIn).collect(Collectors.toList()));

    // Add new proxy to RDF.
    final List<ProxyType> proxies = new ArrayList<>(rdf.getProxyList());
    proxies.add(newEuropeanaProxy);
    rdf.setProxyList(proxies);

    // Done
    return newEuropeanaProxy;
  }

  private static boolean isEuropeanaProxy(ProxyType proxy) {
    return proxy.getEuropeanaProxy() != null && proxy.getEuropeanaProxy().isEuropeanaProxy();
  }
}
