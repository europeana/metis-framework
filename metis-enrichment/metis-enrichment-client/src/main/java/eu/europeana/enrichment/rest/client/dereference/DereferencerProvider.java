package eu.europeana.enrichment.rest.client.dereference;

import eu.europeana.enrichment.api.external.impl.ClientEntityResolverFactory;
import eu.europeana.enrichment.rest.client.ConnectionProvider;
import eu.europeana.enrichment.rest.client.exceptions.DereferenceException;
import eu.europeana.enrichment.utils.EntityMergeEngine;
import eu.europeana.entity.client.config.EntityClientConfiguration;
import java.util.Properties;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Instances of this object can set up {@link Dereferencer} instances.
 * It has connection settings that will apply both to the
 * dereference and enrichment endpoints that it needs.
 */
public class DereferencerProvider extends ConnectionProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(DereferencerProvider.class);

  private String dereferenceUrl;
  private String entityManagementUrl;
  private String entityApiUrl;
  private String entityApiTokenEndpoint;
  private String entityApiGrantParams;

  /**
   * Set the URL of the dereferencing service. The default is null. If set to a blank value,
   * the dereferencer will not be configured to perform dereferencing.
   *
   * @param dereferenceUrl The URL of the dereferencing service.
   */
  public void setDereferenceUrl(String dereferenceUrl) {
    this.dereferenceUrl = dereferenceUrl;
  }

  /**
   * Set the properties values of the enrichment API. The default is null. If set to a blank value,
   * the dereferencer will not be configured to perform dereferencing.
   *
   * @param entityManagementUrl The url of the entity management service
   * @param entityApiUrl The url of the entity API service
   * @param entityApiTokenEndpoint the entity api token endpoint
   * @param entityApiGrantParams the entity api grant params
   */
  public void setEnrichmentPropertiesValues(String entityManagementUrl,
      String entityApiUrl,
      String entityApiTokenEndpoint,
      String entityApiGrantParams) {
    this.entityManagementUrl = entityManagementUrl;
    this.entityApiUrl = entityApiUrl;
    this.entityApiTokenEndpoint = entityApiTokenEndpoint;
    this.entityApiGrantParams = entityApiGrantParams;
  }

  /**
   * Builds an {@link Dereferencer} according to the parameters that are set.
   *
   * @return An instance.
   * @throws IllegalStateException When both the enrichment and dereference URLs are blank.
   * @throws DereferenceException if the enrichment url is wrong and therefore the dereferencer
   * was not successfully created.
   */
  public Dereferencer create() throws DereferenceException {

    // Make sure that the worker can do something.
    if (StringUtils.isBlank(dereferenceUrl) && !hasEntityApiClientProperties()) {
      throw new IllegalStateException("Dereferencing must be enabled.");
    }

    // Do some logging.
    if (dereferenceUrl == null) {
      LOGGER.warn("Creating dereferencer for Europeana entities only.");
    } else if (!hasEntityApiClientProperties()) {
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
    final EntityClientConfiguration entityClientConfiguration;
    if (hasEntityApiClientProperties()) {
      final Properties properties = buildEntityApiClientProperties(entityManagementUrl,
          entityApiUrl, entityApiTokenEndpoint, entityApiGrantParams);
      entityClientConfiguration = new EntityClientConfiguration(properties);
    } else {
      entityClientConfiguration = null;
    }

    // Done.
    return new DereferencerImpl(
        new EntityMergeEngine(),
        new ClientEntityResolverFactory(entityClientConfiguration),
        dereferenceClient);
  }

  private boolean hasEntityApiClientProperties() {
    return StringUtils.isNotBlank(entityManagementUrl)
        && StringUtils.isNotBlank(entityApiUrl)
        && StringUtils.isNotBlank(entityApiTokenEndpoint)
        && StringUtils.isNotBlank(entityApiGrantParams);
  }
}
