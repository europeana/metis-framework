package eu.europeana.enrichment.api.external.impl;

import eu.europeana.api.commons_sb3.auth.AuthenticationBuilder;
import eu.europeana.entity.client.EntityApiClient;
import eu.europeana.entity.client.config.ClientConnectionConfig;
import eu.europeana.entity.client.config.EntityClientConfiguration;
import eu.europeana.entity.client.exception.EntityClientException;

/**
 * The type Client entity resolver factory.
 */
public class ClientEntityResolverFactory {

  private final EntityClientConfiguration entityApiClientConfiguration;

  /**
   * Instantiates a new Client entity resolver factory.
   *
   * @param entityApiClientConfiguration the entity api client configuration
   */
  public ClientEntityResolverFactory(EntityClientConfiguration entityApiClientConfiguration) {
    this.entityApiClientConfiguration = new EntityClientConfiguration(entityApiClientConfiguration);
  }

  /**
   * Create client entity resolver.
   *
   * @return the client entity resolver
   * @throws EntityClientException the entity client exception
   */
  public ClientEntityResolver create()
      throws EntityClientException {
    ClientConnectionConfig clientConnectionConfig =
        new ClientConnectionConfig("200", "20", "1", "30", "30", "60", "30");
    EntityApiClient entityApiClient = new EntityApiClient(entityApiClientConfiguration.getEntityApiUrl(),
        entityApiClientConfiguration.getEntityManagementUrl(),
        AuthenticationBuilder.newAuthentication(entityApiClientConfiguration),
        clientConnectionConfig);
    return new ClientEntityResolver(entityApiClient);
  }
}
