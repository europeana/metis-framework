package eu.europeana.metis.mongo.utils;

import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.or;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.model.Collation;
import dev.morphia.Datastore;
import dev.morphia.Morphia;
import dev.morphia.aggregation.Aggregation;
import dev.morphia.aggregation.AggregationOptions;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import eu.europeana.metis.mongo.model.HasMongoObjectId;
import eu.europeana.metis.mongo.utils.MorphiaUtils;
import eu.europeana.metis.mongo.utils.ObjectIdSerializer;
import java.util.List;
import java.util.Objects;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link MorphiaUtils}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class MorphiaUtilsTest {

  private final static String DATABASE_NAME = "dbTest";

  private static Datastore datastore;

  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;

  @BeforeAll
  static void setup() {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    final String mongoHost = embeddedLocalhostMongo.getMongoHost();
    final int mongoPort = embeddedLocalhostMongo.getMongoPort();
    final MongoClient mongoClient = MongoClients.create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    datastore = Morphia.createDatastore(mongoClient, DATABASE_NAME);
    addEntitiesToDatastore();
  }

  @AfterAll
  static void tearDown() {
    removeEntitiesFromDatastore();
    embeddedLocalhostMongo.stop();
  }

  @Test
  void getListOfQueryRetryable() {
    Query<DummyEntity> query = datastore.find(DummyEntity.class);

    query.filter(or(eq("name", "testEntity1"), eq("name", "testEntity2")));
    final FindOptions findOptions = new FindOptions()
        .skip(0)
        .limit(1);
    List<DummyEntity> entityList = MorphiaUtils.getListOfQueryRetryable(query, findOptions);

    assertEquals(1, entityList.size());
  }

  @Test
  void testGetListOfQueryRetryable() {
    Query<DummyEntity> query = datastore.find(DummyEntity.class);

    query.filter(eq("name", "testEntity1"));

    List<DummyEntity> entityList = MorphiaUtils.getListOfQueryRetryable(query);

    assertEquals(1, entityList.size());
  }

  @Test
  void getListOfAggregationRetryable() {
    final Aggregation<DummyEntity> aggregation = datastore.aggregate(DummyEntity.class);
    aggregation.count("name");

    final List<DummyEntity> dummyEntityList = MorphiaUtils.getListOfAggregationRetryable(aggregation, DummyEntity.class);

    assertEquals(1, dummyEntityList.size());
    assertEquals(5, Integer.valueOf(dummyEntityList.get(0).name));
  }

  @Test
  void testGetListOfAggregationRetryable() {
    final Aggregation<DummyEntity> aggregation = datastore.aggregate(DummyEntity.class);

    aggregation.count("name");
    final AggregationOptions aggregationOptions = new AggregationOptions();
    aggregationOptions.collation(Collation.builder().locale("nl").build());

    final List<DummyEntity> dummyEntityList = MorphiaUtils.getListOfAggregationRetryable(aggregation, DummyEntity.class,
        aggregationOptions);

    assertEquals(1, dummyEntityList.size());
    assertEquals(5, Integer.valueOf(dummyEntityList.get(0).name));
  }

  private static void addEntitiesToDatastore() {
    DummyEntity dummyEntity = new DummyEntity();
    dummyEntity.setName("testEntity1");
    datastore.save(dummyEntity);
    dummyEntity = new DummyEntity();
    dummyEntity.setName("testEntity2");
    datastore.save(dummyEntity);
    dummyEntity = new DummyEntity();
    dummyEntity.setName("Entity3");
    datastore.save(dummyEntity);
    dummyEntity = new DummyEntity();
    dummyEntity.setName("Entity4");
    datastore.save(dummyEntity);
    dummyEntity = new DummyEntity();
    dummyEntity.setName("Entity5");
    datastore.save(dummyEntity);
  }

  private static void removeEntitiesFromDatastore() {
    datastore.delete(datastore.find(DummyEntity.class).filter(eq("name", "testEntity1")).first());
    datastore.delete(datastore.find(DummyEntity.class).filter(eq("name", "testEntity2")).first());
    datastore.delete(datastore.find(DummyEntity.class).filter(eq("name", "Entity3")).first());
    datastore.delete(datastore.find(DummyEntity.class).filter(eq("name", "Entity4")).first());
    datastore.delete(datastore.find(DummyEntity.class).filter(eq("name", "Entity5")).first());
  }

  @Entity
  private static class DummyEntity implements HasMongoObjectId {

    @Id
    @JsonSerialize(using = ObjectIdSerializer.class)
    private ObjectId id;

    private String name;

    public DummyEntity() {

    }

    @Override
    public ObjectId getId() {
      return id;
    }

    @Override
    public void setId(ObjectId id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    @Override
    public String toString() {
      return "TestObject{" +
          "id=" + id +
          ", name='" + name + '\'' +
          '}';
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) {
        return true;
      }
      if (!(o instanceof DummyEntity)) {
        return false;
      }

      final DummyEntity that = (DummyEntity) o;

      if (!Objects.equals(id, that.id)) {
        return false;
      }
      return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
      int result = id != null ? id.hashCode() : 0;
      result = 31 * result + (name != null ? name.hashCode() : 0);
      return result;
    }
  }
}
