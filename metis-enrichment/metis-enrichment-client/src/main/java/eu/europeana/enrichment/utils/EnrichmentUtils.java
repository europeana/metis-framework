package eu.europeana.enrichment.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import eu.europeana.corelib.definitions.jibx.EuropeanaType.Choice;
import eu.europeana.corelib.definitions.jibx.ProxyType;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.ResourceOrLiteralType;
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
   * Currently sets the edm:year fields (obtained from the provider proxy) in the europeana proxy.
   * If no europeana or provider proxy is present, this method has no effect.
   * </p>
   * 
   * @param rdf The RDF in which to set additional field values.
   */
  public static void setAdditionalData(RDF rdf) {

    // Get the provider and europeana proxies
    final ProxyType providerProxy = rdf.getProxyList().stream()
        .filter(proxy -> !isEuropeanaProxy(proxy)).findAny().orElse(null);
    final ProxyType europeanaProxy = rdf.getProxyList().stream()
        .filter(EnrichmentUtils::isEuropeanaProxy).findAny().orElse(null);
    if (providerProxy == null || europeanaProxy == null) {
      return;
    }

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

  private static boolean isEuropeanaProxy(ProxyType proxy) {
    return proxy.getEuropeanaProxy() != null && proxy.getEuropeanaProxy().isEuropeanaProxy();
  }
}
