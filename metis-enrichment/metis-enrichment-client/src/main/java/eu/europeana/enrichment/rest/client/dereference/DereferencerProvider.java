package eu.europeana.enrichment.rest.client.dereference;

import eu.europeana.enrichment.rest.client.AbstractConnectionProvider;
import eu.europeana.enrichment.rest.client.enrichment.EnrichmentClient;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class DereferencerProvider extends AbstractConnectionProvider {


  private String dereferenceUrl = null;
  private String enrichmentUrl = null;

  /**
   * Set the URL of the dereferencing service. The default is null. If set to a blank value, the
   * dereferencer will not be configured to perform dereferencing.
   *
   * @param dereferenceUrl The URL of the dereferencing service.
   * @return This instance, for convenience.
   */
  public DereferencerProvider setDereferenceUrl(String dereferenceUrl) {
    this.dereferenceUrl = dereferenceUrl;
    return this;
  }

  /**
   * Set the URL of the enrichment service. The default is null. If set to a blank value, the
   * dereferencer will not be configured to perform dereferencing.
   *
   * @param enrichmentUrl The URL of the enrichment service.
   * @return This instance, for convenience.
   */
  public DereferencerProvider setEnrichmentUrl(String enrichmentUrl) {
    this.enrichmentUrl = enrichmentUrl;
    return this;
  }

  /**
   * Builds an {@link Dereferencer} according to the parameters that are set.
   *
   * @return An instance.
   * @throws IllegalStateException When both the enrichment and dereference URLs are blank.
   */
  public Dereferencer build() {

    // Make sure that the worker can do something.
    if (StringUtils.isBlank(dereferenceUrl) && StringUtils.isBlank(enrichmentUrl)) {
      throw new IllegalStateException(
          "Dereferencing must be enabled.");
    }

    // Create the request factory
    final HttpComponentsClientHttpRequestFactory requestFactory = super.createRequestFactory();

    // Create the dereference client if needed
    final DereferenceClient dereferenceClient;
    if (StringUtils.isNotBlank(dereferenceUrl)) {
      dereferenceClient = new DereferenceClient(new RestTemplate(requestFactory), dereferenceUrl);
    } else {
      dereferenceClient = null;
    }

    // Create the enrichment client if needed
    final EnrichmentClient enrichmentClient;
    if (StringUtils.isNotBlank(enrichmentUrl)) {
      enrichmentClient = new EnrichmentClient(new RestTemplate(requestFactory), enrichmentUrl,
          batchSizeEnrichment);
    } else {
      enrichmentClient = null;
    }

    // Done.
    return new DereferencerImpl(new EntityMergeEngine(), enrichmentClient, dereferenceClient);
  }

}
