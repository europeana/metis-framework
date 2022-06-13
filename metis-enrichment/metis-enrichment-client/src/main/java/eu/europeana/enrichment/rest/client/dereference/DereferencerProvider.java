package eu.europeana.enrichment.rest.client.dereference;

import eu.europeana.enrichment.api.external.impl.RemoteEntityResolver;
import eu.europeana.enrichment.rest.client.ConnectionProvider;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import java.net.MalformedURLException;
import java.net.URL;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instances of this object can set up {@link Dereferencer} instances. It has connection settings
 * that will apply both to the dereference and enrichment endpoints that it needs.
 */
public class DereferencerProvider extends ConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(DereferencerProvider.class);

  private String dereferenceUrl;
  private String enrichmentUrl;

  /**
   * Set the URL of the dereferencing service. The default is null. If set to a blank value, the
   * dereferencer will not be configured to perform dereferencing.
   *
   * @param dereferenceUrl The URL of the dereferencing service.
   */
  public void setDereferenceUrl(String dereferenceUrl) {
    this.dereferenceUrl = dereferenceUrl;

  }

  /**
   * Set the URL of the enrichment service. The default is null. If set to a blank value, the
   * dereferencer will not be configured to perform dereferencing.
   *
   * @param enrichmentUrl The URL of the enrichment service.
   */
  public void setEnrichmentUrl(String enrichmentUrl) {
    this.enrichmentUrl = enrichmentUrl;

  }

  /**
   * Builds an {@link Dereferencer} according to the parameters that are set.
   *
   * @return An instance.
   * @throws IllegalStateException When both the enrichment and dereference URLs are blank.
   * @throws DereferenceException if the enrichment url is wrong and therefore the dereferencer
   * was not successfully created
   */
  public Dereferencer create() throws DereferenceException {

    // Make sure that the worker can do something.
    if (StringUtils.isBlank(dereferenceUrl) && StringUtils.isBlank(enrichmentUrl)) {
      throw new IllegalStateException(
          "Dereferencing must be enabled.");
    }

    // Do some logging.
    if (dereferenceUrl == null) {
      LOGGER.warn("Creating dereferencer for Europeana entities only.");
    } else if (enrichmentUrl == null) {
      LOGGER.warn("Creating dereferencer for non-Europeana entities only.");
    } else {
      LOGGER.info("Creating dereferencer for both Europeana and non-Europeana entities.");
    }

    // Create the dereference client if needed
    final DereferenceClient dereferenceClient;
    if (StringUtils.isNotBlank(dereferenceUrl)) {
      dereferenceClient = new DereferenceClient(createRestTemplate(), dereferenceUrl);
    } else {
      dereferenceClient = null;
    }

    // Create the enrichment client if needed
    final RemoteEntityResolver remoteEntityResolver;
    if (StringUtils.isNotBlank(enrichmentUrl)) {
      try {
        remoteEntityResolver = new RemoteEntityResolver(new URL(enrichmentUrl),
                batchSizeEnrichment, createRestTemplate());
      } catch (MalformedURLException e) {
        LOGGER.debug("There was a problem with the input values");
        throw new DereferenceException("Problems while building a new Dereferencer", e);
      }
    } else {
      remoteEntityResolver = null;
    }

    // Done.
    return new DereferencerImpl(new EntityMergeEngine(), remoteEntityResolver, dereferenceClient);
  }
}
