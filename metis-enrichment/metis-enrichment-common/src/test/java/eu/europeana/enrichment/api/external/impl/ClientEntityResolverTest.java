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
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.api.internal.SearchTermImpl;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.entity.client.web.EntityClientApi;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Place;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ClientEntityResolverTest {

  private static ClientEntityResolver clientEntityResolver;
  private static EntityClientApi entityClientApi;

  static class EntitiesAndExpectedEnrichmentBases {

    private final List<LinkedList<Entity>> entitiesWithParents;
    private final String expectedConvertedLanguage;

    private final int enrichmentBasesExpectedResults;

    public EntitiesAndExpectedEnrichmentBases(String expectedConvertedLanguage, List<LinkedList<Entity>> entitiesWithParents,
        int enrichmentBasesExpectedResults) {
      this.expectedConvertedLanguage = expectedConvertedLanguage;
      this.entitiesWithParents = entitiesWithParents;
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
    placeEntity.setEntityId("http://data.europeana.eu/place/87");
    int enrichmentBasesExpectedResults = 1;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByText("Return of one entity without parents", Map.of(searchTerm,
        new EntitiesAndExpectedEnrichmentBases(expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByText_parentEntityTestsArguments() throws JsonProcessingException {
    SearchTerm searchTerm = new SearchTermImpl("Crete", "en");
    String expectedConvertedLanguage = "en";
    Entity placeEntity = new Place();
    placeEntity.setEntityId("http://data.europeana.eu/place/84838");
    placeEntity.setIsPartOfArray(List.of("http://data.europeana.eu/place/87"));
    Entity parentPlaceEntity = new Place();
    parentPlaceEntity.setEntityId("http://data.europeana.eu/place/87");
    int enrichmentBasesExpectedResults = 2;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    entityWithParents.add(parentPlaceEntity);
    resolveByText("Return one entity and one parent",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));

    searchTerm = new SearchTermImpl("Crete", "en");
    expectedConvertedLanguage = "en";
    placeEntity = new Place();
    placeEntity.setEntityId("http://data.europeana.eu/place/84838");
    placeEntity.setIsPartOfArray(List.of("http://data.europeana.eu/place/87"));
    parentPlaceEntity = new Place();
    parentPlaceEntity.setEntityId("http://data.europeana.eu/place/87");
    parentPlaceEntity.setIsPartOfArray(List.of("http://data.europeana.eu/place/84838"));
    enrichmentBasesExpectedResults = 2;
    entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    entityWithParents.add(parentPlaceEntity);
    resolveByText("Return one entity and one parent(Circular parents do not cause endless loop)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByText_languageTestsArguments() throws JsonProcessingException {
    SearchTermImpl searchTerm = new SearchTermImpl("Greece", "eng");
    String expectedConvertedLanguage = "en";
    Entity placeEntity = new Place();
    placeEntity.setEntityId("http://data.europeana.eu/place/84838");
    int enrichmentBasesExpectedResults = 1;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByText("Return of one entity without parents(3 letter language)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));

    searchTerm = new SearchTermImpl("Greece", "invalidLanguage");
    resolveByText("Return of one entity without parents(invalid language)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(null, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));

    searchTerm = new SearchTermImpl("Greece", "e");
    resolveByText("Return of one entity without parents(1 letter language)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(null, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));

    searchTerm = new SearchTermImpl("Greece", "");
    resolveByText("Return of one entity without parents(empty language)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(null, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));

    searchTerm = new SearchTermImpl("Greece", null);
    resolveByText("Return of one entity without parents(null language)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(null, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByText_entityTypesTestsArguments() throws JsonProcessingException {
    SearchTerm searchTerm = new SearchTermImpl("Greece", "eng", Set.of(EntityType.PLACE));
    String expectedConvertedLanguage = "en";
    Entity placeEntity = new Place();
    placeEntity.setEntityId("http://data.europeana.eu/place/84838");
    int enrichmentBasesExpectedResults = 1;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByText("Return of one entity without parents(Single entity type)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));

    searchTerm = new SearchTermImpl("Greece", "eng", Set.of(EntityType.TIMESPAN));
    expectedConvertedLanguage = "en";
    placeEntity = new Place();
    placeEntity.setEntityId("http://data.europeana.eu/place/84838");
    enrichmentBasesExpectedResults = 0;
    entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByText("Return of one entity without parents(Single incorrect entity type)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(expectedConvertedLanguage, new LinkedList<>(),
            enrichmentBasesExpectedResults)));

    searchTerm = new SearchTermImpl("Greece", "eng", Set.of(EntityType.TIMESPAN, EntityType.PLACE));
    expectedConvertedLanguage = "en";
    placeEntity = new Place();
    placeEntity.setEntityId("http://data.europeana.eu/place/84838");
    enrichmentBasesExpectedResults = 1;
    entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByText("Return of one entity without parents(Multiple entity types)",
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByTextException() throws JsonProcessingException {
    when(entityClientApi.getEnrichment(anyString(), anyString(), anyString(), isNull())).thenThrow(JsonProcessingException.class);
    assertThrows(UnknownException.class, () -> clientEntityResolver.resolveByText(Set.of(new SearchTermImpl("Greece", "en"))));
  }

  void resolveByText(String testName, Map<SearchTerm, EntitiesAndExpectedEnrichmentBases> searchTermsEntitiesMap)
      throws JsonProcessingException {
    for (Entry<SearchTerm, EntitiesAndExpectedEnrichmentBases> entry : searchTermsEntitiesMap.entrySet()) {
      final String entityTypes = entry.getKey().getCandidateTypes().stream()
                                      .map(entityType -> entityType.name().toLowerCase(Locale.US))
                                      .collect(Collectors.joining(","));
      final List<Entity> children = entry.getValue().getEntitiesWithParents().stream().map(LinkedList::getFirst)
                                         .collect(Collectors.toList());
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
      entry.getValue().getEntitiesWithParents().stream().flatMap(List::stream).forEach(entity -> {
        assertTrue(searchTermEnrichmentBaseMap.get(entry.getKey()).stream()
                                              .anyMatch(item -> entity.getEntityId().equals(item.getAbout())));
      });

    }
  }

  @Test
  void resolveById() {
  }

  @Test
  void resolveByUri() {
  }
}