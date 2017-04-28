/*
a * Copyright 2005-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.europeana.enrichment.service;


import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.ObjectIdSerializer;
import eu.europeana.enrichment.api.internal.AgentTermList;
import eu.europeana.enrichment.api.internal.ConceptTermList;
import eu.europeana.enrichment.api.internal.MongoTerm;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.api.internal.PlaceTermList;
import eu.europeana.enrichment.api.internal.TimespanTermList;
import eu.europeana.enrichment.utils.MongoDatabaseUtils;
import eu.europeana.metis.cache.redis.RedisProvider;
import eu.europeana.metis.utils.EntityClass;
import eu.europeana.metis.utils.InputValue;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.annotation.PreDestroy;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.Version;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.module.SimpleModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;


/**
 * Main enrichment class
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
@SuppressWarnings("rawtypes")
@Component
public class RedisInternalEnricher {

  private final Logger LOGGER = LoggerFactory.getLogger(RedisInternalEnricher.class);

  private final static ObjectMapper obj = new ObjectMapper();

  private Jedis jedis;
  private String mongoHost;

  public RedisInternalEnricher(String mongoHost, RedisProvider provider, boolean populate) {
    this.mongoHost = mongoHost;
    SimpleModule sm = new SimpleModule("test", Version.unknownVersion());
    sm.addSerializer(new ObjectIdSerializer());
    obj.registerModule(sm);
    jedis = provider.getJedis();
    if (populate) {
      if (!jedis.exists("enrichmentstatus") || (
          !StringUtils.equals(jedis.get("enrichmentstatus"), "started") && !StringUtils
              .equals(jedis.get("enrichmentstatus"), "finished"))) {
        LOGGER.info(
            "Redis status 'enrichmentstatus' does not exist or is not in a 'started' or 'finished' state.");
        LOGGER.info("Re-populating Redis from Mongo");
        populate();
      } else {
        LOGGER.info("Status 'enrichmentstatus' exists with value: " + check());
      }
    }
  }

  public String check() {
    return jedis.get("enrichmentstatus");
  }

  public void recreate() {
    LOGGER.info("Recreate triggered.");
    jedis.del("enrichmentstatus");
    populate();
  }

  public void remove(List<String> uris) {
    for (String str : uris) {
      jedis.del("concept:parent:" + str);
      jedis.del("agent:parent:" + str);
      jedis.del("timespan:parent:" + str);
      jedis.del("place:parent:" + str);
      jedis.hdel("concept:uri", str);
      jedis.hdel("agent:uri", str);
      jedis.hdel("timespan:uri", str);
      jedis.hdel("place:uri", str);
      Set<String> conceptKeys = jedis.keys("concept:entity:*");
      for (String key : conceptKeys) {
        jedis.srem(key, str);
      }
      Set<String> agentKeys = jedis.keys("agent:entity:*");
      for (String key : agentKeys) {
        jedis.srem(key, str);
      }
      Set<String> placeKeys = jedis.keys("place:entity:*");
      for (String key : placeKeys) {
        jedis.srem(key, str);
      }
      Set<String> timespanKeys = jedis.keys("timespan:entity:*");
      for (String key : timespanKeys) {
        jedis.srem(key, str);
      }

    }
  }

  public void populate() {

    if (!jedis.isConnected()) {
      jedis.connect();
    }
    jedis.set("enrichmentstatus", "started");
    LOGGER.info("Initializing population of Redis from Mongo.");
    MongoDatabaseUtils.dbExists(mongoHost, 27017);
    List<MongoTerm> agentsMongo = MongoDatabaseUtils.getAllAgents();
    LOGGER.info("Found agents: " + agentsMongo.size());
    int i = 0;
    for (MongoTerm agent : agentsMongo) {
      try {
        AgentTermList atl = (AgentTermList) MongoDatabaseUtils
            .findByCode(agent.getCodeUri(), "people");
        if (atl != null) {
          try {
            EntityWrapper ag = new EntityWrapper();
            ag.setOriginalField("");
            ag.setClassName(AgentImpl.class.getName());
            ag.setContextualEntity(
                this.getObjectMapper().writeValueAsString(atl.getRepresentation()));
            ag.setUrl(agent.getCodeUri());
            ag.setOriginalValue(agent.getOriginalLabel());
            if (!jedis.isConnected()) {
              jedis.connect();
            }
            jedis.sadd("agent:entity:def:" + agent.getLabel(), agent.getCodeUri());
            if (agent.getLang() != null) {
              jedis.sadd("agent:entity:" + agent.getLang() + ":" + agent.getLabel(),
                  agent.getCodeUri());
            }
            jedis.hset("agent:uri", agent.getCodeUri(), obj.writeValueAsString(ag));
            List<String> parents = this.findAgentParents(atl.getParent());
            if (parents != null && parents.size() > 0) {
              jedis.sadd("agent:parent:" + agent.getCodeUri(), parents.toArray(new String[]{}));
            }
            if (atl.getOwlSameAs() != null) {
              for (String sameAs : atl.getOwlSameAs()) {
                jedis.hset("agent:sameas", sameAs, agent.getCodeUri());
              }
            }
          } catch (IOException var14) {
            var14.printStackTrace();
          }
        }
        if (i % 100 == 0) {
          LOGGER.info("Agents added: ", i);
        }
        i++;
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }

    }

    List<MongoTerm> conceptsMongo1 = MongoDatabaseUtils.getAllConcepts();
    LOGGER.info("Found concepts: " + conceptsMongo1.size());
    i = 0;
    for (MongoTerm concept : conceptsMongo1) {
      try {
        ConceptTermList ctl = (ConceptTermList) MongoDatabaseUtils
            .findByCode(concept.getCodeUri(), "concept");
        if (ctl != null) {
          try {
            EntityWrapper i$ = new EntityWrapper();
            i$.setOriginalField("");
            i$.setClassName(ConceptImpl.class.getName());
            i$.setContextualEntity(
                this.getObjectMapper().writeValueAsString(ctl.getRepresentation()));
            i$.setUrl(concept.getCodeUri());
            i$.setOriginalValue(concept.getOriginalLabel());
            if (!jedis.isConnected()) {
              jedis.connect();
            }
            jedis.sadd("concept:entity:def:" + concept.getLabel(), concept.getCodeUri());
            if (concept.getLang() != null) {
              jedis.sadd("concept:entity:" + concept.getLang() + ":" + concept.getLabel(),
                  concept.getCodeUri());
            }
            jedis.hset("concept:uri", concept.getCodeUri(), obj.writeValueAsString(i$));
            List<String> parents = this.findConceptParents(ctl.getParent());
            if (parents != null && parents.size() > 0) {
              jedis.sadd("concept:parent:" + concept.getCodeUri(), parents.toArray(new String[]{}));
            }
            if (ctl.getOwlSameAs() != null) {
              for (String sameAs : ctl.getOwlSameAs()) {
                jedis.hset("concept:sameas", sameAs, concept.getCodeUri());
              }
            }
          } catch (IOException var12) {
            var12.printStackTrace();
          }
        }
        if (i % 100 == 0) {
          LOGGER.info("Concepts added: ", i);
        }
        i++;
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }

    }

    List<MongoTerm> placesMongo2 = MongoDatabaseUtils.getAllPlaces();
    LOGGER.info("Found places: " + placesMongo2.size());
    i = 0;
    for (MongoTerm place : placesMongo2) {
      try {
        PlaceTermList ptl = (PlaceTermList) MongoDatabaseUtils
            .findByCode(place.getCodeUri(), "place");
        if (ptl != null) {
          try {
            EntityWrapper entry = new EntityWrapper();
            entry.setOriginalField("");
            entry.setClassName(PlaceImpl.class.getName());
            entry.setContextualEntity(
                this.getObjectMapper().writeValueAsString(ptl.getRepresentation()));
            entry.setUrl(place.getCodeUri());
            entry.setOriginalValue(place.getOriginalLabel());
            if (!jedis.isConnected()) {
              jedis.connect();
            }
            jedis.sadd("place:entity:def:" + place.getLabel(), place.getCodeUri());
            if (place.getLang() != null) {
              jedis.sadd("place:entity:" + place.getLang() + ":" + place.getLabel(),
                  place.getCodeUri());
            }
            jedis.hset("place:uri", place.getCodeUri(), obj.writeValueAsString(entry));
            List<String> parents = this.findPlaceParents(ptl.getParent());
            if (parents != null && parents.size() > 0) {
              jedis.sadd("place:parent:" + place.getCodeUri(), parents.toArray(new String[]{}));
            }
            if (ptl.getOwlSameAs() != null) {
              for (String sameAs : ptl.getOwlSameAs()) {
                jedis.hset("place:sameas", sameAs, place.getCodeUri());
              }
            }
          } catch (IOException var10) {
            var10.printStackTrace();
          }
        }
        if (i % 100 == 0) {
          LOGGER.info("Places added: ", i);
        }
        i++;
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }

    List<MongoTerm> timespanMongo3 = MongoDatabaseUtils.getAllTimespans();
    LOGGER.info("Found timespans: " + timespanMongo3.size());
    i = 0;
    for (MongoTerm timespan : timespanMongo3) {
      try {
        TimespanTermList tsl = (TimespanTermList) MongoDatabaseUtils
            .findByCode(timespan.getCodeUri(), "period");
        if (tsl != null) {
          try {
            EntityWrapper ex = new EntityWrapper();
            ex.setOriginalField("");
            ex.setClassName(TimespanImpl.class.getName());
            ex.setContextualEntity(
                this.getObjectMapper().writeValueAsString(tsl.getRepresentation()));
            ex.setOriginalValue(timespan.getOriginalLabel());
            ex.setUrl(timespan.getCodeUri());
            if (!jedis.isConnected()) {
              jedis.connect();
            }
            jedis.sadd("timespan:entity:def:" + timespan.getLabel(), timespan.getCodeUri());
            if (timespan.getLang() != null) {
              jedis.sadd("timespan:entity:" + timespan.getLang() + ":" + timespan.getLabel(),
                  timespan.getCodeUri());
            }
            jedis.hset("timespan:uri", timespan.getCodeUri(), obj.writeValueAsString(ex));
            List<String> parents = this.findTimespanParents(tsl.getParent());
            if (parents != null && parents.size() > 0) {
              jedis.sadd("timespan:parent:" + timespan.getCodeUri(),
                  parents.toArray(new String[]{}));
            }
            if (tsl.getOwlSameAs() != null) {
              for (String sameAs : tsl.getOwlSameAs()) {
                jedis.hset("timespan:sameas", sameAs, timespan.getCodeUri());
              }
            }
          } catch (IOException var8) {
            var8.printStackTrace();
          }
        }
        if (i % 100 == 0) {
          LOGGER.info("Timespans added: ", i);
        }
        i++;
      } catch (MalformedURLException e) {
        e.printStackTrace();
      }
    }
    jedis.set("enrichmentstatus", "finished");
  }


  /**
   * The internal enrichment functionality not to be exposed yet as there is a
   * strong dependency to the external resources to recreate the DB The
   * enrichment is performed by lowercasing every value so that searchability
   * in the DB is enhanced, but the Capitalized version is always retrieved
   *
   * @param values The values to enrich
   * @return A list of enrichments
   */
  protected List<? extends EntityWrapper> tag(List<InputValue> values)
      throws JsonGenerationException, JsonMappingException, IOException {

    List<EntityWrapper> entities = new ArrayList<EntityWrapper>();
    for (InputValue inputValue : values) {
      for (EntityClass voc : inputValue.getVocabularies()) {
        entities.addAll(findEntities(inputValue.getValue()
            .toLowerCase(), inputValue.getOriginalField(), voc, inputValue.getLanguage()));
      }
    }
    return entities;
  }

  private List<? extends EntityWrapper> findEntities(String lowerCase,
      String field, EntityClass className, String lang)
      throws JsonGenerationException, JsonMappingException, IOException {
    List<EntityWrapper> entities = new ArrayList<EntityWrapper>();
    switch (className) {
      case AGENT:
        entities.addAll(findAgentEntities(lowerCase, field, lang));
        break;
      case CONCEPT:
        entities.addAll(findConceptEntities(lowerCase, field, lang));
        break;
      case PLACE:
        entities.addAll(findPlaceEntities(lowerCase, field, lang));
        break;
      case TIMESPAN:
        entities.addAll(findTimespanEntities(lowerCase, field, lang));
      default:
        break;
    }
    return entities;
  }

  private List<EntityWrapper> findConceptEntities(String value,
      String originalField, String lang) throws JsonGenerationException,
      JsonMappingException, IOException {
    Set<EntityWrapper> concepts = new HashSet<>();

    if (StringUtils.isEmpty(lang) || lang.length() != 2) {
      lang = "def";
    }
    if (!jedis.isConnected()) {
      jedis.connect();
    }
    if (jedis.exists("concept:entity:" + lang + ":" + value)) {
      Set<String> urisToCheck = jedis.smembers("concept:entity:" + lang + ":" + value);
      for (String uri : urisToCheck) {
        EntityWrapper entity = obj.readValue(jedis.hget("concept:uri", uri), EntityWrapper.class);
        entity.setOriginalField(originalField);
        concepts.add(entity);
        if (jedis.exists("concept:parent:" + uri)) {
          Set<String> parents = jedis.smembers("concept:parent:" + uri);
          for (String parent : parents) {
            EntityWrapper parentEntity = obj
                .readValue(jedis.hget("concept:uri", parent), EntityWrapper.class);
            concepts.add(parentEntity);
          }
        }

      }
    }
    List<EntityWrapper> list = new ArrayList<>();
    list.addAll(concepts);
    return list;
  }

  private List<String> findConceptParents(String parent)
      throws JsonGenerationException, JsonMappingException, IOException {
    ArrayList parentEntities = new ArrayList();
    MongoTermList parents = MongoDatabaseUtils.findByCode(parent, "concept");
    if (parents != null) {
      parentEntities.add(parents.getCodeUri());
      if (parents.getParent() != null && !parent.equals(parents.getParent())) {
        parentEntities.addAll(this.findConceptParents(parents.getParent()));
      }
    }

    return parentEntities;
  }

  private List<? extends EntityWrapper> findAgentEntities(String value,
      String originalField, String lang) throws JsonGenerationException,
      JsonMappingException, IOException {
    Set agents = new HashSet<>();
    if (StringUtils.isEmpty(lang) || lang.length() != 2) {
      lang = "def";
    }
    if (!jedis.isConnected()) {
      jedis.connect();
    }
    if (jedis.exists("agent:entity:" + lang + ":" + value)) {
      Set<String> urisToCheck = jedis.smembers("agent:entity:" + lang + ":" + value);
      for (String uri : urisToCheck) {
        EntityWrapper entity = obj.readValue(jedis.hget("agent:uri", uri), EntityWrapper.class);
        entity.setOriginalField(originalField);
        agents.add(entity);
        if (jedis.exists("agent:parent:" + uri)) {
          Set<String> parents = jedis.smembers("agent:parent:" + uri);
          for (String parent : parents) {
            EntityWrapper parentEntity = obj
                .readValue(jedis.hget("agent:uri", parent), EntityWrapper.class);
            agents.add(parentEntity);
          }
        }

      }
    }

    List list = new ArrayList<>();
    list.addAll(agents);
    return list;
  }

  private List<String> findAgentParents(String parent)
      throws JsonGenerationException, JsonMappingException, IOException {
    ArrayList parentEntities = new ArrayList();
    MongoTermList parents = MongoDatabaseUtils.findByCode(parent, "people");
    if (parents != null) {
      parentEntities.add(parents.getCodeUri());
      if (parents.getParent() != null && !parent.equals(parents.getParent())) {
        parentEntities.addAll(this.findAgentParents(parents.getParent()));
      }
    }

    return parentEntities;
  }

  private List<? extends EntityWrapper> findPlaceEntities(String value,
      String originalField, String lang) throws JsonGenerationException,
      JsonMappingException, IOException {
    Set places = new HashSet<>();
    if (StringUtils.isEmpty(lang) || lang.length() != 2) {
      lang = "def";
    }
    if (!jedis.isConnected()) {
      jedis.connect();
    }
    if (jedis.exists("place:entity:" + lang + ":" + value)) {
      Set<String> urisToCheck = jedis.smembers("place:entity:" + lang + ":" + value);
      for (String uri : urisToCheck) {
        EntityWrapper entity = obj.readValue(jedis.hget("place:uri", uri), EntityWrapper.class);
        entity.setOriginalField(originalField);
        places.add(entity);
        if (jedis.exists("place:parent:" + uri)) {
          Set<String> parents = jedis.smembers("place:parent:" + uri);
          for (String parent : parents) {
            EntityWrapper parentEntity = obj
                .readValue(jedis.hget("place:uri", parent), EntityWrapper.class);
            places.add(parentEntity);
          }
        }

      }
    }

    List list = new ArrayList<>();
    list.addAll(places);
    return list;
  }

  private List<String> findPlaceParents(String parent)
      throws JsonGenerationException, JsonMappingException, IOException {
    ArrayList parentEntities = new ArrayList();
    MongoTermList parents = MongoDatabaseUtils.findByCode(parent, "place");
    if (parents != null) {
      parentEntities.add(parents.getCodeUri());
      if (parents.getParent() != null && !parent.equals(parents.getParent())) {
        parentEntities.addAll(this.findPlaceParents(parents.getParent()));
      }
    }

    return parentEntities;
  }


  private List<? extends EntityWrapper> findTimespanEntities(String value,
      String originalField, String lang) throws JsonGenerationException,
      JsonMappingException, IOException {
    Set timespans = new HashSet<>();
    if (StringUtils.isEmpty(lang) || lang.length() != 2) {
      lang = "def";
    }
    if (!jedis.isConnected()) {
      jedis.connect();
    }
    if (jedis.exists("timespan:entity:" + lang + ":" + value)) {
      Set<String> urisToCheck = jedis.smembers("timespan:entity:" + lang + ":" + value);
      for (String uri : urisToCheck) {
        EntityWrapper entity = obj.readValue(jedis.hget("timespan:uri", uri), EntityWrapper.class);
        entity.setOriginalField(originalField);
        timespans.add(entity);
        if (jedis.exists("timespan:parent:" + uri)) {
          Set<String> parents = jedis.smembers("timespan:parent:" + uri);
          for (String parent : parents) {
            EntityWrapper parentEntity = obj
                .readValue(jedis.hget("timespan:uri", parent), EntityWrapper.class);
            timespans.add(parentEntity);
          }
        }

      }
    }

    List list = new ArrayList<>();
    list.addAll(timespans);
    return list;
  }

  private List<String> findTimespanParents(String parent)
      throws JsonGenerationException, JsonMappingException, IOException {
    ArrayList parentEntities = new ArrayList();
    MongoTermList parents = MongoDatabaseUtils.findByCode(parent, "period");
    if (parents != null) {
      parentEntities.add(parents.getCodeUri());
      if (parents.getParent() != null && !parent.equals(parents.getParent())) {
        try {
          Thread.sleep(10L);
        } catch (InterruptedException var5) {
          LOGGER.error(null, var5);
        }

        parentEntities.addAll(this.findTimespanParents(parents.getParent()));
      }
    }

    return parentEntities;
  }

  public EntityWrapper getByUri(String uri) throws IOException {
    if (!jedis.isConnected()) {
      jedis.connect();
    }
    if (jedis.hexists("agent:uri", uri)) {
      return obj.readValue(jedis.hget("agent:uri", uri), EntityWrapper.class);
    }
    if (jedis.hexists("concept:uri", uri)) {
      return obj.readValue(jedis.hget("concept:uri", uri), EntityWrapper.class);
    }
    if (jedis.hexists("timespan:uri", uri)) {
      return obj.readValue(jedis.hget("timespan:uri", uri), EntityWrapper.class);
    }
    if (jedis.hexists("place:uri", uri)) {
      return obj.readValue(jedis.hget("place:uri", uri), EntityWrapper.class);
    }

    if (jedis.hexists("agent:sameas", uri)) {
      return obj
          .readValue(jedis.hget("agent:uri", jedis.hget("agent:sameas", uri)), EntityWrapper.class);
    }
    if (jedis.hexists("concept:sameas", uri)) {
      return obj.readValue(jedis.hget("concept:uri", jedis.hget("concept:sameas", uri)),
          EntityWrapper.class);
    }
    if (jedis.hexists("timespan:sameas", uri)) {
      return obj.readValue(jedis.hget("timespan:uri", jedis.hget("timespan:sameas", uri)),
          EntityWrapper.class);
    }
    if (jedis.hexists("place:sameas", uri)) {
      return obj
          .readValue(jedis.hget("place:uri", jedis.hget("place:sameas", uri)), EntityWrapper.class);
    }

    return null;
  }

  private ObjectMapper getObjectMapper() {
    return obj;
  }

  @PreDestroy
  public void close() {
    if (jedis != null) {
      jedis.close();
    }
  }


}
