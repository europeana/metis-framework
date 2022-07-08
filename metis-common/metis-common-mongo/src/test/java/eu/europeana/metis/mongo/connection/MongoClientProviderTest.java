package eu.europeana.metis.mongo.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import com.mongodb.connection.ConnectionPoolSettings;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link MongoClientProvider}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class MongoClientProviderTest {

  private final static String DATABASE_NAME = "dbTest";

  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;

  @BeforeAll
  static void setup() {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
  }

  @AfterAll
  static void tearDown() {
    embeddedLocalhostMongo.stop();
  }

  private static MongoProperties getMongoProperties() {
    final String mongoHost = embeddedLocalhostMongo.getMongoHost();
    final int mongoPort = embeddedLocalhostMongo.getMongoPort();
    final MongoProperties<IllegalArgumentException> mongoProperties = new MongoProperties<>(
        IllegalArgumentException::new);
    mongoProperties.setMongoHosts(new String[]{mongoHost}, new int[]{mongoPort});
    mongoProperties.setMongoCredentials("user", "wachtwoord", "authenticationDB");
    mongoProperties.setApplicationName(DATABASE_NAME);
    mongoProperties.setMaxConnectionPoolSize(10);
    return mongoProperties;
  }

  @Test
  void getClientSettingsBuilder() {
    final MongoClientSettings mongoClientSettings = MongoClientProvider.getDefaultClientSettingsBuilder().build();
    assertFalse(mongoClientSettings.getRetryWrites());
    assertEquals(ReadPreference.secondaryPreferred(), mongoClientSettings.getReadPreference());
    assertEquals("Europeana Application Suite", mongoClientSettings.getApplicationName());
    assertEquals(30_000, mongoClientSettings.getConnectionPoolSettings().getMaxConnectionIdleTime(TimeUnit.MILLISECONDS));
    assertEquals(20, mongoClientSettings.getConnectionPoolSettings().getMaxSize());
  }

  @Test
  void create() {
    final String mongoHost = embeddedLocalhostMongo.getMongoHost();
    final int mongoPort = embeddedLocalhostMongo.getMongoPort();

    MongoClientProvider<IllegalArgumentException> mongoClientProvider = MongoClientProvider.create(
        String.format("mongodb://%s:%s", mongoHost, mongoPort));

    assertNotNull(mongoClientProvider);
  }

  @Test
  void createAsSupplier() {
    final String mongoHost = embeddedLocalhostMongo.getMongoHost();
    final int mongoPort = embeddedLocalhostMongo.getMongoPort();

    final Supplier<MongoClient> mongoClientSupplier = MongoClientProvider.createAsSupplier(String.format("mongodb://%s:%s", mongoHost, mongoPort));

    assertTrue(mongoClientSupplier.get() instanceof MongoClient);
  }

  @Test
  void getAuthenticationDatabase() {
    MongoClientProvider<IllegalArgumentException> mongoClientProvider = new MongoClientProvider<IllegalArgumentException>(getMongoProperties());

    assertNotNull(mongoClientProvider);
    assertEquals("authenticationDB", mongoClientProvider.getAuthenticationDatabase());
  }

  @Test
  void createMongoClient() {
    final MongoClient mongoClient = new MongoClientProvider<IllegalArgumentException>(getMongoProperties()).createMongoClient();

    assertNotNull(mongoClient);
    assertTrue(mongoClient instanceof MongoClient);
  }

  @Test
  void createConnectionPoolSettings() {
    final ConnectionPoolSettings connectionPoolSettings = MongoClientProvider.createConnectionPoolSettings(10);
    assertEquals(30_000, connectionPoolSettings.getMaxConnectionIdleTime(TimeUnit.MILLISECONDS));
    assertEquals(10, connectionPoolSettings.getMaxSize());
  }
}