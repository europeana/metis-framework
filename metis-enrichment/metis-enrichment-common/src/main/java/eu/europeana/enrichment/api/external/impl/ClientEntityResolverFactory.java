package eu.europeana.enrichment.api.external.impl;

import eu.europeana.api.commons_sb3.auth.AuthenticationBuilder;
import eu.europeana.entity.client.EntityApiClient;
import eu.europeana.entity.client.config.EntityClientConfiguration;
import eu.europeana.entity.client.exception.EntityClientException;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.nio.PoolingAsyncClientConnectionManagerBuilder;
import org.apache.hc.core5.reactor.IOReactorConfig;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;

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
    return new ClientEntityResolver(
        new EntityApiClient(
            entityApiClientConfiguration.getEntityApiUrl(),
            entityApiClientConfiguration.getEntityManagementUrl(),
            AuthenticationBuilder.newAuthentication(entityApiClientConfiguration),
            PoolingAsyncClientConnectionManagerBuilder
                .create()
                .setMaxConnTotal(200)
                .setMaxConnPerRoute(50)
                .setDefaultConnectionConfig(
                    ConnectionConfig.custom()
                                    .setConnectTimeout(Timeout.ofSeconds(30))
                                    .setSocketTimeout(Timeout.ofSeconds(30))
                                    .setValidateAfterInactivity(TimeValue.ofSeconds(1))
                                    .setTimeToLive(TimeValue.ofSeconds(30))
                                    .build())
                .build(),
            IOReactorConfig.custom()
                           .setSoTimeout(Timeout.ofSeconds(30))
                           .setTcpNoDelay(true)
                           .setSoKeepAlive(true)
                           .build(),
            RequestConfig.custom()
                         .setConnectionRequestTimeout(Timeout.ofSeconds(30))
                         .setResponseTimeout(Timeout.ofSeconds(60))
                         .build()
        ));
  }
}
