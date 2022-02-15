package eu.europeana.metis.mongo.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.MongoClientSettings;
import com.mongodb.ReadPreference;
import com.mongodb.client.MongoClient;
import eu.europeana.metis.mongo.connection.MongoClientProvider;
import eu.europeana.metis.mongo.connection.MongoProperties;
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

  @Test
  void getDefaultClientSettingsBuilder() {
    MongoClientSettings.Builder actual = MongoClientProvider.getDefaultClientSettingsBuilder();

    assertFalse(actual.build().getRetryWrites());
    assertEquals(ReadPreference.secondaryPreferred(), actual.build().getReadPreference());
    assertEquals("Europeana Application Suite", actual.build().getApplicationName());
    assertEquals(30_000, actual.build().getConnectionPoolSettings().getMaxConnectionIdleTime(TimeUnit.MILLISECONDS));
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

  private static MongoProperties getMongoProperties() {
    final String mongoHost = embeddedLocalhostMongo.getMongoHost();
    final int mongoPort = embeddedLocalhostMongo.getMongoPort();
    final MongoProperties<IllegalArgumentException> mongoProperties = new MongoProperties<>(
        IllegalArgumentException::new);
    mongoProperties.setMongoHosts(new String[]{mongoHost}, new int[]{mongoPort});
    mongoProperties.setMongoCredentials("user","wachtwoord","authenticationDB");
    mongoProperties.setApplicationName(DATABASE_NAME);
    return mongoProperties;
  }
}