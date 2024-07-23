package eu.europeana.metis.dereference.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import eu.europeana.enrichment.api.external.DereferenceResultStatus;
import eu.europeana.metis.dereference.DereferenceResult;
import eu.europeana.metis.dereference.ProcessedEntity;
import eu.europeana.metis.dereference.RdfRetriever;
import eu.europeana.metis.dereference.Vocabulary;
import eu.europeana.metis.dereference.service.dao.ProcessedEntityDao;
import eu.europeana.metis.dereference.service.dao.VocabularyDao;
import jakarta.xml.bind.JAXBException;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.apache.commons.io.IOUtils;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.stubbing.Answer;

/**
 * Unit tests for {@link MongoDereferenceService}
 */
class MongoDereferenceServiceTest {

  private static final VocabularyDao vocabularyDao = mock(VocabularyDao.class);
  private static final ProcessedEntityDao processedEntityDao = mock(ProcessedEntityDao.class);
  private final RdfRetriever retriever = mock(RdfRetriever.class);
  private final MongoDereferenceService dereferenceService =
      spy(new MongoDereferenceService(retriever, processedEntityDao, vocabularyDao));

  private static final String GEONAMES_URI = "http://sws.geonames.org/";
  private static Vocabulary GEONAMES;
  private static final String PLACE_ID = "http://sws.geonames.org/3020251/";
  private static String PLACE_SOURCE_ENTITY;

  private static final Map<String, ProcessedEntity> CACHE = new HashMap<>();

  @BeforeAll
  static void prepareData() throws IOException {

    // Create the vocabulary
    GEONAMES = new Vocabulary();
    GEONAMES.setId(new ObjectId(new Date()));
    GEONAMES.setUris(Collections.singleton(GEONAMES_URI));
    GEONAMES.setXslt(IOUtils.toString(Objects.requireNonNull(MongoDereferenceServiceTest.class
        .getClassLoader().getResourceAsStream("geonames.xsl")), StandardCharsets.UTF_8));
    GEONAMES.setName("Geonames");
    GEONAMES.setIterations(0);

    // Create the place
    PLACE_SOURCE_ENTITY = IOUtils.toString(Objects.requireNonNull(MongoDereferenceServiceTest.class
        .getClassLoader().getResourceAsStream("place_entity.xsl")), StandardCharsets.UTF_8);
  }

  @BeforeEach
  void resetMocks() throws IOException, URISyntaxException, JAXBException {

    // Reset the mocks
    reset(vocabularyDao, processedEntityDao, retriever, dereferenceService);

    // Add support for vocabulary in the mocks.
    final String searchString = new URI(GEONAMES_URI).getHost();
    doReturn(List.of(GEONAMES)).when(vocabularyDao).getByUriSearch(searchString);
    doReturn(GEONAMES).when(vocabularyDao).get(GEONAMES.getId().toString());

    // Add support for the place in the mocks.
    doReturn(PLACE_SOURCE_ENTITY).when(retriever).retrieve(eq(PLACE_ID), anyString());

    // Clear cache and build the cache functionality
    CACHE.clear();
    doAnswer((Answer<Void>) invocation -> {
      final ProcessedEntity entity = invocation.getArgument(0);
      CACHE.put(entity.getResourceId(), entity);
      return null;
    }).when(processedEntityDao).save(any());
    doAnswer((Answer<ProcessedEntity>) invocation -> CACHE.get((String) invocation.getArgument(0)))
        .when(processedEntityDao).getByResourceId(anyString());
  }

  @Test
  void testDereference_Success() throws IOException, URISyntaxException {

    // First time: no cache
    final DereferenceResult result0 = dereferenceService.dereference(PLACE_ID);
    assertNotNull(result0);
    assertEquals(1, result0.getEnrichmentBasesAsList().size());
    assertEquals(PLACE_ID, result0.getEnrichmentBasesAsList().get(0).getAbout());
    assertEquals(DereferenceResultStatus.SUCCESS, result0.getDereferenceStatus());
    verify(retriever, times(1)).retrieve(eq(PLACE_ID), anyString());
    assertTrue(CACHE.containsKey(PLACE_ID));
    // TODO check the cached item.

    // Second time: use cache, no second retrieval.
    // TODO
/*    final DereferenceResult result1 = mongoDereferenceService.dereference(PLACE_ID);
    assertNotNull(result1);
    assertEquals(1, result1.getEnrichmentBasesAsList().size());
    assertEquals(PLACE_ID, result1.getEnrichmentBasesAsList().get(0).getAbout());
    assertEquals(DereferenceResultStatus.SUCCESS, result1.getDereferenceStatus());
    verify(retriever, never()).retrieve(eq(PLACE_ID), anyString());
    assertTrue(CACHE.containsKey(PLACE_ID));*/
  }

  @Test
  void testDereference_IllegalArgument() {
    final IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
        () -> dereferenceService.dereference(null));
    assertEquals("Parameter resourceId cannot be null.", exception.getMessage());
  }

  @Test
  void testDereference_NoVocabularyMatching() throws IOException, URISyntaxException {
    final String nonExistingVocabularyEntity = "http://XXX.YYYYYYY.org/3020251/";
    final DereferenceResult emptyResult = dereferenceService.dereference(nonExistingVocabularyEntity);
    assertNotNull(emptyResult);
    assertTrue(emptyResult.getEnrichmentBasesAsList().isEmpty());
    assertEquals(DereferenceResultStatus.NO_VOCABULARY_MATCHING, emptyResult.getDereferenceStatus());
    verify(retriever, never()).retrieve(anyString(), anyString());
    assertTrue(CACHE.containsKey(nonExistingVocabularyEntity));
    // TODO check the cached item.

    // Second time: use cache, no second retrieval.
    // TODO
  }

  @Test
  void testDereference_NoEntityForVocabulary() throws IOException, URISyntaxException {

    // Without cached item
    final String nonExistingId = GEONAMES_URI + "XXXXXX";
    final DereferenceResult emptyResult = dereferenceService.dereference(nonExistingId);
    assertNotNull(emptyResult);
    assertTrue(emptyResult.getEnrichmentBasesAsList().isEmpty());
    assertEquals(DereferenceResultStatus.NO_ENTITY_FOR_VOCABULARY, emptyResult.getDereferenceStatus());
    verify(retriever, times(1)).retrieve(eq(nonExistingId), anyString());
    assertTrue(CACHE.containsKey(nonExistingId));
    // TODO check the cached item.

    // Second time: use cache, no second retrieval.
    // TODO
  }

  @Test
  void testDereference_InvalidUrl() throws IOException, URISyntaxException {

    // Entity ID: ensure that it is indeed invalid.
    final String entityId = "http://sws.geonames.org/?_)(*&^%$#@!3020251/";
    assertThrows(URISyntaxException.class, () -> new URI(entityId));

    // Try to dereference. There should not be a cached item created.
    final DereferenceResult result = dereferenceService.dereference(entityId);
    assertNotNull(result);
    assertTrue(result.getEnrichmentBasesAsList().isEmpty());
    assertEquals(DereferenceResultStatus.INVALID_URL, result.getDereferenceStatus());
    verify(retriever, never()).retrieve(anyString(), anyString());
    assertFalse(CACHE.containsKey(entityId));
  }

  @Test
  void testDereference_XmlXsltError() throws URISyntaxException, IOException {
    doReturn("THIS WILL BE AN ERROR").when(retriever).retrieve(eq(PLACE_ID), anyString());
    final DereferenceResult result = dereferenceService.dereference(PLACE_ID);
    assertNotNull(result);
    assertTrue(result.getEnrichmentBasesAsList().isEmpty());
    assertEquals(DereferenceResultStatus.ENTITY_FOUND_XML_XSLT_ERROR, result.getDereferenceStatus());
    verify(retriever, times(1)).retrieve(eq(PLACE_ID), anyString());
    assertTrue(CACHE.containsKey(PLACE_ID));
    // TODO check the cached item.

    // Second time: use cache, no second retrieval.
    // TODO
  }

  @Test
  void testDereference_XmlXsltProduceNoContextualClass() throws URISyntaxException, IOException {
    doReturn("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\"?><empty/>").when(retriever).retrieve(eq(PLACE_ID), anyString());
    final DereferenceResult result = dereferenceService.dereference(PLACE_ID);
    assertNotNull(result);
    assertTrue(result.getEnrichmentBasesAsList().isEmpty());
    assertEquals(DereferenceResultStatus.ENTITY_FOUND_XML_XSLT_PRODUCE_NO_CONTEXTUAL_CLASS, result.getDereferenceStatus());
    verify(retriever, times(1)).retrieve(eq(PLACE_ID), anyString());
    assertTrue(CACHE.containsKey(PLACE_ID));
    // TODO check the cached item.

    // Second time: use cache, no second retrieval.
    // TODO
  }
}
