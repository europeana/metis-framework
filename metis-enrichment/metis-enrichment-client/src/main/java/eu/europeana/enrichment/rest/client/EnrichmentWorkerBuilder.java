package eu.europeana.enrichment.rest.client;

import eu.europeana.enrichment.rest.client.dereference.DereferenceClient;
import eu.europeana.enrichment.rest.client.dereference.Dereferencer;
import eu.europeana.enrichment.rest.client.enrichment.Enricher;
import eu.europeana.enrichment.rest.client.enrichment.EnrichmentClient;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * This class is able to construct {@link EnrichmentWorker} instances and is as such the main
 * gateway to dereference and enrichment functionality.
 */
public class EnrichmentWorkerBuilder {

  private Dereferencer dereferencer = null;
  private Enricher enricher = null;

  // TODO: Fix Javadoc
  /**
   * Set the URL of the dereferencing service. The default is null. If set to a blank value, the
   * enrichment worker will not be configured to perform dereferencing.
   *
   * @param dereferencer The URL of the dereferencing service.
   * @return This instance, for convenience.
   */
  public EnrichmentWorkerBuilder setDereferencer(Dereferencer dereferencer) {
    this.dereferencer = dereferencer;
    return this;
  }

  // TODO: Fix Javadoc
  /**
   * Set the URL of the enrichment service. The default is null. If set to a blank value, the
   * enrichment worker will not be configured to perform enrichment.
   *
   * @param enricher The URL of the dereferencing service.
   * @return This instance, for convenience.
   */
  public EnrichmentWorkerBuilder setEnricher(Enricher enricher) { // TODO: Remove this --> it is used for building the client
    this.enricher = enricher;
    return this;
  }

  /**
   * Builds an {@link EnrichmentWorker} according to the parameters that are set.
   *
   * @return An instance.
   * @throws IllegalStateException When both the enrichment and dereference URLs are blank.
   */
  public EnrichmentWorker build() {

    // Make sure that the worker can do something.
    if (dereferencer == null && enricher == null) {
      throw new IllegalStateException(
              "Either dereferencing or enrichment (or both) must be enabled.");
    }

    // Done.
    return new EnrichmentWorkerImpl(dereferencer, enricher);
  }
}
