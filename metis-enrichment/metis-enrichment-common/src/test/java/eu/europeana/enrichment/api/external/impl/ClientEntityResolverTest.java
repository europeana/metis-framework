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
  void resolveByText_Entity_Without_Parents() throws JsonProcessingException {
    SearchTermImpl searchTerm = new SearchTermImpl("Greece", "en");
    String expectedConvertedLanguage = "en";
    Entity placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    int enrichmentBasesExpectedResults = 1;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByText(Map.of(searchTerm,
        new EntitiesAndExpectedEnrichmentBases(true, expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByText_Multiple_Entities_Without_Parents() throws JsonProcessingException {
    SearchTermImpl searchTerm = new SearchTermImpl("Greece", "en");
    String expectedConvertedLanguage = "en";
    Entity placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    int enrichmentBasesExpectedResults = 0;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByText(Map.of(searchTerm,
        new EntitiesAndExpectedEnrichmentBases(true, expectedConvertedLanguage, List.of(entityWithParents, entityWithParents),
            enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByText_Entity_With_One_Parent() throws JsonProcessingException {
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
    resolveByText(
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByText_Entity_With_One_Parent_Circular_OK() throws JsonProcessingException {
    SearchTerm searchTerm = new SearchTermImpl("Crete", "en");
    String expectedConvertedLanguage = "en";
    Entity placeEntity = new Place();
    placeEntity.setEntityId(CHILD_URI);
    placeEntity.setIsPartOfArray(List.of(PARENT_URI));
    Entity parentPlaceEntity = new Place();
    parentPlaceEntity.setEntityId(PARENT_URI);
    parentPlaceEntity.setIsPartOfArray(List.of(CHILD_URI));
    int enrichmentBasesExpectedResults = 2;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    entityWithParents.add(parentPlaceEntity);
    resolveByText(
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByText_3LetterLanguage_Entity_Without_Parents() throws JsonProcessingException {
    test_not_null("eng", "en");
  }

  @Test
  void resolveByText_InvalidLanguage_Entity_Without_Parents() throws JsonProcessingException {
    test_not_null("invalidLanguage", null);
  }

  @Test
  void resolveByText_1LetterLanguage_Entity_Without_Parents() throws JsonProcessingException {
    test_not_null("e", null);
  }

  @Test
  void resolveByText_EmptyLanguage_Entity_Without_Parents() throws JsonProcessingException {
    test_not_null("", null);
  }

  @Test
  void resolveByText_NullLanguage_Entity_Without_Parents() throws JsonProcessingException {
    test_not_null(null, null);
  }

  void test_not_null(String language, String expectedLanguage) throws JsonProcessingException {
    SearchTermImpl searchTerm = new SearchTermImpl("Greece", language);
    Entity placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    int enrichmentBasesExpectedResults = 1;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByText(Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, expectedLanguage, List.of(entityWithParents),
        enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByText_CorrectEntityType_Entity_Without_Parents() throws JsonProcessingException {
    SearchTerm searchTerm = new SearchTermImpl("Greece", "eng", Set.of(EntityType.PLACE));
    String expectedConvertedLanguage = "en";
    Entity placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    int enrichmentBasesExpectedResults = 1;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByText(
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByText_IncorrectEntityType_Entity_Without_Parents() throws JsonProcessingException {
    SearchTerm searchTerm = new SearchTermImpl("Greece", "eng", Set.of(EntityType.TIMESPAN));
    String expectedConvertedLanguage = "en";
    Entity placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    int enrichmentBasesExpectedResults = 0;
    resolveByText(
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, expectedConvertedLanguage, List.of(new LinkedList<>()),
            enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByText_MultipleEntityTypes_Entity_Without_Parents() throws JsonProcessingException {
    SearchTerm searchTerm = new SearchTermImpl("Greece", "eng", Set.of(EntityType.TIMESPAN, EntityType.PLACE));
    String expectedConvertedLanguage = "en";
    Entity placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    int enrichmentBasesExpectedResults = 1;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByText(
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, expectedConvertedLanguage, List.of(entityWithParents),
            enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByText_JsonProcessingException() throws JsonProcessingException {
    when(entityClientApi.getEnrichment(anyString(), anyString(), anyString(), isNull())).thenThrow(JsonProcessingException.class);
    final Set<SearchTermImpl> searchTerms = Set.of(new SearchTermImpl("Greece", "en"));
    assertThrows(UnknownException.class, () -> clientEntityResolver.resolveByText(searchTerms));
  }

  void resolveByText(Map<SearchTerm, EntitiesAndExpectedEnrichmentBases> searchTermsEntitiesMap)
      throws JsonProcessingException {
    for (Entry<SearchTerm, EntitiesAndExpectedEnrichmentBases> entry : searchTermsEntitiesMap.entrySet()) {
      final String entityTypes = entry.getKey().getCandidateTypes().stream()
                                      .map(entityType -> entityType.name().toLowerCase(Locale.US))
                                      .collect(Collectors.joining(","));
      final List<Entity> children = entry.getValue().getEntitiesWithParents().stream().map(LinkedList::peekFirst)
                                         .filter(Objects::nonNull).toList();
      when(entityClientApi.getEnrichment(entry.getKey().getTextValue(), entry.getValue().getExpectedConvertedLanguage(),
          entityTypes, null)).thenReturn(children);

      parentMatching(entry.getValue(), children);
    }

    final Map<SearchTerm, List<EnrichmentBase>> searchTermEnrichmentBasesMap = clientEntityResolver.resolveByText(
        new HashSet<>(searchTermsEntitiesMap.keySet()));

    resultAssertions(searchTermsEntitiesMap, searchTermEnrichmentBasesMap);
  }

  @Test
  void resolveById_Entity_Without_Parents() throws MalformedURLException {
    Entity placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    final ReferenceTerm referenceTerm = new ReferenceTermImpl(new URL(PARENT_URI));
    int enrichmentBasesExpectedResults = 1;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveById(Map.of(referenceTerm, new EntitiesAndExpectedEnrichmentBases(true, null, List.of(entityWithParents),
        enrichmentBasesExpectedResults)));
  }

  void resolveById(Map<ReferenceTerm, EntitiesAndExpectedEnrichmentBases> referenceTermsEntitiesMap) {
    for (Entry<ReferenceTerm, EntitiesAndExpectedEnrichmentBases> entry : referenceTermsEntitiesMap.entrySet()) {
      final List<Entity> children = entry.getValue().getEntitiesWithParents().stream().map(LinkedList::getFirst)
                                         .toList();

      if (entry.getValue().isChildEuropeanaEntity) {
        when(entityClientApi.getEntityById(entry.getKey().getReference().toString())).thenReturn(children.get(0));
      } else {
        when(entityClientApi.getEntityByUri(entry.getKey().getReference().toString())).thenReturn(children);
      }
      parentMatching(entry.getValue(), children);
    }

    final Map<ReferenceTerm, EnrichmentBase> referenceTermEnrichmentBaseMap = clientEntityResolver.resolveById(
        new HashSet<>(referenceTermsEntitiesMap.keySet()));
    final Map<ReferenceTerm, List<EnrichmentBase>> referenceTermEnrichmentBasesMap = referenceTermEnrichmentBaseMap.entrySet()
                                                                                                                   .stream()
                                                                                                                   .collect(
                                                                                                                       Collectors.toMap(
                                                                                                                           Entry::getKey,
                                                                                                                           entry -> List.of(
                                                                                                                               entry.getValue()),
                                                                                                                           (existing, incoming) -> existing));
    resultAssertions(referenceTermsEntitiesMap, referenceTermEnrichmentBasesMap);
  }

  @Test
  void resolveByUri_Entity_Without_Parents() throws MalformedURLException {
    Entity placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    final ReferenceTerm referenceTerm = new ReferenceTermImpl(new URL(PARENT_URI));
    int enrichmentBasesExpectedResults = 1;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByUri(Map.of(referenceTerm, new EntitiesAndExpectedEnrichmentBases(true, null, List.of(entityWithParents),
        enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByUri_Entity_With_One_Parent() throws MalformedURLException {
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
    resolveByUri(Map.of(referenceTerm, new EntitiesAndExpectedEnrichmentBases(true, null, List.of(entityWithParents),
        enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByUri_SameAsCheck_Entity_With_One_Parent_Circular_OK() throws MalformedURLException {
    ReferenceTerm referenceTerm = new ReferenceTermImpl(new URL(CHILD_SAME_AS_URI));
    Entity placeEntity = new Place();
    placeEntity.setEntityId(CHILD_URI);
    placeEntity.setIsPartOfArray(List.of(PARENT_URI));
    Entity parentPlaceEntity = new Place();
    parentPlaceEntity.setEntityId(PARENT_URI);
    parentPlaceEntity.setIsPartOfArray(List.of(CHILD_URI));
    int enrichmentBasesExpectedResults = 2;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    entityWithParents.add(parentPlaceEntity);
    resolveByUri(Map.of(referenceTerm, new EntitiesAndExpectedEnrichmentBases(false, null, List.of(entityWithParents),
        enrichmentBasesExpectedResults)));
  }

  @Test
  void resolveByUri_Entity_With_One_Parent_Circular_OK() throws MalformedURLException {
    ReferenceTerm referenceTerm = new ReferenceTermImpl(new URL(CHILD_URI));
    Entity placeEntity = new Place();
    placeEntity.setEntityId(CHILD_URI);
    placeEntity.setIsPartOfArray(List.of(PARENT_URI));
    Entity parentPlaceEntity = new Place();
    parentPlaceEntity.setEntityId(PARENT_URI);
    parentPlaceEntity.setIsPartOfArray(List.of(CHILD_URI));
    int enrichmentBasesExpectedResults = 2;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    entityWithParents.add(parentPlaceEntity);
    resolveByUri(Map.of(referenceTerm, new EntitiesAndExpectedEnrichmentBases(true, null, List.of(entityWithParents),
        enrichmentBasesExpectedResults)));
  }

  void resolveByUri(Map<ReferenceTerm, EntitiesAndExpectedEnrichmentBases> referenceTermsEntitiesMap) {
    for (Entry<ReferenceTerm, EntitiesAndExpectedEnrichmentBases> entry : referenceTermsEntitiesMap.entrySet()) {
      final List<Entity> children = entry.getValue().getEntitiesWithParents().stream().map(LinkedList::getFirst)
                                         .toList();

      if (entry.getValue().isChildEuropeanaEntity) {
        when(entityClientApi.getEntityById(entry.getKey().getReference().toString())).thenReturn(children.get(0));
      } else {
        when(entityClientApi.getEntityByUri(entry.getKey().getReference().toString())).thenReturn(children);
      }
      parentMatching(entry.getValue(), children);
    }

    final Map<ReferenceTerm, List<EnrichmentBase>> referenceTermEnrichmentBasesMap = clientEntityResolver.resolveByUri(
        new HashSet<>(referenceTermsEntitiesMap.keySet()));
    resultAssertions(referenceTermsEntitiesMap, referenceTermEnrichmentBasesMap);
  }

  private <T> void resultAssertions(Map<T, EntitiesAndExpectedEnrichmentBases> termsEntitiesMap,
      Map<T, List<EnrichmentBase>> termsEnrichmentBasesMap) {
    //Verify each request has a result even if it's an empty list
    assertEquals(termsEntitiesMap.size(), termsEnrichmentBasesMap.size());

    for (Entry<T, EntitiesAndExpectedEnrichmentBases> entry : termsEntitiesMap.entrySet()) {
      //For each search we expect an amount of enrichment bases
      assertEquals(entry.getValue().getEnrichmentBasesExpectedResults(), termsEnrichmentBasesMap.get(entry.getKey()).size());

      //For expected results we check to find that each expected entity exists in the results
      if (entry.getValue().getEnrichmentBasesExpectedResults() > 0) {
        entry.getValue().getEntitiesWithParents().stream().flatMap(List::stream)
             .forEach(entity -> assertTrue(termsEnrichmentBasesMap.get(entry.getKey()).stream().anyMatch(
                 item -> entity.getEntityId().equals(item.getAbout()))));
      }
    }
  }

  private void parentMatching(EntitiesAndExpectedEnrichmentBases entry, List<Entity> children) {
    final List<Entity> parentEntities = entry.getEntitiesWithParents().stream().flatMap(List::stream)
                                             .filter(entity -> !children.contains(entity)).toList();
    for (Entity parentEntity : parentEntities) {
      when(entityClientApi.getEntityById(parentEntity.getEntityId())).thenReturn(parentEntity);
    }
  }
}