package eu.europeana.enrichment.rest.client.enrichment;

import eu.europeana.enrichment.rest.client.AbstractConnectionBuilder;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

public class EnricherBuilder extends AbstractConnectionBuilder {

  private String enrichmentUrl = null;

  /**
   * Set the URL of the enrichment service. The default is null. If set to a blank value, the
   * enrichment worker will not be configured to perform enrichment.
   *
   * @param enrichmentUrl The URL of the dereferencing service.
   * @return This instance, for convenience.
   */
  public EnricherBuilder setEnrichmentUrl(String enrichmentUrl) {
    this.enrichmentUrl = enrichmentUrl;
    return this;
  }

  /**
   * Builds an {@link Enricher} according to the parameters that are set.
   *
   * @return An instance.
   * @throws IllegalStateException When both the enrichment and dereference URLs are blank.
   */
  public Enricher build() {

    // Make sure that the worker can do something.
    if (StringUtils.isBlank(enrichmentUrl)) {
      throw new IllegalStateException(
          "Either dereferencing or enrichment (or both) must be enabled.");
    }

    // Create the request factory
    final HttpComponentsClientHttpRequestFactory requestFactory = super.createRequestFactory();

    // Create the enrichment client if needed
    final EnrichmentClient enrichmentClient;
    if (StringUtils.isNotBlank(enrichmentUrl)) {
      enrichmentClient = new EnrichmentClient(new RestTemplate(requestFactory), enrichmentUrl,
          batchSizeEnrichment);
    } else {
      enrichmentClient = null;
    }

    // Done.
    return new EnricherImpl(new EntityMergeEngine(), enrichmentClient);
  }

}
