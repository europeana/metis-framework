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

  /**
   * Set the dereferencer service. The default value is null.
   *
   * @param dereferencer The derenferencer.
   * @return This instance, for convenience.
   */
  public EnrichmentWorkerBuilder setDereferencer(Dereferencer dereferencer) {
    this.dereferencer = dereferencer;
    return this;
  }

  /**
   * Set the enricher service. The default value is null.
   *
   * @param enricher The enricher.
   * @return This instance, for convenience.
   */
  public EnrichmentWorkerBuilder setEnricher(Enricher enricher) {
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
