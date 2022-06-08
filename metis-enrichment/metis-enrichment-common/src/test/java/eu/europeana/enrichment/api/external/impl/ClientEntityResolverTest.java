package eu.europeana.enrichment.api.external.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.core.JsonProcessingException;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.api.internal.SearchTermImpl;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.entity.client.web.EntityClientApi;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Place;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ClientEntityResolverTest {

  private static ClientEntityResolver clientEntityResolver;
  private static EntityClientApi entityClientApi;
  private static final String CHILD_URI = "http://data.europeana.eu/place/84838";
  private static final String PARENT_URI = "http://data.europeana.eu/place/87";
  private static final String CHILD_SAME_AS_URI = "http://viaf.org/viaf/237254468";

  static class EntitiesAndExpectedEnrichmentBases {

    private final List<LinkedList<Entity>> entitiesWithParents;
    private final boolean isChildEuropeanaEntity;
    private final String expectedConvertedLanguage;

    private final int enrichmentBasesExpectedResults;

    public EntitiesAndExpectedEnrichmentBases(boolean isChildEuropeanaEntity, String expectedConvertedLanguage,
        List<LinkedList<Entity>> entitiesWithParents, int enrichmentBasesExpectedResults) {
      this.expectedConvertedLanguage = expectedConvertedLanguage;
      this.entitiesWithParents = entitiesWithParents;
      this.isChildEuropeanaEntity = isChildEuropeanaEntity;
      this.enrichmentBasesExpectedResults = enrichmentBasesExpectedResults;
    }

    public String getExpectedConvertedLanguage() {
      return expectedConvertedLanguage;
    }

    public List<LinkedList<Entity>> getEntitiesWithParents() {
      return entitiesWithParents;
    }

    public int getEnrichmentBasesExpectedResults() {
      return enrichmentBasesExpectedResults;
    }
  }

  @BeforeAll
  static void prepare() {
    entityClientApi = mock(EntityClientApi.class);
    clientEntityResolver = new ClientEntityResolver(entityClientApi, 10);
  }

  @AfterEach
  void tearDown() {
    reset(entityClientApi);
  }

  @Test
  void resolveByText_singleEntity() throws JsonProcessingException {
    SearchTermImpl searchTerm = new SearchTermImpl("Greece", "en");
    String expectedConvertedLanguage = "en";
    Entity placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    int enrichmentBasesExpectedResults = 1;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByText("Return of one entity without parents", Map.of(searchTerm,
        new EntitiesAndExpectedEnrichmentBases(true, expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByText_parentEntity() throws JsonProcessingException {
    SearchTerm searchTerm = new SearchTermImpl("Crete", "en");
    String expectedConvertedLanguage = "en";
    Entity placeEntity = new Place();
    placeEntity.setEntityId(CHILD_URI);
    placeEntity.setIsPartOfArray(List.of(PARENT_URI));
    Entity parentPlaceEntity = new Place();
    parentPlaceEntity.setEntityId(PARENT_URI);
    int enrichmentBasesExpectedResults = 2;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    entityWithParents.add(parentPlaceEntity);
    resolveByText("Return one entity and one parent",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));

    searchTerm = new SearchTermImpl("Crete", "en");
    expectedConvertedLanguage = "en";
    placeEntity = new Place();
    placeEntity.setEntityId(CHILD_URI);
    placeEntity.setIsPartOfArray(List.of(PARENT_URI));
    parentPlaceEntity = new Place();
    parentPlaceEntity.setEntityId(PARENT_URI);
    parentPlaceEntity.setIsPartOfArray(List.of(CHILD_URI));
    enrichmentBasesExpectedResults = 2;
    entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    entityWithParents.add(parentPlaceEntity);
    resolveByText("Return one entity and one parent(Circular parents do not cause endless loop)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByText_language() throws JsonProcessingException {
    SearchTermImpl searchTerm = new SearchTermImpl("Greece", "eng");
    String expectedConvertedLanguage = "en";
    Entity placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    int enrichmentBasesExpectedResults = 1;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByText("Return of one entity without parents(3 letter language)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));

    searchTerm = new SearchTermImpl("Greece", "invalidLanguage");
    resolveByText("Return of one entity without parents(invalid language)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, null, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));

    searchTerm = new SearchTermImpl("Greece", "e");
    resolveByText("Return of one entity without parents(1 letter language)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, null, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));

    searchTerm = new SearchTermImpl("Greece", "");
    resolveByText("Return of one entity without parents(empty language)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, null, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));

    searchTerm = new SearchTermImpl("Greece", null);
    resolveByText("Return of one entity without parents(null language)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, null, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByText_entityTypes() throws JsonProcessingException {
    SearchTerm searchTerm = new SearchTermImpl("Greece", "eng", Set.of(EntityType.PLACE));
    String expectedConvertedLanguage = "en";
    Entity placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    int enrichmentBasesExpectedResults = 1;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByText("Return of one entity without parents(Single entity type)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));

    searchTerm = new SearchTermImpl("Greece", "eng", Set.of(EntityType.TIMESPAN));
    expectedConvertedLanguage = "en";
    placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    enrichmentBasesExpectedResults = 0;
    entityWithParents = new LinkedList<>();
    resolveByText("Return of one entity without parents(Single incorrect entity type)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));

    searchTerm = new SearchTermImpl("Greece", "eng", Set.of(EntityType.TIMESPAN, EntityType.PLACE));
    expectedConvertedLanguage = "en";
    placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    enrichmentBasesExpectedResults = 1;
    entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByText("Return of one entity without parents(Multiple entity types)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByText_JsonProcessingException() throws JsonProcessingException {
    when(entityClientApi.getEnrichment(anyString(), anyString(), anyString(), isNull())).thenThrow(JsonProcessingException.class);
    assertThrows(UnknownException.class, () -> clientEntityResolver.resolveByText(Set.of(new SearchTermImpl("Greece", "en"))));
  }

  void resolveByText(String testName, Map<SearchTerm, EntitiesAndExpectedEnrichmentBases> searchTermsEntitiesMap)
      throws JsonProcessingException {
    for (Entry<SearchTerm, EntitiesAndExpectedEnrichmentBases> entry : searchTermsEntitiesMap.entrySet()) {
      final String entityTypes = entry.getKey().getCandidateTypes().stream()
                                      .map(entityType -> entityType.name().toLowerCase(Locale.US))
                                      .collect(Collectors.joining(","));
      final List<Entity> children = entry.getValue().getEntitiesWithParents().stream().map(LinkedList::peekFirst)
                                         .filter(Objects::nonNull).collect(Collectors.toList());
      when(entityClientApi.getEnrichment(entry.getKey().getTextValue(), entry.getValue().getExpectedConvertedLanguage(),
          entityTypes, null)).thenReturn(children);

      final List<Entity> parentEntities = entry.getValue().getEntitiesWithParents().stream().flatMap(List::stream)
                                               .filter(entity -> !children.contains(entity)).collect(Collectors.toList());
      for (Entity parentEntity : parentEntities) {
        when(entityClientApi.getEntityById(parentEntity.getEntityId())).thenReturn(parentEntity);
      }
    }

    final Map<SearchTerm, List<EnrichmentBase>> searchTermEnrichmentBaseMap = clientEntityResolver.resolveByText(
        new HashSet<>(searchTermsEntitiesMap.keySet()));

    //Verify each request has a result even if it's an empty list
    assertEquals(searchTermsEntitiesMap.size(), searchTermEnrichmentBaseMap.size());

    for (Entry<SearchTerm, EntitiesAndExpectedEnrichmentBases> entry : searchTermsEntitiesMap.entrySet()) {
      //For each search we expect an amount of enrichment bases
      assertEquals(entry.getValue().getEnrichmentBasesExpectedResults(), searchTermEnrichmentBaseMap.get(entry.getKey()).size());

      //Verify that each entity
      entry.getValue().getEntitiesWithParents().stream().flatMap(List::stream)
           .forEach(entity -> assertTrue(searchTermEnrichmentBaseMap.get(entry.getKey()).stream().anyMatch(
               item -> entity.getEntityId().equals(item.getAbout()))));
    }
  }

  @Test
  void resolveById() {
  }

  @Test
  void resolveByUri_singleEntity() throws MalformedURLException {
    Entity placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    final ReferenceTerm referenceTerm = new ReferenceTermImpl(new URL(PARENT_URI));
    int enrichmentBasesExpectedResults = 1;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByUri("Return of one entity without parents", Map.of(referenceTerm,
        new EntitiesAndExpectedEnrichmentBases(true, null, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByUri_parentEntity() throws MalformedURLException {
    ReferenceTerm referenceTerm = new ReferenceTermImpl(new URL(CHILD_URI));
    Entity placeEntity = new Place();
    placeEntity.setEntityId(CHILD_URI);
    placeEntity.setIsPartOfArray(List.of(PARENT_URI));
    Entity parentPlaceEntity = new Place();
    parentPlaceEntity.setEntityId(PARENT_URI);
    int enrichmentBasesExpectedResults = 2;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    entityWithParents.add(parentPlaceEntity);
    resolveByUri("Return one entity and one parent",
        Map.of(referenceTerm, new EntitiesAndExpectedEnrichmentBases(true, null, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));

    //Check with sameAs as well
    referenceTerm = new ReferenceTermImpl(new URL(CHILD_SAME_AS_URI));
    placeEntity = new Place();
    placeEntity.setEntityId(CHILD_URI);
    placeEntity.setIsPartOfArray(List.of(PARENT_URI));
    parentPlaceEntity = new Place();
    parentPlaceEntity.setEntityId(PARENT_URI);
    parentPlaceEntity.setIsPartOfArray(List.of(CHILD_URI));
    enrichmentBasesExpectedResults = 2;
    entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    entityWithParents.add(parentPlaceEntity);
    resolveByUri("Return one entity and one parent(Circular parents do not cause endless loop)",
        Map.of(referenceTerm, new EntitiesAndExpectedEnrichmentBases(false, null, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));

    //Circular does not fail or get stuck
    referenceTerm = new ReferenceTermImpl(new URL(CHILD_URI));
    placeEntity = new Place();
    placeEntity.setEntityId(CHILD_URI);
    placeEntity.setIsPartOfArray(List.of(PARENT_URI));
    parentPlaceEntity = new Place();
    parentPlaceEntity.setEntityId(PARENT_URI);
    parentPlaceEntity.setIsPartOfArray(List.of(CHILD_URI));
    enrichmentBasesExpectedResults = 2;
    entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    entityWithParents.add(parentPlaceEntity);
    resolveByUri("Return one entity and one parent(Circular parents do not cause endless loop)",
        Map.of(referenceTerm, new EntitiesAndExpectedEnrichmentBases(true, null, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));
  }

  void resolveByUri(String testName, Map<ReferenceTerm, EntitiesAndExpectedEnrichmentBases> referenceTermsEntitiesMap) {
    for (Entry<ReferenceTerm, EntitiesAndExpectedEnrichmentBases> entry : referenceTermsEntitiesMap.entrySet()) {
      final List<Entity> children = entry.getValue().getEntitiesWithParents().stream().map(LinkedList::getFirst)
                                         .collect(Collectors.toList());

      if (entry.getValue().isChildEuropeanaEntity) {
        when(entityClientApi.getEntityById(entry.getKey().getReference().toString())).thenReturn(children.get(0));
      } else {
        when(entityClientApi.getEntityByUri(entry.getKey().getReference().toString())).thenReturn(children);
      }

      final List<Entity> parentEntities = entry.getValue().getEntitiesWithParents().stream().flatMap(List::stream)
                                               .filter(entity -> !children.contains(entity)).collect(Collectors.toList());
      for (Entity parentEntity : parentEntities) {
        when(entityClientApi.getEntityById(parentEntity.getEntityId())).thenReturn(parentEntity);
      }
    }

    final Map<ReferenceTerm, List<EnrichmentBase>> searchTermEnrichmentBaseMap = clientEntityResolver.resolveByUri(
        new HashSet<>(referenceTermsEntitiesMap.keySet()));

    //Verify each request has a result even if it's an empty list
    assertEquals(referenceTermsEntitiesMap.size(), searchTermEnrichmentBaseMap.size());

    for (Entry<ReferenceTerm, EntitiesAndExpectedEnrichmentBases> entry : referenceTermsEntitiesMap.entrySet()) {
      //For each search we expect an amount of enrichment bases
      assertEquals(entry.getValue().getEnrichmentBasesExpectedResults(), searchTermEnrichmentBaseMap.get(entry.getKey()).size());

      //Verify that each entity
      entry.getValue().getEntitiesWithParents().stream().flatMap(List::stream)
           .forEach(entity -> assertTrue(searchTermEnrichmentBaseMap.get(entry.getKey()).stream().anyMatch(
               item -> entity.getEntityId().equals(item.getAbout()))));
    }
  }
}