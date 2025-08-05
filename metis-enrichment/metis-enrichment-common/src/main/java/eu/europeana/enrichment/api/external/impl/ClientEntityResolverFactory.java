package eu.europeana.enrichment.api.external.impl;

import eu.europeana.api.commons_sb3.auth.AuthenticationBuilder;
import eu.europeana.entity.client.EntityApiClient;
import eu.europeana.entity.client.config.EntityClientConfiguration;
import eu.europeana.entity.client.exception.EntityClientException;
import java.util.concurrent.TimeUnit;
import javax.validation.constraints.NotNull;
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
    this.entityApiClientConfiguration = entityApiClientConfiguration;
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
            PoolingAsyncClientConnectionManagerBuilder.create()
                                                      .setMaxConnTotal(200)
                                                      .setMaxConnPerRoute(50)
                                                      .setDefaultConnectionConfig(
                                                          ConnectionConfig.custom()
                                                                          .setValidateAfterInactivity(TimeValue.ofSeconds(5))
                                                                          .setTimeToLive(TimeValue.ofSeconds(60))
                                                                          .build())
                                                      .build(),
            IOReactorConfig.custom()
                           .setSoTimeout(Timeout.of(30, TimeUnit.SECONDS))
                           .build(),
            RequestConfig.custom()
                         .setConnectionRequestTimeout(Timeout.of(30, TimeUnit.SECONDS))
                         .setResponseTimeout(Timeout.of(30, TimeUnit.SECONDS))
                         .build()
        ));
  }
}
