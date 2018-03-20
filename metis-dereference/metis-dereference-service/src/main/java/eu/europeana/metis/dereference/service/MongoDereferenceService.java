package eu.europeana.metis.dereference.service;

import java.io.StringReader;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.transform.TransformerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.rest.client.EnrichmentClient;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.CacheDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import eu.europeana.metis.dereference.service.utils.IncomingRecordToEdmConverter;
import eu.europeana.metis.dereference.service.utils.RdfRetriever;
import eu.europeana.metis.dereference.service.utils.VocabularyMatchUtils;

/**
 * Mongo implementation of the dereference service Created by ymamakis on 2/11/16.
 */
public class MongoDereferenceService implements DereferenceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MongoDereferenceService.class);

  private final RdfRetriever retriever;
  private final CacheDao cacheDao;
  private final VocabularyDao vocabularyDao;
  private final EnrichmentClient enrichmentClient;

  /**
   * Constructor.
   * 
   * @param retriever Object that retrieves entities from their source services.
   * @param cacheDao Object that accesses the cache of processed entities.
   * @param vocabularyDao Object that accesses vocabularies.
   * @param enrichmentClient Object that accesses the enrichment service.
   */
  @Autowired
  public MongoDereferenceService(RdfRetriever retriever, CacheDao cacheDao,
      VocabularyDao vocabularyDao, EnrichmentClient enrichmentClient) {
    this.retriever = retriever;
    this.cacheDao = cacheDao;
    this.vocabularyDao = vocabularyDao;
    this.enrichmentClient = enrichmentClient;
  }

  @Override
  public EnrichmentResultList dereference(String resourceId)
      throws TransformerException, JAXBException, URISyntaxException {

    // Sanity check
    if (resourceId == null) {
      throw new IllegalArgumentException("Parameter resourceId cannot be null.");
    }

    // First try to get the entity from the entity collection.
    EnrichmentBase enrichedEntity = enrichmentClient.getByUri(resourceId);

    // Otherwise, get it from the source.
    if (enrichedEntity == null) {
      enrichedEntity = retrieveCachedEntity(resourceId);
    }

    // Prepare the result: empty if we didn't find an entity.
    final EnrichmentResultList result = new EnrichmentResultList();
    if (enrichedEntity != null) {
      result.getResult().add(enrichedEntity);
    }

    // Done.
    return result;
  }

  private EnrichmentBase retrieveCachedEntity(String resourceId)
      throws JAXBException, TransformerException, URISyntaxException {

    // Try to get it from the cache.
    final ProcessedEntity cachedEntity = cacheDao.get(resourceId);
    String entityString = cachedEntity != null ? cachedEntity.getXml() : null;

    // If it is not in the cache, get it from the source vocabulary and cache it.
    if (entityString == null) {
      entityString = retrieveTransformedEntity(resourceId);
      if (entityString != null) {
        final ProcessedEntity entityToCache = new ProcessedEntity();
        entityToCache.setXml(entityString);
        entityToCache.setURI(resourceId);
        cacheDao.save(entityToCache);
      }
    }

    // Parse the entity.
    final EnrichmentBase result;
    if (entityString != null) {
      final StringReader reader = new StringReader(entityString);
      final JAXBContext context = JAXBContext.newInstance(EnrichmentBase.class);
      result = (EnrichmentBase) context.createUnmarshaller().unmarshal(reader);
    } else {
      result = null;
    }

    // Done
    return result;
  }

  private String retrieveTransformedEntity(String resourceId)
      throws URISyntaxException, TransformerException {

    // Get the vocabularies that potentially match the given resource ID.
    final List<Vocabulary> vocabularyCandidates =
        VocabularyMatchUtils.findVocabulariesForUrl(resourceId, vocabularyDao::getByUriSearch);

    // Get the original entity given the list of vocabulary candidates
    final String originalEntity = retrieveOriginalEntity(resourceId, vocabularyCandidates);
    if (originalEntity == null) {
      return null;
    }
    
    // Try to find the vocabulary.
    final Vocabulary vocabulary;
    if (vocabularyCandidates.isEmpty()) {
      vocabulary = null;
    } else {
      vocabulary = VocabularyMatchUtils.findVocabularyForType(vocabularyCandidates, originalEntity,
          resourceId);
    }

    // Check vocabulary existence
    if (vocabulary == null) {
      LOGGER.debug("Could not find vocabulary for resource {}.", resourceId);
      return null;
    }

    // Transform the original entity.
    return new IncomingRecordToEdmConverter(vocabulary).convert(originalEntity, resourceId);
  }

  private String retrieveOriginalEntity(String resourceId, List<Vocabulary> vocabularyCandidates) {

    // Sanity check
    if (vocabularyCandidates.isEmpty()) {
      return null;
    }

    // Obtain the possible suffixes to check from the vocabulary candidates. Note that all the
    // vocabulary candidates must at this point represent the same vocabulary, so the number of
    // suffixes should be very limited (1, if all vocabularies are configured the same way).
    final Set<String> possibleSuffixes = vocabularyCandidates.stream()
        .map(vocabulary -> vocabulary.getSuffix() == null ? "" : vocabulary.getSuffix())
        .collect(Collectors.toSet());

    // Obtain the original entity
    final String originalEntity = retriever.retrieve(resourceId, possibleSuffixes);
    if (originalEntity == null) {
      LOGGER.info("No entity XML for uri {}", resourceId);
    }

    // Done
    return originalEntity;
  }
}
