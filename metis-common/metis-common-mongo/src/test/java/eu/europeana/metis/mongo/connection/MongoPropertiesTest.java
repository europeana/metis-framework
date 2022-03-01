package eu.europeana.metis.mongo.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.metis.mongo.connection.MongoProperties;
import eu.europeana.metis.mongo.connection.MongoProperties.ReadPreferenceValue;
import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link MongoProperties}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class MongoPropertiesTest {

  private MongoProperties mongoProperties;

  @Test
  void setAllProperties() throws Exception {
    mongoProperties = new MongoProperties<>(IllegalArgumentException::new);

    mongoProperties.setAllProperties(new String[]{"localhost"},
        new int[]{8521},
        "authenticationdb",
        "userName",
        "password", true,
        ReadPreferenceValue.PRIMARY_PREFERRED,
        "testAplication");

    assertMongoProperties(mongoProperties);
  }

  @Test
  void setMongoPropertiesOnlyAuthentication() throws Exception {
    mongoProperties = new MongoProperties<>(IllegalArgumentException::new);

    mongoProperties.setAllProperties(new String[]{"localhost"},
        new int[]{8521},
        "authenticationdb",
        "userName",
        "password");

    assertMongoPropertiesOnlyWithAuthentication(mongoProperties);
  }

  @Test
  void setMongoPropertiesWithoutAuthentication() throws Exception {
    mongoProperties = new MongoProperties<>(IllegalArgumentException::new);

    mongoProperties.setAllProperties(new String[]{"localhost"},
        new int[]{8521},
        "",
        "userName",
        "");

    assertMongoPropertiesWithoutAuthentication(mongoProperties);
  }

  @Test
  void setMongoPropertiesSsl() throws Exception {
    mongoProperties = new MongoProperties<>(IllegalArgumentException::new);

    mongoProperties.setAllProperties(new String[]{"localhost"},
        new int[]{8521},
        "authenticationdb",
        "userName",
        "password");
    mongoProperties.setMongoEnableSsl();

    assertMongoPropertiesOnlyWithAuthentication(mongoProperties);
    assertTrue(mongoProperties.mongoEnableSsl());
  }

  @Test
  void addMongoHost() throws Exception {
    mongoProperties = new MongoProperties<>(IllegalArgumentException::new);

    mongoProperties.setAllProperties(new String[]{"localhost"},
        new int[]{8521},
        "authenticationdb",
        "userName",
        "password");

    mongoProperties.addMongoHost(new InetSocketAddress("192.168.1.2", 12345));

    assertEquals(2, mongoProperties.getMongoHosts().size());
    assertEquals("192.168.1.2:12345", mongoProperties.getMongoHosts().get(1).toString());
  }

  private static void assertMongoPropertiesOnlyWithAuthentication(MongoProperties mongoProperties) throws Exception {
    assertEquals("localhost:8521", mongoProperties.getMongoHosts().get(0).toString());
    assertNotNull(mongoProperties.getMongoCredentials());
    assertEquals("authenticationdb", mongoProperties.getMongoCredentials().getSource());
    assertEquals("userName", mongoProperties.getMongoCredentials().getUserName());
    assertEquals("password", new String(mongoProperties.getMongoCredentials().getPassword()));
  }

  private static void assertMongoPropertiesWithoutAuthentication(MongoProperties mongoProperties) throws Exception {
    assertEquals("localhost:8521", mongoProperties.getMongoHosts().get(0).toString());
    assertNull(mongoProperties.getMongoCredentials());
  }

  private static void assertMongoProperties(MongoProperties mongoProperties) throws Exception {
    assertMongoPropertiesOnlyWithAuthentication(mongoProperties);
    assertTrue(mongoProperties.mongoEnableSsl());
    assertEquals(ReadPreferenceValue.PRIMARY_PREFERRED, mongoProperties.getReadPreferenceValue());
    assertEquals("testAplication", mongoProperties.getApplicationName());
  }
}