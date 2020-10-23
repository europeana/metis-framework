package eu.europeana.enrichment.rest.client.enrichment;

import eu.europeana.enrichment.rest.client.AbstractConnectionProvider;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import org.apache.commons.lang3.StringUtils;

public class EnricherProvider extends AbstractConnectionProvider {

  private String enrichmentUrl = null;

  /**
   * Set the URL of the enrichment service. The default is null. If set to a blank value, the
   * enrichment worker will not be configured to perform enrichment.
   *
   * @param enrichmentUrl The URL of the dereferencing service.
   */
  public void setEnrichmentUrl(String enrichmentUrl) {
    this.enrichmentUrl = enrichmentUrl;
  }

  /**
   * Builds an {@link Enricher} according to the parameters that are set.
   *
   * @return An instance.
   * @throws IllegalStateException When both the enrichment and dereference URLs are blank.
   */
  public Enricher create() {

    // Make sure that the worker can do something.
    if (StringUtils.isBlank(enrichmentUrl)) {
      throw new IllegalStateException(
          "Enrichment must be enabled.");
    }

    // Create the enrichment client if needed
    final EnrichmentClient enrichmentClient;
    if (StringUtils.isNotBlank(enrichmentUrl)) {
      enrichmentClient = new EnrichmentClient(createRestTemplate(), enrichmentUrl,
              batchSizeEnrichment);
    } else {
      enrichmentClient = null;
    }

    // Done.
    return new EnricherImpl(new EntityMergeEngine(), enrichmentClient);
  }

}
