//package eu.europeana.enrichment.service;
//
//import eu.europeana.enrichment.service.exception.CacheStatusException;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.List;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//import eu.europeana.enrichment.api.external.EntityWrapper;
//import eu.europeana.enrichment.utils.InputValue;
//
///**
// * Tagging (aka semantic enrichment) of records from SOLR with built-in vocabularies.
// *
// * @author Borys Omelayenko
// * @author Yorgos.Mamakis@ europeana.eu
// */
//@Service
//public class Enricher {
//
//  private static final Logger LOGGER = LoggerFactory.getLogger(Enricher.class);
//
//  private final RedisInternalEnricher redisEnricher;
//
//  /**
//   * Constructor with required parameter.
//   *
//   * @param redisEnricher the redis enricher
//   */
//  @Autowired
//  public Enricher(RedisInternalEnricher redisEnricher) {
//    this.redisEnricher = redisEnricher;
//  }
//
//  /**
//   * Main enrichment method
//   *
//   * @param values The values to enrich
//   * @return The resulting enrichment List
//   * @throws IOException if enrichment fails
//   */
//  public List<EntityWrapper> tagExternal(List<InputValue> values) throws IOException {
//    try {
//      return new ArrayList<>(redisEnricher.tag(values));
//    } catch (RuntimeException | CacheStatusException e) {
//      LOGGER.warn("Unable to retrieve entity from tag", e);
//    }
//    return Collections.emptyList();
//  }
//
//  /**
//   * Enrich a URI
//   *
//   * @param uri the URI to enrich
//   * @return the wrapped enriched information
//   */
//  public EntityWrapper getByUri(String uri) {
//    try {
//      return redisEnricher.getByUri(uri);
//    } catch (RuntimeException | IOException | CacheStatusException e) {
//      LOGGER.warn("Unable to retrieve entity from uri", e);
//    }
//    return null;
//  }
//
//  /**
//   * Enrich a Id
//   *
//   * @param id the ID to enrich
//   * @return the wrapped enriched information
//   */
//  public EntityWrapper getById(String id) {
//    try {
//      return redisEnricher.getById(id);
//    } catch (RuntimeException | IOException | CacheStatusException e) {
//      LOGGER.warn("Unable to retrieve entity from id", e);
//    }
//    return null;
//  }
//}
