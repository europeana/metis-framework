package eu.europeana.enrichment.api.external.impl;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import eu.europeana.entity.client.config.EntityClientConfiguration;
import eu.europeana.entity.client.exception.EntityClientException;
import java.util.Properties;
import org.junit.jupiter.api.Test;

class ClientEntityResolverFactoryTest {

  @Test
  void create() throws EntityClientException {
    Properties properties = new Properties();
    properties.setProperty("entity.api.url", "url");
    properties.setProperty("entity.management.url", "management");
    properties.setProperty("token_endpoint", "dummytoken");
    properties.setProperty("grant_params", "dummyparams");
    EntityClientConfiguration entityApiClientConfiguration = new EntityClientConfiguration(properties);

    ClientEntityResolverFactory clientEntityResolverFactory = new ClientEntityResolverFactory(entityApiClientConfiguration);

    assertInstanceOf(ClientEntityResolver.class, clientEntityResolverFactory.create());
  }
}
