package eu.europeana.enrichment.service;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performAction;
import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performFunction;
import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performThrowingAction;

import com.fasterxml.jackson.core.Version;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import eu.europeana.corelib.solr.entity.AbstractEdmEntityImpl;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.ObjectIdSerializer;
import eu.europeana.enrichment.api.internal.AgentTermList;
import eu.europeana.enrichment.api.internal.ConceptTermList;
import eu.europeana.enrichment.api.internal.MongoTerm;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.api.internal.PlaceTermList;
import eu.europeana.enrichment.api.internal.TimespanTermList;
import eu.europeana.enrichment.service.exception.CacheStatusException;
import eu.europeana.enrichment.utils.EntityDao;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.enrichment.utils.InputValue;
import eu.europeana.enrichment.utils.RedisProvider;
import eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.ThrowingConsumer;
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
  private static final List<EntityInfo<?, ?>> ENTITY_TYPES = createEntityTypeList();
  private static final Pattern PATTERN_MATCHING_VERY_BROAD_TIMESPANS = Pattern
      .compile("http://semium.org/time/(ChronologicalPeriod$|Time$|(AD|BC)[1-9]x{3}$)");
  private final EntityDao entityDao;
  private final RedisProvider redisProvider;

  /**
   * Constructor with all required parameters.
   *
   * @param entityDao the dao where the entity will be read from
   * @param provider the redis connection provider
   */
  public RedisInternalEnricher(EntityDao entityDao, RedisProvider provider) {
    this.entityDao = entityDao;
    SimpleModule sm = new SimpleModule("test", Version.unknownVersion());
    sm.addSerializer(new ObjectIdSerializer());
    OBJECT_MAPPER.registerModule(sm);
    redisProvider = provider;
  }

  private static List<EntityInfo<?, ?>> createEntityTypeList() {
    final ArrayList<EntityInfo<?, ?>> entityInfos = new ArrayList<>();
    entityInfos.add(new EntityInfo<>(EntityType.AGENT, AgentTermList.class, CACHED_AGENT));
    entityInfos.add(new EntityInfo<>(EntityType.CONCEPT, ConceptTermList.class, CACHED_CONCEPT));
    entityInfos.add(new EntityInfo<>(EntityType.PLACE, PlaceTermList.class, CACHED_PLACE));
    entityInfos.add(new EntityInfo<>(EntityType.TIMESPAN, TimespanTermList.class, CACHED_TIMESPAN));
    return entityInfos;
  }

  /**
   * Checks the status of the operation of populating fields from Mongo to Redis.
   *
   * @return the status.
   */
  public final CacheStatus getCurrentStatus() {
    final String statusString;
    try (Jedis jedis = redisProvider.getJedis()) {
      statusString = performFunction(jedis, j -> j.get(CACHED_ENRICHMENT_STATUS));
    }
    return CacheStatus.getByName(statusString);
  }

  private void setCurrentStatus(CacheStatus status) {
    try (Jedis jedis = redisProvider.getJedis()) {
      if (status == null || status == CacheStatus.NONE) {
        performAction(jedis, j -> j.del(CACHED_ENRICHMENT_STATUS));
      } else {
        performAction(jedis, j -> j.set(CACHED_ENRICHMENT_STATUS, status.name()));
      }
    }
  }

  private void verifyCacheReady() throws CacheStatusException {
    final CacheStatus status = getCurrentStatus();
    if (status != CacheStatus.FINISHED && status != CacheStatus.TRIGGERED) {
      throw new CacheStatusException(status);
    }
  }

  /**
   * Triggers a cache recreate.
   *
   * @throws CacheStatusException if the cache is not fully created.
   */
  public void triggerRecreate() throws CacheStatusException {
    verifyCacheReady();
    setCurrentStatus(CacheStatus.TRIGGERED);
  }

  /**
   * Remove a list of uris from redis.
   *
   * @param uris the list of uris to be removed
   * @throws CacheStatusException if the cache is not fully created.
   */
  public void remove(List<String> uris) throws CacheStatusException {
    verifyCacheReady();
    try (Jedis jedis = redisProvider.getJedis()) {
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
    }
  }

  private void removeKeysForEntity(Jedis jedis, String str, String cachedEntity) {
    Set<String> conceptKeys = jedis.keys(cachedEntity + CACHED_ENTITY_WILDCARD);
    for (String key : conceptKeys) {
      jedis.srem(key, str);
    }
  }

  /**
   * Restarts the population of Mongo to Redis except when the cache is ready and not marked for
   * redirect.
   */
  public void recreateCache() {

    // Verify the state
    if (getCurrentStatus() == CacheStatus.FINISHED) {
      LOGGER.info("Cache recreation skipped: cache does not require recreate.");
      return;
    }

    // Empty the cache.
    LOGGER.info("Emptying cache");
    try (Jedis jedis = redisProvider.getJedis()) {
      performAction(jedis, Jedis::flushAll);
    } finally {
      setCurrentStatus(CacheStatus.NONE);
    }

    // Populate the cache. This can take some time.
    LOGGER.info("Populating cache");
    long startTime = System.currentTimeMillis();
    try {
      setCurrentStatus(CacheStatus.STARTED);
      for (EntityInfo<?, ?> type : ENTITY_TYPES) {
        loadEntities(type);
      }
      setCurrentStatus(CacheStatus.FINISHED);
    } catch (RuntimeException e) {
      setCurrentStatus(CacheStatus.NONE);
      throw e;
    }

    // Finalize: perform logging.
    int totalSeconds = (int) ((System.currentTimeMillis() - startTime) / MILLISECONDS_PER_SECOND);
    int seconds = totalSeconds % SECONDS_PER_MINUTE;
    int minutes = (totalSeconds - seconds) / SECONDS_PER_MINUTE;
    LOGGER.info("Time spent in populating Redis. minutes: {}, seconds: {}", minutes, seconds);
  }

  private void loadEntities(EntityInfo<?, ?> entityInfo) {
    try (Jedis jedis = redisProvider.getJedis()) {
      List<MongoTerm> terms = entityDao.getAllMongoTerms(entityInfo.entityType);
      int termCount = terms.size();
      LOGGER.info("Found entities of type {}: {}", entityInfo.entityType, termCount);
      int i = 0;
      for (MongoTerm term : terms) {
        loadEntity(entityInfo, term, jedis);
        i++;
        if (i % COUNT_OF_ITEMS_COLLECTED_TO_LOG == 0) {
          LOGGER.info("Elements added: {} out of: {}", i, termCount);
        }
      }
    }
  }

  private void loadEntity(EntityInfo<?, ?> entityInfo, MongoTerm term, Jedis jedis) {
    MongoTermList<?> termList = entityDao
        .findTermListByField(entityInfo.mongoTermListClass, EntityDao.CODE_URI_FIELD,
            term.getCodeUri());
    if (termList != null) {
      loadEntities(entityInfo, term, jedis, termList);
    }
  }

  private void loadEntities(EntityInfo<?, ?> entityInfo, MongoTerm term, Jedis jedis,
      MongoTermList<?> termList) {
    try {
      EntityWrapper entityWrapper = new EntityWrapper();
      entityWrapper.setOriginalField("");
      entityWrapper.setEntityType(entityInfo.entityType);
      entityWrapper.setContextualEntity(
          this.getObjectMapper().writeValueAsString(termList.getRepresentation()));
      entityWrapper.setOriginalValue(term.getOriginalLabel());
      entityWrapper.setUrl(term.getCodeUri());
      jedis.sadd(entityInfo.cachedEntityPrefix + CACHED_ENTITY_DEF + term.getLabel(),
          term.getCodeUri());
      if (term.getLang() != null) {
        jedis.sadd(entityInfo.cachedEntityPrefix + CACHED_ENTITY + term.getLang() +
            CACHE_NAME_SEPARATOR + term.getLabel(), term.getCodeUri());
      }
      jedis.hset(entityInfo.cachedEntityPrefix + CACHED_URI, term.getCodeUri(),
          OBJECT_MAPPER.writeValueAsString(entityWrapper));
      Set<String> parentCodeUris = this.findParentCodeUris(termList, entityInfo);
      if (!parentCodeUris.isEmpty()) {
        jedis.sadd(entityInfo.cachedEntityPrefix + CACHED_PARENT + term.getCodeUri(),
            parentCodeUris.toArray(new String[]{}));
      }
      if (termList.getOwlSameAs() != null) {
        for (String sameAs : termList.getOwlSameAs()) {
          jedis.hset(entityInfo.cachedEntityPrefix + CACHED_SAMEAS, sameAs, term.getCodeUri());
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
   * @throws CacheStatusException if the cache is not fully created.
   * @throws IOException if the cache could not be accessed.
   */
  protected List<EntityWrapper> tag(List<InputValue> values)
      throws IOException, CacheStatusException {
    verifyCacheReady();
    List<EntityWrapper> entities = new ArrayList<>();
    for (InputValue inputValue : values) {
      if (inputValue.getVocabularies() == null) {
        continue;
      }
      for (EntityType voc : inputValue.getVocabularies()) {
        entities.addAll(findEntities(inputValue.getValue().toLowerCase(Locale.US),
            inputValue.getOriginalField(), inputValue.getLanguage(), voc));
      }
    }
    return entities;
  }

  private Set<String> findParentCodeUris(MongoTermList<?> termList, EntityInfo<?, ?> entityInfo) {
    final Set<String> parentEntities = new HashSet<>();
    MongoTermList<?> currentMongoTermList = termList;
    while (StringUtils.isNotBlank(currentMongoTermList.getParent())) {
      currentMongoTermList = entityDao.findTermListByField(entityInfo.mongoTermListClass,
          EntityDao.CODE_URI_FIELD, currentMongoTermList.getParent());
      //Break when there is no other parent available or when we have already encountered the codeUri
      if (currentMongoTermList == null || !parentEntities.add(currentMongoTermList.getCodeUri())) {
        break;
      }
    }
    return parentEntities;
  }

  private List<EntityWrapper> findEntities(String value, String field, String lang,
      EntityType entityType) throws IOException {
    final String cachedEntityPrefix;
    switch (entityType) {
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
        throw new IllegalStateException("Unknown entity class: " + entityType.name());
    }
    return findEntities(value, field, lang, cachedEntityPrefix);
  }

  private List<EntityWrapper> findEntities(String value, String originalField, String lang,
      String cachedEntityPrefix) throws IOException {

    final String correctedLang;
    if (StringUtils.isEmpty(lang) || lang.length() != LANGUAGE_TAG_LENGTH) {
      correctedLang = "def";
    } else {
      correctedLang = lang;
    }

    final Set<EntityWrapper> result = new HashSet<>();
    final ThrowingConsumer<Jedis, IOException> action = jedis -> {
      if (!jedis.isConnected()) {
        jedis.connect();
      }
      final String cacheKey =
              cachedEntityPrefix + CACHED_ENTITY + correctedLang + CACHE_NAME_SEPARATOR + value;
      if (Boolean.TRUE.equals(jedis.exists(cacheKey))) {
        Set<String> urisToCheck = jedis.smembers(cacheKey);
        for (String uri : urisToCheck) {
          EntityWrapper entity = OBJECT_MAPPER
                  .readValue(jedis.hget(cachedEntityPrefix + CACHED_URI, uri),
                          EntityWrapper.class);
          entity.setOriginalField(originalField);
          result.add(entity);
          result.addAll(findParentEntities(cachedEntityPrefix, jedis, uri));
        }
      }
    };

    try (Jedis jedis = redisProvider.getJedis()) {
      performThrowingAction(jedis, action);
    }
    return new ArrayList<>(result);
  }

  private Set<EntityWrapper> findParentEntities(String cachedEntityPrefix, Jedis jedis,
      String uri) throws IOException {
    Set<EntityWrapper> entityWrapperSet = new HashSet<>();
    if (Boolean.TRUE.equals(jedis.exists(cachedEntityPrefix + CACHED_PARENT + uri))) {
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
   * @throws CacheStatusException if the cache is not fully created.
   * @throws IOException if the cache could not be accessed.
   */
  public EntityWrapper getById(String requestedId) throws IOException, CacheStatusException {
    verifyCacheReady();

    // Get the result providers - this is constant and does not depend on the input.
    final List<GetterByUri> getters = Arrays.asList(
        (uri, jedis) -> getEntityWrapperFromCodeUri(uri, jedis, CACHED_AGENT),
        (uri, jedis) -> getEntityWrapperFromCodeUri(uri, jedis, CACHED_CONCEPT),
        (uri, jedis) -> getEntityWrapperFromCodeUri(uri, jedis, CACHED_TIMESPAN),
        (uri, jedis) -> getEntityWrapperFromCodeUri(uri, jedis, CACHED_PLACE)
    );

    // Get the first non-null result.
    return getFirstMatch(getters, requestedId);
  }

  /**
   * Get an enrichment document based on the uri provided.
   *
   * @param requestedUri the provided uri
   * @return the enrichment document corresponding to the provided uri
   * @throws CacheStatusException if the cache is not fully created.
   * @throws IOException if the cache could not be accessed.
   */
  public EntityWrapper getByUri(String requestedUri) throws IOException, CacheStatusException {
    verifyCacheReady();

    // Get the result providers - this is constant and does not depend on the input.
    final List<GetterByUri> getters = Arrays.asList(
        (uri, jedis) -> getEntityWrapperFromCodeUri(uri, jedis, CACHED_AGENT),
        (uri, jedis) -> getEntityWrapperFromCodeUri(uri, jedis, CACHED_CONCEPT),
        (uri, jedis) -> getEntityWrapperFromCodeUri(uri, jedis, CACHED_TIMESPAN),
        (uri, jedis) -> getEntityWrapperFromCodeUri(uri, jedis, CACHED_PLACE),
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
    if (Boolean.TRUE.equals(jedis.hexists(cachedEntity + CACHED_SAMEAS, uri))) {
      return OBJECT_MAPPER.readValue(jedis.hget(cachedEntity + CACHED_URI,
          jedis.hget(cachedEntity + CACHED_SAMEAS, uri)), EntityWrapper.class);
    }
    return null;
  }

  private EntityWrapper getEntityWrapperFromCodeUri(String uri, Jedis jedis, String cachedEntity)
      throws IOException {
    if (Boolean.TRUE.equals(jedis.hexists(cachedEntity + CACHED_URI, uri))) {
      return OBJECT_MAPPER
          .readValue(jedis.hget(cachedEntity + CACHED_URI, uri), EntityWrapper.class);
    }
    return null;
  }

  private ObjectMapper getObjectMapper() {
    return OBJECT_MAPPER;
  }

  private static class EntityInfo<T extends MongoTermList<S>, S extends AbstractEdmEntityImpl> {

    protected final EntityType entityType;
    protected final Class<T> mongoTermListClass;
    protected final String cachedEntityPrefix;

    EntityInfo(EntityType entityType, Class<T> mongoTermListClass, String cachedEntityPrefix) {
      this.entityType = entityType;
      this.mongoTermListClass = mongoTermListClass;
      this.cachedEntityPrefix = cachedEntityPrefix;
    }
  }
}
