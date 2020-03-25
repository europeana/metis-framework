package eu.europeana.enrichment.service;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.ObjectIdSerializer;
import eu.europeana.enrichment.api.internal.MongoTerm;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.utils.EnrichmentEntityDao;
import eu.europeana.enrichment.utils.EntityClass;
import eu.europeana.enrichment.utils.InputValue;
import eu.europeana.enrichment.utils.RedisProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
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

  private static final int SECONDS_PER_MINUTE = 60;
  private static final int MILLISECONDS_PER_SECOND = 1000;
  private static final int LANGUAGE_TAG_LENGTH = 2;
  private static final int COUNT_OF_ITEMS_COLLECTED_TO_LOG = 100;

  private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
  private static final List<EntityType> ENTITY_TYPES = createEntityTypeList();
  private static final Pattern PATTERN_MATCHING_VERY_BROAD_TIMESPANS = Pattern
      .compile("http://semium.org/time/(ChronologicalPeriod$|Time$|(AD|BC)[1-9]x{3}$)");
  private final EnrichmentEntityDao entityDao;
  private final RedisProvider redisProvider;

  /**
   * Constructor with all required parameters.
   * <p>Based on the parameter {@code populate}, the populating of redis will be enabled or not and
   * if so it will check the {@link #CACHED_ENRICHMENT_STATUS} field value in the redis db and
   * depending on it's status it will start populating redis with values or not.</p>
   *
   * @param entityDao the dao where the entity will be read from
   * @param provider the redis connection provider
   * @param populate the flag to enable populating or not, if applicable
   */
  public RedisInternalEnricher(EnrichmentEntityDao entityDao, RedisProvider provider,
      boolean populate) {
    this.entityDao = entityDao;
    SimpleModule sm = new SimpleModule("test", Version.unknownVersion());
    sm.addSerializer(new ObjectIdSerializer());
    OBJECT_MAPPER.registerModule(sm);
    redisProvider = provider;
    if (populate) {
      Jedis jedis = redisProvider.getJedis();
      if (jedis.exists(CACHED_ENRICHMENT_STATUS)
          && (StringUtils.equals(jedis.get(CACHED_ENRICHMENT_STATUS), "started")
          || StringUtils.equals(jedis.get(CACHED_ENRICHMENT_STATUS), "finished"))) {
        if (LOGGER.isInfoEnabled()) {
          LOGGER.info("Status 'enrichmentstatus' exists with value: {}", check());
        }
      } else {
        LOGGER.info(
            "Redis status 'enrichmentstatus' does not exist or is not in a 'started' or 'finished' state.");
        LOGGER.info("Re-populating Redis from Mongo");
        jedis.close();
        populate();
      }
    }
  }

  private static List<EntityType> createEntityTypeList() {
    final ArrayList<EntityType> entityTypes = new ArrayList<>();
    entityTypes.add(new EntityType(EntityClass.AGENT, CACHED_AGENT));
    entityTypes.add(new EntityType(EntityClass.CONCEPT, CACHED_CONCEPT));
    entityTypes.add(new EntityType(EntityClass.PLACE, CACHED_PLACE));
    entityTypes.add(new EntityType(EntityClass.TIMESPAN, CACHED_TIMESPAN));
    return entityTypes;
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
      removeKeysForEntity(jedis, str, CACHED_CONCEPT);
      removeKeysForEntity(jedis, str, CACHED_AGENT);
      removeKeysForEntity(jedis, str, CACHED_PLACE);
      removeKeysForEntity(jedis, str, CACHED_TIMESPAN);
    }
    jedis.close();
  }

  private void removeKeysForEntity(Jedis jedis, String str, String cachedEntity) {
    Set<String> conceptKeys = jedis.keys(cachedEntity + CACHED_ENTITY_WILDCARD);
    for (String key : conceptKeys) {
      jedis.srem(key, str);
    }
  }

  private void populate() {
    long startTime = System.currentTimeMillis();
    setStatus("started");
    for (EntityType type : ENTITY_TYPES) {
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
      if (i % COUNT_OF_ITEMS_COLLECTED_TO_LOG == 0) {
        LOGGER.info("Elements added: {} out of: {}", i, termCount);
      }
    }
    jedis.close();
  }

  private void loadEntity(EntityType entityType, MongoTerm term, Jedis jedis) {
    MongoTermList<?> termList = entityDao.findByCode(term.getCodeUri(), entityType.entityClass);
    if (termList != null) {
      loadEntities(entityType, term, jedis, termList);
    }
  }

  private void loadEntities(EntityType entityType, MongoTerm term, Jedis jedis,
      MongoTermList<?> termList) {
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
        jedis.sadd(entityType.cachedEntityPrefix + CACHED_ENTITY + term.getLang() +
            CACHE_NAME_SEPARATOR + term.getLabel(), term.getCodeUri());
      }
      jedis.hset(entityType.cachedEntityPrefix + CACHED_URI, term.getCodeUri(),
          OBJECT_MAPPER.writeValueAsString(entityWrapper));
      List<String> parents = this.findParents(termList.getParent(), entityType.entityClass);
      if (!parents.isEmpty()) {
        jedis.sadd(entityType.cachedEntityPrefix + CACHED_PARENT + term.getCodeUri(),
            parents.toArray(new String[]{}));
      }
      if (termList.getOwlSameAs() != null) {
        for (String sameAs : termList.getOwlSameAs()) {
          jedis.hset(entityType.cachedEntityPrefix + CACHED_SAMEAS, sameAs, term.getCodeUri());
        }
      }
    } catch (IOException exception) {
      LOGGER.warn("", exception);
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

  private List<String> findParents(String parent, EntityClass entityClass) {
    List<String> parentEntities = new ArrayList<>();
    MongoTermList<?> parents = entityDao.findByCode(parent, entityClass);
    if (parents != null) {
      parentEntities.add(parents.getCodeUri());
      if (parents.getParent() != null && !parent.equals(parents.getParent())) {
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

    if (StringUtils.isEmpty(lang) || lang.length() != LANGUAGE_TAG_LENGTH) {
      lang = "def";
    }
    Jedis jedis = redisProvider.getJedis();
    if (!jedis.isConnected()) {
      jedis.connect();
    }
    final String cacheKey =
        cachedEntityPrefix + CACHED_ENTITY + lang + CACHE_NAME_SEPARATOR + value;
    if (jedis.exists(cacheKey)) {
      Set<String> urisToCheck = jedis.smembers(cacheKey);
      for (String uri : urisToCheck) {
        EntityWrapper entity = OBJECT_MAPPER
            .readValue(jedis.hget(cachedEntityPrefix + CACHED_URI, uri), EntityWrapper.class);
        entity.setOriginalField(originalField);
        result.add(entity);
        result.addAll(findParentEntities(cachedEntityPrefix, jedis, uri));
      }
    }
    jedis.close();
    return new ArrayList<>(result);
  }

  private Set<EntityWrapper> findParentEntities(String cachedEntityPrefix, Jedis jedis,
      String uri) throws IOException {
    Set<EntityWrapper> entityWrapperSet = new HashSet<>();
    if (jedis.exists(cachedEntityPrefix + CACHED_PARENT + uri)) {
      Set<String> parentEntityUrls = jedis.smembers(cachedEntityPrefix + CACHED_PARENT + uri);
      for (String parentEntityUrl : parentEntityUrls) {
        //For timespans, do not get entities for very broad timespans
        if (CACHED_TIMESPAN.equals(cachedEntityPrefix) && PATTERN_MATCHING_VERY_BROAD_TIMESPANS
            .matcher(parentEntityUrl).matches()) {
          continue;
        }
        EntityWrapper parentEntityWrapper = OBJECT_MAPPER.readValue(
            jedis.hget(cachedEntityPrefix + CACHED_URI, parentEntityUrl), EntityWrapper.class);
        entityWrapperSet.add(parentEntityWrapper);
      }
    }
    return entityWrapperSet;
  }

  /**
   * Get an enrichment document based on the id provided.
   *
   * @param requestedId the provided id
   * @return the enrichment document corresponding to the provided id
   * @throws IOException if something went wrong when accessing the db
   */
  public EntityWrapper getById(String requestedId) throws IOException {

    // Get the result providers - this is constant and does not depend on the input.
    final List<GetterByUri> getters = Arrays.asList(
            (uri, jedis) -> getEntityWrapper(uri, jedis, CACHED_AGENT),
            (uri, jedis) -> getEntityWrapper(uri, jedis, CACHED_CONCEPT),
            (uri, jedis) -> getEntityWrapper(uri, jedis, CACHED_TIMESPAN),
            (uri, jedis) -> getEntityWrapper(uri, jedis, CACHED_PLACE)
    );

    // Get the first non-null result.
    return getFirstMatch(getters, requestedId);
  }

  /**
   * Get an enrichment document based on the uri provided.
   *
   * @param requestedUri the provided uri
   * @return the enrichment document corresponding to the provided uri
   * @throws IOException if something went wrong when accessing the db
   */
  public EntityWrapper getByUri(String requestedUri) throws IOException {

    // Get the result providers - this is constant and does not depend on the input.
    final List<GetterByUri> getters = Arrays.asList(
            (uri, jedis) -> getEntityWrapper(uri, jedis, CACHED_AGENT),
            (uri, jedis) -> getEntityWrapper(uri, jedis, CACHED_CONCEPT),
            (uri, jedis) -> getEntityWrapper(uri, jedis, CACHED_TIMESPAN),
            (uri, jedis) -> getEntityWrapper(uri, jedis, CACHED_PLACE),
            (uri, jedis) -> getEntityWrapperFromSameAs(uri, jedis, CACHED_AGENT),
            (uri, jedis) -> getEntityWrapperFromSameAs(uri, jedis, CACHED_CONCEPT),
            (uri, jedis) -> getEntityWrapperFromSameAs(uri, jedis, CACHED_TIMESPAN),
            (uri, jedis) -> getEntityWrapperFromSameAs(uri, jedis, CACHED_PLACE)
    );

    // Get the first non-null result.
    return getFirstMatch(getters, requestedUri);
  }

  private EntityWrapper getFirstMatch(List<GetterByUri> getters, String requestedUri)
          throws IOException {
    try (final Jedis jedis = redisProvider.getJedis()) {
      for (GetterByUri getter : getters) {
        final EntityWrapper result = getter.getByUri(requestedUri, jedis);
        if (result != null) {
          return result;
        }
      }
      return null;
    }
  }

  @FunctionalInterface
  private interface GetterByUri {

    EntityWrapper getByUri(String uri, Jedis jedis) throws IOException;
  }

  private EntityWrapper getEntityWrapperFromSameAs(String uri, Jedis jedis, String cachedEntity)
          throws IOException {
    if (jedis.hexists(cachedEntity + CACHED_SAMEAS, uri)) {
      return OBJECT_MAPPER.readValue(jedis.hget(cachedEntity + CACHED_URI,
              jedis.hget(cachedEntity + CACHED_SAMEAS, uri)), EntityWrapper.class);
    }
    return null;
  }

  private EntityWrapper getEntityWrapper(String uri, Jedis jedis, String cachedEntity)
          throws IOException {
    if (jedis.hexists(cachedEntity + CACHED_URI, uri)) {
      return OBJECT_MAPPER
              .readValue(jedis.hget(cachedEntity + CACHED_URI, uri), EntityWrapper.class);
    }
    return null;
  }

  private ObjectMapper getObjectMapper() {
    return OBJECT_MAPPER;
  }

  private static class EntityType {

    private final EntityClass entityClass;
    private final String cachedEntityPrefix;

    EntityType(EntityClass entityClass, String cachedEntityPrefix) {
      this.entityClass = entityClass;
      this.cachedEntityPrefix = cachedEntityPrefix;
    }
  }
}
