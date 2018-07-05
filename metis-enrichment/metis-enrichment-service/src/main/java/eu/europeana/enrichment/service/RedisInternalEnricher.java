package eu.europeana.enrichment.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.ObjectIdSerializer;
import eu.europeana.enrichment.api.internal.MongoTerm;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.utils.EntityClass;
import eu.europeana.enrichment.utils.EnrichmentEntityDao;
import eu.europeana.enrichment.utils.InputValue;
import eu.europeana.metis.cache.redis.RedisProvider;
import redis.clients.jedis.Jedis;


/**
 * Main enrichment class
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
@Component
public class RedisInternalEnricher {

  private static final Logger LOGGER = LoggerFactory.getLogger(RedisInternalEnricher.class);

  private static final String CACHE_NAME_SEPARATOR = ":";

  private static final String CACHED_AGENT = "agent" + CACHE_NAME_SEPARATOR;
  private static final String CACHED_CONCEPT = "concept" + CACHE_NAME_SEPARATOR;
  private static final String CACHED_PLACE = "place" + CACHE_NAME_SEPARATOR;
  private static final String CACHED_TIMESPAN = "timespan" + CACHE_NAME_SEPARATOR;

  private static final String CACHED_ENTITY = "entity" + CACHE_NAME_SEPARATOR;
  private static final String CACHED_PARENT = "parent" + CACHE_NAME_SEPARATOR;
  private static final String CACHED_SAMEAS = "sameas";
  private static final String CACHED_URI = "uri";

  private static final String CACHED_ENTITY_DEF = CACHED_ENTITY + "def" + CACHE_NAME_SEPARATOR;
  private static final String CACHED_ENTITY_WILDCARD = CACHED_ENTITY + "*";

  private static final String CACHED_ENRICHMENT_STATUS = "enrichmentstatus";

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

  private static final int SECONDS_PER_MINUTE = 60;
  private static final int MILLISECONDS_PER_SECOND = 1000;

  private enum EntityType {

    AGENT(EntityClass.AGENT, CACHED_AGENT),

    CONCEPT(EntityClass.CONCEPT, CACHED_CONCEPT),

    PLACE(EntityClass.PLACE, CACHED_PLACE),

    TIMESTAMP(EntityClass.TIMESPAN, CACHED_TIMESPAN);

    public final EntityClass entityClass;
    public final String cachedEntityPrefix;

    private EntityType(EntityClass entityClass, String cachedEntityPrefix) {
      this.entityClass = entityClass;
      this.cachedEntityPrefix = cachedEntityPrefix;
    }
  }

  private final EnrichmentEntityDao entityDao;
  private final RedisProvider redisProvider;

  public RedisInternalEnricher(EnrichmentEntityDao entityDao, RedisProvider provider,
      boolean populate) {
    this.entityDao = entityDao;
    SimpleModule sm = new SimpleModule("test", Version.unknownVersion());
    sm.addSerializer(new ObjectIdSerializer());
    OBJECT_MAPPER.registerModule(sm);
    redisProvider = provider;
    if (populate) {
      Jedis jedis = redisProvider.getJedis();
      if (!jedis.exists(CACHED_ENRICHMENT_STATUS)
          || (!StringUtils.equals(jedis.get(CACHED_ENRICHMENT_STATUS), "started")
          && !StringUtils.equals(jedis.get(CACHED_ENRICHMENT_STATUS), "finished"))) {
        LOGGER.info(
            "Redis status 'enrichmentstatus' does not exist or is not in a 'started' or 'finished' state.");
        LOGGER.info("Re-populating Redis from Mongo");
        jedis.close();
        populate();
      } else {
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("Status 'enrichmentstatus' exists with value: {}", check());
        }
      }
    }
  }

  /**
   * Checks the status of the operation of populating fields from Mongo to Redis
   *
   * @return the status, can be "started" or "finished"
   */
  public final String check() {
    Jedis jedis = redisProvider.getJedis();
    String status = jedis.get(CACHED_ENRICHMENT_STATUS);
    jedis.close();
    return status;
  }

  /**
   * Restarts the population of Mongo to Redis
   */
  public void recreate() {
    LOGGER.info("Recreate triggered.");
    Jedis jedis = redisProvider.getJedis();
    jedis.del(CACHED_ENRICHMENT_STATUS);
    jedis.close();
    populate();
  }

  /**
   * Removes all information from Redis
   */
  public void emptyCache() {
    LOGGER.info("Empty cache");
    Jedis jedis = redisProvider.getJedis();
    jedis.flushAll();
    jedis.close();
    populate();
  }

  /**
   * Remove a list of uris from redis.
   *
   * @param uris the list of uris to be removed
   */
  public void remove(List<String> uris) {
    Jedis jedis = redisProvider.getJedis();
    for (String str : uris) {
      jedis.del(CACHED_CONCEPT + CACHED_PARENT + str);
      jedis.del(CACHED_AGENT + CACHED_PARENT + str);
      jedis.del(CACHED_TIMESPAN + CACHED_PARENT + str);
      jedis.del(CACHED_PLACE + CACHED_PARENT + str);
      jedis.hdel(CACHED_CONCEPT + CACHED_URI, str);
      jedis.hdel(CACHED_AGENT + CACHED_URI, str);
      jedis.hdel(CACHED_TIMESPAN + CACHED_URI, str);
      jedis.hdel(CACHED_PLACE + CACHED_URI, str);
      Set<String> conceptKeys = jedis.keys(CACHED_CONCEPT + CACHED_ENTITY_WILDCARD);
      for (String key : conceptKeys) {
        jedis.srem(key, str);
      }
      Set<String> agentKeys = jedis.keys(CACHED_AGENT + CACHED_ENTITY_WILDCARD);
      for (String key : agentKeys) {
        jedis.srem(key, str);
      }
      Set<String> placeKeys = jedis.keys(CACHED_PLACE + CACHED_ENTITY_WILDCARD);
      for (String key : placeKeys) {
        jedis.srem(key, str);
      }
      Set<String> timespanKeys = jedis.keys(CACHED_TIMESPAN + CACHED_ENTITY_WILDCARD);
      for (String key : timespanKeys) {
        jedis.srem(key, str);
      }
    }
    jedis.close();
  }

  private void populate() {
    long startTime = System.currentTimeMillis();
    setStatus("started");
    for (EntityType type : EntityType.values()) {
      loadEntities(type);
    }
    setStatus("finished");
    int totalSeconds = (int) ((System.currentTimeMillis() - startTime) / MILLISECONDS_PER_SECOND);
    int seconds = totalSeconds % SECONDS_PER_MINUTE;
    int minutes = (totalSeconds - seconds) / SECONDS_PER_MINUTE;
    LOGGER.info("Time spent in populating Redis. minutes: {}, seconds: {}", minutes, seconds);
  }

  private void setStatus(String status) {
    Jedis jedis = redisProvider.getJedis();
    jedis.set(CACHED_ENRICHMENT_STATUS, status);
    jedis.close();
  }

  private void loadEntities(EntityType entityType) {
    Jedis jedis = redisProvider.getJedis();
    List<MongoTerm> terms = entityDao.getAllMongoTerms(entityType.entityClass);
    int termCount = terms.size();
    LOGGER.info("Found entities of type {}: {}", entityType.entityClass, termCount);
    int i = 0;
    for (MongoTerm term : terms) {
      loadEntity(entityType, term, jedis);
      i++;
      if (i % 100 == 0) {
        LOGGER.info("Elements added: {} out of: {}", i, termCount);
      }
    }
    jedis.close();
  }
  
  private void loadEntity(EntityType entityType, MongoTerm term, Jedis jedis) {
    MongoTermList<?> termList = entityDao.findByCode(term.getCodeUri(), entityType.entityClass);
    if (termList != null) {
      try {
        EntityWrapper entityWrapper = new EntityWrapper();
        entityWrapper.setOriginalField("");
        entityWrapper.setEntityClass(entityType.entityClass);
        entityWrapper.setContextualEntity(
            this.getObjectMapper().writeValueAsString(termList.getRepresentation()));
        entityWrapper.setOriginalValue(term.getOriginalLabel());
        entityWrapper.setUrl(term.getCodeUri());
        jedis.sadd(entityType.cachedEntityPrefix + CACHED_ENTITY_DEF + term.getLabel(),
            term.getCodeUri());
        if (term.getLang() != null) {
          jedis.sadd(entityType.cachedEntityPrefix + CACHED_ENTITY + term.getLang() + ":"
              + term.getLabel(), term.getCodeUri());
        }
        jedis.hset(entityType.cachedEntityPrefix + CACHED_URI, term.getCodeUri(),
            OBJECT_MAPPER.writeValueAsString(entityWrapper));
        List<String> parents = this.findParents(termList.getParent(), entityType.entityClass);
        jedis.sadd(entityType.cachedEntityPrefix + CACHED_PARENT + term.getCodeUri(),
            parents.toArray(new String[] {}));
        if (termList.getOwlSameAs() != null) {
          for (String sameAs : termList.getOwlSameAs()) {
            jedis.hset(entityType.cachedEntityPrefix + CACHED_SAMEAS, sameAs, term.getCodeUri());
          }
        }
      } catch (IOException exception) {
        LOGGER.warn("", exception);
      }
    }
  }

    /**
   * The internal enrichment functionality not to be exposed yet as there is a strong dependency to
   * the external resources to recreate the DB The enrichment is performed by lowercasing every
   * value so that searchability in the DB is enhanced, but the Capitalized version is always
   * retrieved
   *
   * @param values The values to enrich
   * @return A list of enrichments
   * @throws IOException 
   */
  protected List<EntityWrapper> tag(List<InputValue> values) throws IOException {

    List<EntityWrapper> entities = new ArrayList<>();
    for (InputValue inputValue : values) {
      if (inputValue.getVocabularies() == null) {
        continue;
      }
      for (EntityClass voc : inputValue.getVocabularies()) {
        entities.addAll(findEntities(inputValue.getValue().toLowerCase(Locale.US),
            inputValue.getOriginalField(), inputValue.getLanguage(), voc));
      }
    }
    return entities;
  }

  private List<String> findParents(String parent, EntityClass entityClass) throws IOException {
    List<String> parentEntities = new ArrayList<>();
    MongoTermList<?> parents = entityDao.findByCode(parent, entityClass);
    if (parents != null) {
      parentEntities.add(parents.getCodeUri());
      if (parents.getParent() != null && !parent.equals(parents.getParent())) {
        // TODO why is this necessary in this particular case?
        if (entityClass == EntityClass.TIMESPAN) {
          try {
            Thread.sleep(10L);
          } catch (InterruptedException var5) {
            Thread.currentThread().interrupt();
          }
        }
        parentEntities.addAll(this.findParents(parents.getParent(), entityClass));
      }
    }

    return parentEntities;
  }

  private List<EntityWrapper> findEntities(String lowerCase, String field, String lang,
      EntityClass entityClass) throws IOException {
    final String cachedEntityPrefix;
    switch (entityClass) {
      case AGENT:
        cachedEntityPrefix = CACHED_AGENT;
        break;
      case CONCEPT:
        cachedEntityPrefix = CACHED_CONCEPT;
        break;
      case PLACE:
        cachedEntityPrefix = CACHED_PLACE;
        break;
      case TIMESPAN:
        cachedEntityPrefix = CACHED_TIMESPAN;
        break;
      default:
        throw new IllegalStateException("Unknown entity class: " + entityClass.name());
    }
    return findEntities(lowerCase, field, lang, cachedEntityPrefix);
  }

  private List<EntityWrapper> findEntities(String value, String originalField, String lang,
      String cachedEntityPrefix) throws IOException {
    Set<EntityWrapper> result = new HashSet<>();

    if (StringUtils.isEmpty(lang) || lang.length() != 2) {
      lang = "def";
    }
    Jedis jedis = redisProvider.getJedis();
    if (!jedis.isConnected()) {
      jedis.connect();
    }
    if (jedis.exists(cachedEntityPrefix + CACHED_ENTITY + lang + ":" + value)) {
      Set<String> urisToCheck =
          jedis.smembers(cachedEntityPrefix + CACHED_ENTITY + lang + ":" + value);
      for (String uri : urisToCheck) {
        EntityWrapper entity = OBJECT_MAPPER
            .readValue(jedis.hget(cachedEntityPrefix + CACHED_URI, uri), EntityWrapper.class);
        entity.setOriginalField(originalField);
        result.add(entity);
        if (jedis.exists(cachedEntityPrefix + CACHED_PARENT + uri)) {
          Set<String> parents = jedis.smembers(cachedEntityPrefix + CACHED_PARENT + uri);
          for (String parent : parents) {
            EntityWrapper parentEntity = OBJECT_MAPPER.readValue(
                jedis.hget(cachedEntityPrefix + CACHED_URI, parent), EntityWrapper.class);
            result.add(parentEntity);
          }
        }
      }
    }
    jedis.close();
    return new ArrayList<>(result);
  }

  public EntityWrapper getByUri(String uri) throws IOException {
    Jedis jedis = redisProvider.getJedis();
    EntityWrapper entityWrapper = null;
    if (jedis.hexists(CACHED_AGENT + CACHED_URI, uri)) {
      entityWrapper =
          OBJECT_MAPPER.readValue(jedis.hget(CACHED_AGENT + CACHED_URI, uri), EntityWrapper.class);
    }
    if (jedis.hexists(CACHED_CONCEPT + CACHED_URI, uri)) {
      entityWrapper = OBJECT_MAPPER.readValue(jedis.hget(CACHED_CONCEPT + CACHED_URI, uri),
          EntityWrapper.class);
    }
    if (jedis.hexists(CACHED_TIMESPAN + CACHED_URI, uri)) {
      entityWrapper = OBJECT_MAPPER.readValue(jedis.hget(CACHED_TIMESPAN + CACHED_URI, uri),
          EntityWrapper.class);
    }
    if (jedis.hexists(CACHED_PLACE + CACHED_URI, uri)) {
      entityWrapper =
          OBJECT_MAPPER.readValue(jedis.hget(CACHED_PLACE + CACHED_URI, uri), EntityWrapper.class);
    }

    if (jedis.hexists(CACHED_AGENT + CACHED_SAMEAS, uri)) {
      entityWrapper = OBJECT_MAPPER.readValue(
          jedis.hget(CACHED_AGENT + CACHED_URI, jedis.hget(CACHED_AGENT + CACHED_SAMEAS, uri)),
          EntityWrapper.class);
    }
    if (jedis.hexists(CACHED_CONCEPT + CACHED_SAMEAS, uri)) {
      entityWrapper = OBJECT_MAPPER.readValue(
          jedis.hget(CACHED_CONCEPT + CACHED_URI, jedis.hget(CACHED_CONCEPT + CACHED_SAMEAS, uri)),
          EntityWrapper.class);
    }
    if (jedis.hexists(CACHED_TIMESPAN + CACHED_SAMEAS, uri)) {
      entityWrapper = OBJECT_MAPPER.readValue(jedis.hget(CACHED_TIMESPAN + CACHED_URI,
          jedis.hget(CACHED_TIMESPAN + CACHED_SAMEAS, uri)), EntityWrapper.class);
    }
    if (jedis.hexists(CACHED_PLACE + CACHED_SAMEAS, uri)) {
      entityWrapper = OBJECT_MAPPER.readValue(
          jedis.hget(CACHED_PLACE + CACHED_URI, jedis.hget(CACHED_PLACE + CACHED_SAMEAS, uri)),
          EntityWrapper.class);
    }
    jedis.close();
    return entityWrapper;
  }

  private ObjectMapper getObjectMapper() {
    return OBJECT_MAPPER;
  }
}
