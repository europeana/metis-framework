package eu.europeana.enrichment.api.external.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

import eu.europeana.enrichment.api.external.exceptions.EntityApiException;
import eu.europeana.enrichment.api.external.impl.ClientEntityResolver.OperationMode;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.api.internal.SearchTermImpl;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.entity.client.EntityApiClient;
import eu.europeana.entity.client.exception.EntityClientException;
import eu.europeana.entitymanagement.definitions.model.Entity;
import eu.europeana.entitymanagement.definitions.model.Place;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

class ClientEntityResolverTest {

  private static ClientEntityResolver clientEntityResolver;
  private static EntityApiClient entityClientApi;
  private static final String CHILD_URI = "http://data.europeana.eu/place/84838";
  private static final String PARENT_URI = "http://data.europeana.eu/place/87";
  private static final String CHILD_SAME_AS_URI = "http://viaf.org/viaf/237254468";

  record EntitiesAndExpectedEnrichmentBases(boolean isChildEuropeanaEntity,
                                            String expectedConvertedLanguage,
                                            List<LinkedList<Entity>> entitiesWithParents,
                                            int enrichmentBasesExpectedResults) {
  }


  static void prepare(OperationMode mode) {
    entityClientApi = mock(EntityApiClient.class);
    clientEntityResolver = new ClientEntityResolver(entityClientApi, mode);
  }

  @AfterEach
  void tearDown() {
    reset(entityClientApi);
  }

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByText_Entity_Without_Parents(OperationMode operationMode) throws EntityClientException {
    prepare(operationMode);
    SearchTermImpl searchTerm = new SearchTermImpl("Netherlands", "en");
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

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByText_Multiple_Entities_Without_Parents(OperationMode operationMode) throws EntityClientException {
    prepare(operationMode);
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

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByText_Entity_With_One_Parent(OperationMode operationMode) throws EntityClientException {
    prepare(operationMode);
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

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByText_Entity_With_One_Parent_Circular_OK(OperationMode operationMode) throws EntityClientException {
    prepare(operationMode);
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

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByText_3LetterLanguage_Entity_Without_Parents(OperationMode operationMode) throws EntityClientException {
    prepare(operationMode);
    test_not_null("eng", "en");
  }

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByText_InvalidLanguage_Entity_Without_Parents(OperationMode operationMode) throws EntityClientException {
    prepare(operationMode);
    test_not_null("invalidLanguage", null);
  }

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByText_1LetterLanguage_Entity_Without_Parents(OperationMode operationMode) throws EntityClientException {
    prepare(operationMode);
    test_not_null("e", null);
  }

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByText_EmptyLanguage_Entity_Without_Parents(OperationMode operationMode) throws EntityClientException {
    prepare(operationMode);
    test_not_null("", null);
  }

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByText_NullLanguage_Entity_Without_Parents(OperationMode operationMode) throws EntityClientException {
    prepare(operationMode);
    test_not_null(null, null);
  }

  void test_not_null(String language, String expectedLanguage) throws EntityClientException {
    SearchTermImpl searchTerm = new SearchTermImpl("Greece", language);
    Entity placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    int enrichmentBasesExpectedResults = 1;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByText(Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, expectedLanguage, List.of(entityWithParents),
        enrichmentBasesExpectedResults)));
  }

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByText_CorrectEntityType_Entity_Without_Parents(OperationMode operationMode) throws EntityClientException {
    prepare(operationMode);
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

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByText_IncorrectEntityType_Entity_Without_Parents(OperationMode operationMode) throws EntityClientException {
    prepare(operationMode);
    SearchTerm searchTerm = new SearchTermImpl("Greece", "eng", Set.of(EntityType.TIMESPAN));
    String expectedConvertedLanguage = "en";
    Entity placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    int enrichmentBasesExpectedResults = 0;
    resolveByText(
        Map.of(searchTerm, new EntitiesAndExpectedEnrichmentBases(true, expectedConvertedLanguage, List.of(new LinkedList<>()),
            enrichmentBasesExpectedResults)));
  }

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByText_MultipleEntityTypes_Entity_Without_Parents(OperationMode operationMode) throws EntityClientException {
    prepare(operationMode);
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

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByText_JsonProcessingException(OperationMode operationMode) throws EntityClientException {
    prepare(operationMode);
    when(entityClientApi.enrichEntity(anyString(), anyString(), anyString(), isNull())).thenThrow(EntityClientException.class);
    final Set<SearchTermImpl> searchTerms = Set.of(new SearchTermImpl("Italy", "it"));
    assertThrows(EntityApiException.class, () -> clientEntityResolver.resolveByText(searchTerms));
  }

  void resolveByText(Map<SearchTerm, EntitiesAndExpectedEnrichmentBases> searchTermsEntitiesMap)
      throws EntityClientException {
    for (Entry<SearchTerm, EntitiesAndExpectedEnrichmentBases> entry : searchTermsEntitiesMap.entrySet()) {
      final String entityTypes = entry.getKey().getCandidateTypes().stream()
                                      .map(entityType -> entityType.name().toLowerCase(Locale.US))
                                      .collect(Collectors.joining(","));
      final List<Entity> children = entry.getValue().entitiesWithParents().stream().map(LinkedList::peekFirst)
                                         .filter(Objects::nonNull).toList();
      when(entityClientApi.enrichEntity(entry.getKey().getTextValue(), entry.getValue().expectedConvertedLanguage(),
          entityTypes, null)).thenReturn(children);

      parentMatching(entry.getValue(), children);
    }

    final Map<SearchTerm, List<EnrichmentBase>> searchTermEnrichmentBasesMap = clientEntityResolver.resolveByText(
        new HashSet<>(searchTermsEntitiesMap.keySet()));

    resultAssertions(searchTermsEntitiesMap, searchTermEnrichmentBasesMap);
  }

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveById_For_equivalency(OperationMode operationMode)
      throws URISyntaxException, MalformedURLException, EntityClientException {
    prepare(operationMode);
    final ReferenceTerm referenceTermId = new ReferenceTermImpl(new URI(CHILD_URI).toURL());
    final ReferenceTerm referenceTermEquivalence = new ReferenceTermImpl(new URI(CHILD_SAME_AS_URI).toURL());
    final Entity placeEntity = new Place();
    placeEntity.setEntityId(CHILD_URI);
    when(entityClientApi.getEntity(referenceTermId.getReference().toString())).thenReturn(placeEntity);
    when(entityClientApi.getEntity(referenceTermEquivalence.getReference().toString())).thenReturn(placeEntity);

    final Map<ReferenceTerm, EnrichmentBase> resultId = clientEntityResolver.resolveById(Set.of(referenceTermId));
    assertEquals(1, resultId.size());
    assertSame(placeEntity.getEntityId(), resultId.get(referenceTermId).getAbout());
    final Map<ReferenceTerm, EnrichmentBase> resultEquivalence = clientEntityResolver.resolveById(Set.of(referenceTermEquivalence));
    assertEquals(0, resultEquivalence.size());
  }

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveById_Entity_Without_Parents(OperationMode operationMode)
      throws MalformedURLException, URISyntaxException, EntityClientException {
    prepare(operationMode);
    Entity placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    final ReferenceTerm referenceTerm = new ReferenceTermImpl(new URI(PARENT_URI).toURL());
    int enrichmentBasesExpectedResults = 1;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveById(Map.of(referenceTerm, new EntitiesAndExpectedEnrichmentBases(true, null,
        List.of(entityWithParents), enrichmentBasesExpectedResults)));
  }

  void resolveById(Map<ReferenceTerm, EntitiesAndExpectedEnrichmentBases> referenceTermsEntitiesMap)
      throws EntityClientException {
    for (Entry<ReferenceTerm, EntitiesAndExpectedEnrichmentBases> entry : referenceTermsEntitiesMap.entrySet()) {
      final List<Entity> children = entry.getValue().entitiesWithParents().stream().map(LinkedList::getFirst)
                                         .toList();

      if (entry.getValue().isChildEuropeanaEntity) {
        when(entityClientApi.getEntity(entry.getKey().getReference().toString())).thenReturn(children.getFirst());
      } else {
        when(entityClientApi.resolveEntity(entry.getKey().getReference().toString())).thenReturn(children);
      }
      parentMatching(entry.getValue(), children);
    }

    final Map<ReferenceTerm, EnrichmentBase> referenceTermEnrichmentBaseMap = clientEntityResolver.resolveById(
        new HashSet<>(referenceTermsEntitiesMap.keySet()));
    final Map<ReferenceTerm, List<EnrichmentBase>> referenceTermEnrichmentBasesMap =
        referenceTermEnrichmentBaseMap.entrySet().stream().collect(
            Collectors.toMap(Entry::getKey, entry -> List.of(entry.getValue()),
                (existing, incoming) -> existing));
    resultAssertions(referenceTermsEntitiesMap, referenceTermEnrichmentBasesMap);
  }

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByUri_Entity_Without_Parents(OperationMode operationMode)
      throws MalformedURLException, URISyntaxException, EntityClientException {
    prepare(operationMode);
    Entity placeEntity = new Place();
    placeEntity.setEntityId(PARENT_URI);
    final ReferenceTerm referenceTerm = new ReferenceTermImpl(new URI(PARENT_URI).toURL());
    int enrichmentBasesExpectedResults = 1;
    LinkedList<Entity> entityWithParents = new LinkedList<>();
    entityWithParents.add(placeEntity);
    resolveByUri(Map.of(referenceTerm, new EntitiesAndExpectedEnrichmentBases(true, null, List.of(entityWithParents),
        enrichmentBasesExpectedResults)));
  }

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByUri_Entity_With_One_Parent(OperationMode operationMode)
      throws MalformedURLException, URISyntaxException, EntityClientException {
    prepare(operationMode);
    ReferenceTerm referenceTerm = new ReferenceTermImpl(new URI(CHILD_URI).toURL());
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

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByUri_SameAsCheck_Entity_With_One_Parent_Circular_OK(OperationMode operationMode)
      throws URISyntaxException, MalformedURLException, EntityClientException {
    prepare(operationMode);
    ReferenceTerm referenceTerm = new ReferenceTermImpl(new URI(CHILD_SAME_AS_URI).toURL());
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

  @ParameterizedTest
  @EnumSource(OperationMode.class)
  void resolveByUri_Entity_With_One_Parent_Circular_OK(OperationMode operationMode)
      throws URISyntaxException, MalformedURLException, EntityClientException {
    prepare(operationMode);
    ReferenceTerm referenceTerm = new ReferenceTermImpl(new URI(CHILD_URI).toURL());
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

  void resolveByUri(Map<ReferenceTerm, EntitiesAndExpectedEnrichmentBases> referenceTermsEntitiesMap)
      throws EntityClientException {
    for (Entry<ReferenceTerm, EntitiesAndExpectedEnrichmentBases> entry : referenceTermsEntitiesMap.entrySet()) {
      final List<Entity> children = entry.getValue().entitiesWithParents().stream().map(LinkedList::getFirst)
                                         .toList();

      if (entry.getValue().isChildEuropeanaEntity) {
        when(entityClientApi.getEntity(entry.getKey().getReference().toString())).thenReturn(children.getFirst());
      } else {
        when(entityClientApi.resolveEntity(entry.getKey().getReference().toString())).thenReturn(children);
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
      assertEquals(entry.getValue().enrichmentBasesExpectedResults(), termsEnrichmentBasesMap.get(entry.getKey()).size());

      //For expected results we check to find that each expected entity exists in the results
      if (entry.getValue().enrichmentBasesExpectedResults() > 0) {
        entry.getValue().entitiesWithParents().stream().flatMap(List::stream)
             .forEach(entity -> assertTrue(termsEnrichmentBasesMap.get(entry.getKey()).stream().anyMatch(
                 item -> entity.getEntityId().equals(item.getAbout()))));
      }
    }
  }

  private void parentMatching(EntitiesAndExpectedEnrichmentBases entry, List<Entity> children)
      throws EntityClientException {
    final Set<String> childIds = children.stream().map(Entity::getEntityId).collect(Collectors.toSet());
    final List<Entity> parentEntities = entry.entitiesWithParents().stream().flatMap(List::stream)
                                             .filter(entity -> !childIds.contains(entity.getEntityId()))
                                             .toList();
    for (Entity parentEntity : parentEntities) {
      when(entityClientApi.getEntity(parentEntity.getEntityId())).thenReturn(parentEntity);
    }
  }
}
