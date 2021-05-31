package eu.europeana.enrichment.service;

import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.hamcrest.MockitoHamcrest.argThat;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.internal.ProxyFieldType;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.api.internal.SearchTermContext;
import eu.europeana.enrichment.api.internal.SearchTermImpl;
import eu.europeana.enrichment.internal.model.EnrichmentTerm;
import eu.europeana.enrichment.internal.model.OrganizationEnrichmentEntity;
import eu.europeana.enrichment.service.dao.EnrichmentDao;
import eu.europeana.enrichment.utils.EntityType;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

class PersistentEntityResolverTest {

  private static EnrichmentObjectUtils enrichmentObjectUtils;
  private static EnrichmentDao enrichmentDao;
  private static PersistentEntityResolver persistentEntityResolver;

  @BeforeAll
  static void prepare() {
    enrichmentObjectUtils = new EnrichmentObjectUtils();

    enrichmentDao = mock(EnrichmentDao.class);
    persistentEntityResolver = new PersistentEntityResolver(enrichmentDao);
  }

  @AfterEach
  void tearDown() {
    Mockito.reset(enrichmentDao);
  }

  @Test
  void resolveByText_WithTwoLetterLanguageAndEntityType() {
    final SearchTermImpl searchTerm = new SearchTermImpl("Clarence Williams", "pl",
        Set.of(EntityType.AGENT, EntityType.TIMESPAN));
    assertResolveByText(searchTerm, enrichmentObjectUtils.agentTerm1);
  }

  @Test
  void resolveByText_WithTwoLetterLanguageAndFieldType() {
    final SearchTermContext searchTerm = new SearchTermContext("Clarence Williams", "pl",
        Set.of(ProxyFieldType.DC_CREATOR, ProxyFieldType.DCTERMS_ISSUED));
    assertResolveByText(searchTerm, enrichmentObjectUtils.agentTerm1);
  }

  @Test
  void resolveByText_WithTwoLanguageAndEmptySearch() {
    final SearchTermImpl searchTerm = new SearchTermImpl(null, "pl",
        Set.of(EntityType.AGENT, EntityType.TIMESPAN));
    assertResolveByText(searchTerm, enrichmentObjectUtils.agentTerm1);
  }

  @Test
  void resolveByText_WithTwoLanguageAndEmptyEntityType() {
    final SearchTermImpl searchTerm = new SearchTermImpl("Clarence Williams", "pl",
        new HashSet<>());
    assertResolveByText(searchTerm, enrichmentObjectUtils.agentTerm1);
  }

  @Test
  void resolveByText_WithThreeLetterLanguageAndEntityType() {
    final SearchTermImpl searchTerm = new SearchTermImpl("Периодическое издание", "rus",
        Set.of(EntityType.AGENT, EntityType.CONCEPT));
    assertResolveByText(searchTerm, enrichmentObjectUtils.conceptTerm1);
  }

  @Test
  void resolveByText_WithNullLanguageAndEntityType() {
    final SearchTermImpl searchTerm = new SearchTermImpl("Периодическое издание", null,
        Set.of(EntityType.AGENT, EntityType.CONCEPT));
    assertResolveByText(searchTerm, enrichmentObjectUtils.conceptTerm1);
  }

  @Test
  void resolveByText_WithInvalidLanguageAndEntityType() {
    final SearchTermImpl searchTerm = new SearchTermImpl("Периодическое издание", "invalidLanguage",
        Set.of(EntityType.AGENT, EntityType.CONCEPT));
    assertResolveByText(searchTerm, enrichmentObjectUtils.conceptTerm1);

  }

  @Test
  void resolveByText_WithTwoLetterLanguageAndEntityType_AndParent() {
    final SearchTermImpl searchTerm = new SearchTermImpl("Margaret Lee (Singaporean actress)", "en",
        Set.of(EntityType.AGENT));

    //Fake the connection
    enrichmentObjectUtils.customPlaceTerm.getEnrichmentEntity()
        .setIsPartOf(enrichmentObjectUtils.placeTerm1.getEnrichmentEntity().getAbout());

    @SuppressWarnings("unchecked") ArgumentCaptor<Map<String, List<Pair<String, String>>>> fieldNameMapCaptor = ArgumentCaptor
        .forClass(Map.class);
    when(enrichmentDao.getAllEnrichmentTermsByFields(fieldNameMapCaptor.capture()))
        .thenReturn(List.of(enrichmentObjectUtils.customPlaceTerm))
        .thenReturn(Collections.emptyList());
    when(enrichmentDao.getEnrichmentTermByField(EnrichmentDao.ENTITY_ABOUT_FIELD,
        enrichmentObjectUtils.customPlaceTerm.getEnrichmentEntity().getIsPartOf()))
        .thenReturn(Optional.of(enrichmentObjectUtils.placeTerm1));
    final Map<SearchTerm, List<EnrichmentBase>> searchTermEnrichmentBaseListMap = persistentEntityResolver
        .resolveByText(Set.of(searchTerm));
    assertEquals(2, searchTermEnrichmentBaseListMap.get(searchTerm).size());
  }

  @Test
  void resolveByText_WithTwoLetterLanguageAndEntityType_AndParent_Circular() {
    final SearchTermImpl searchTerm = new SearchTermImpl("Margaret Lee (Singaporean actress)", "en",
        Set.of(EntityType.AGENT));

    //Fake the connection
    enrichmentObjectUtils.customPlaceTerm.getEnrichmentEntity()
        .setIsPartOf(enrichmentObjectUtils.placeTerm1.getEnrichmentEntity().getAbout());
    enrichmentObjectUtils.placeTerm1.getEnrichmentEntity()
        .setIsPartOf(enrichmentObjectUtils.customPlaceTerm.getEnrichmentEntity().getAbout());

    @SuppressWarnings("unchecked") ArgumentCaptor<Map<String, List<Pair<String, String>>>> fieldNameMapCaptor = ArgumentCaptor
        .forClass(Map.class);
    when(enrichmentDao.getAllEnrichmentTermsByFields(fieldNameMapCaptor.capture()))
        .thenReturn(List.of(enrichmentObjectUtils.customPlaceTerm))
        .thenReturn(Collections.emptyList());
    when(enrichmentDao.getEnrichmentTermByField(EnrichmentDao.ENTITY_ABOUT_FIELD,
        enrichmentObjectUtils.customPlaceTerm.getEnrichmentEntity().getIsPartOf()))
        .thenReturn(Optional.of(enrichmentObjectUtils.placeTerm1));
    when(enrichmentDao.getEnrichmentTermByField(EnrichmentDao.ENTITY_ABOUT_FIELD,
        enrichmentObjectUtils.placeTerm1.getEnrichmentEntity().getIsPartOf()))
        .thenReturn(Optional.of(enrichmentObjectUtils.customPlaceTerm));
    final Map<SearchTerm, List<EnrichmentBase>> searchTermEnrichmentBaseListMap = persistentEntityResolver
        .resolveByText(Set.of(searchTerm));
    assertEquals(2, searchTermEnrichmentBaseListMap.get(searchTerm).size());
  }

  @Test
  void resolveByText_WithTwoLetterLanguageAndEntityType_AndParent_VeryBroadTimespan() {
    final SearchTermImpl searchTerm = new SearchTermImpl("8. Jahrhundert", "de",
        Set.of(EntityType.TIMESPAN));

    //Fake the connection
    enrichmentObjectUtils.customTimespanTerm.getEnrichmentEntity()
        .setIsPartOf("http://semium.org/time/ChronologicalPeriod");

    @SuppressWarnings("unchecked") ArgumentCaptor<Map<String, List<Pair<String, String>>>> fieldNameMapCaptor = ArgumentCaptor
        .forClass(Map.class);
    when(enrichmentDao.getAllEnrichmentTermsByFields(fieldNameMapCaptor.capture()))
        .thenReturn(List.of(enrichmentObjectUtils.customTimespanTerm))
        .thenReturn(Collections.emptyList());
    final Map<SearchTerm, List<EnrichmentBase>> searchTermEnrichmentBaseListMap = persistentEntityResolver
        .resolveByText(Set.of(searchTerm));
    assertEquals(1, searchTermEnrichmentBaseListMap.get(searchTerm).size());
  }

  void assertResolveByText(SearchTerm searchTerm, EnrichmentTerm expectedEnrichmentTerm) {
    @SuppressWarnings("unchecked") ArgumentCaptor<Map<String, List<Pair<String, String>>>> fieldNameMapCaptor = ArgumentCaptor
        .forClass(Map.class);
    when(enrichmentDao.getAllEnrichmentTermsByFields(fieldNameMapCaptor.capture()))
        .thenReturn(List.of(expectedEnrichmentTerm)).thenReturn(Collections.emptyList());
    final Map<SearchTerm, List<EnrichmentBase>> searchTermEnrichmentBaseListMap = persistentEntityResolver
        .resolveByText(Set.of(searchTerm));

    //Make sure the queries are correct
    if (CollectionUtils.isEmpty(searchTerm.getCandidateTypes())) {
      final Map<String, List<Pair<String, String>>> fieldNameMap = fieldNameMapCaptor.getAllValues()
          .stream().filter(map -> map.get(null).size() == 0).findFirst().orElse(null);
      assertEnrichmentBaseResults(searchTerm, expectedEnrichmentTerm,
          searchTermEnrichmentBaseListMap, null, fieldNameMap);
    }

    for (EntityType entityType : searchTerm.getCandidateTypes()) {
      final Map<String, List<Pair<String, String>>> fieldNameMap = fieldNameMapCaptor.getAllValues()
          .stream().filter(map -> entityType.name().equals(map.get(null).get(0).getValue()))
          .findFirst().orElse(null);
      assertEnrichmentBaseResults(searchTerm, expectedEnrichmentTerm,
          searchTermEnrichmentBaseListMap, entityType, fieldNameMap);
    }
  }

  private void assertEnrichmentBaseResults(SearchTerm searchTerm,
      EnrichmentTerm expectedEnrichmentTerm,
      Map<SearchTerm, List<EnrichmentBase>> searchTermListMap, EntityType entityType,
      Map<String, List<Pair<String, String>>> fieldNameMap) {
    final List<EnrichmentBase> enrichmentBases = searchTermListMap.get(searchTerm);

    //Assert result
    if (StringUtils.isBlank(searchTerm.getTextValue())) {
      assertNull(fieldNameMap);
      assertNull(enrichmentBases);
    } else {
      assertNotNull(fieldNameMap);
      assertFieldNameMap(searchTerm.getTextValue(), searchTerm.getLanguage(), entityType,
          fieldNameMap);
      assertEquals(1, enrichmentBases.size());
      assertEquals(expectedEnrichmentTerm.getEnrichmentEntity().getAbout(),
          enrichmentBases.get(0).getAbout());
    }
  }

  private void assertFieldNameMap(String textValue, String language, EntityType entityType,
      Map<String, List<Pair<String, String>>> fieldNameMap) {
    if (entityType == null) {
      assertEquals(0, fieldNameMap.get(null).size());
    } else {
      assertEquals(entityType.name(), fieldNameMap.get(null).get(0).getValue());
    }
    final List<Pair<String, String>> pairs = fieldNameMap.get(EnrichmentDao.LABEL_INFOS_FIELD);
    assertTrue(pairs.stream().anyMatch(
        pair -> pair.getKey().equals(EnrichmentDao.LABEL_FIELD) && pair.getValue()
            .equals(textValue.toLowerCase(Locale.US))));

    final String languageToCheck = Arrays.stream(Locale.getISOLanguages()).map(Locale::new).filter(
        locale -> locale.getLanguage().equals(language) || locale.getISO3Language()
            .equals(language)).map(Locale::getLanguage).findFirst().orElse(null);

    if (StringUtils.isNotBlank(languageToCheck)) {
      assertTrue(pairs.stream().anyMatch(
          pair -> pair.getKey().equals(EnrichmentDao.LANG_FIELD) && pair.getValue()
              .equals(languageToCheck)));
    }
  }

  @Test
  void resolveById() throws MalformedURLException {
    final ReferenceTerm referenceTerm = new ReferenceTermImpl(
        new URL(enrichmentObjectUtils.placeTerm1.getEnrichmentEntity().getAbout()),
        Set.of(EntityType.PLACE, EntityType.TIMESPAN));
    assertResolveById(referenceTerm, enrichmentObjectUtils.placeTerm1);
  }

  @Test
  void resolveById_emptyResult() throws MalformedURLException {
    final ReferenceTerm referenceTerm = new ReferenceTermImpl(new URL("http://example.com"),
        Set.of(EntityType.PLACE, EntityType.TIMESPAN));
    assertResolveById(referenceTerm, null);
  }

  @Test
  void resolveById_throwsException() throws MalformedURLException {
    final ReferenceTerm referenceTerm = new ReferenceTermImpl(
        new URL(enrichmentObjectUtils.placeTerm1.getEnrichmentEntity().getAbout()),
        Set.of(EntityType.PLACE, EntityType.TIMESPAN));

    when(enrichmentDao.getAllEnrichmentTermsByFields(anyMap()))
        .thenThrow(new RuntimeException("Example"));
    assertEquals(0, persistentEntityResolver.resolveById(Set.of(referenceTerm)).size());
  }

  void assertResolveById(ReferenceTerm referenceTerm, EnrichmentTerm expectedEnrichmentTerm) {
    @SuppressWarnings("unchecked") ArgumentCaptor<Map<String, List<Pair<String, String>>>> fieldNameMapCaptor = ArgumentCaptor
        .forClass(Map.class);

    when(enrichmentDao.getAllEnrichmentTermsByFields(fieldNameMapCaptor.capture())).thenReturn(
        Optional.ofNullable(expectedEnrichmentTerm).map(List::of)
            .orElseGet(Collections::emptyList));
    final Map<ReferenceTerm, EnrichmentBase> referenceTermEnrichmentBaseMap = persistentEntityResolver
        .resolveById(Set.of(referenceTerm));

    assertEnrichmentBaseResults(referenceTerm, expectedEnrichmentTerm,
        referenceTermEnrichmentBaseMap, fieldNameMapCaptor.getValue());
  }

  private void assertEnrichmentBaseResults(ReferenceTerm referenceTerm,
      EnrichmentTerm expectedEnrichmentTerm,
      Map<ReferenceTerm, EnrichmentBase> referenceTermEnrichmentBaseMap,
      Map<String, List<Pair<String, String>>> fieldNameMap) {

    assertNotNull(fieldNameMap);
    assertEquals(1, fieldNameMap.get(null).size());
    assertEquals(EnrichmentDao.ENTITY_ABOUT_FIELD, fieldNameMap.get(null).get(0).getKey());
    assertEquals(referenceTerm.getReference().toString(), fieldNameMap.get(null).get(0).getValue());

    final EnrichmentBase enrichmentBase = referenceTermEnrichmentBaseMap.get(referenceTerm);
    if (expectedEnrichmentTerm == null) {
      assertNull(enrichmentBase);
    } else {
      assertNotNull(enrichmentBase);
      assertEquals(expectedEnrichmentTerm.getEnrichmentEntity().getAbout(),
          enrichmentBase.getAbout());
    }
  }

  @Test
  void resolveByUri() throws MalformedURLException {
    final ReferenceTerm referenceTerm = new ReferenceTermImpl(
        new URL(enrichmentObjectUtils.placeTerm1.getEnrichmentEntity().getAbout()),
        Set.of(EntityType.PLACE, EntityType.TIMESPAN));
    assertResolveByUri(referenceTerm, enrichmentObjectUtils.placeTerm1);
  }

  @Test
  void resolveByUri_emptyEntityTypes() throws MalformedURLException {
    final ReferenceTerm referenceTerm = new ReferenceTermImpl(
        new URL(enrichmentObjectUtils.placeTerm1.getEnrichmentEntity().getAbout()),
        Collections.emptySet());
    assertResolveByUri(referenceTerm, enrichmentObjectUtils.placeTerm1);
  }

  @Test
  void resolveByUri_emptyResult() throws MalformedURLException {
    final ReferenceTerm referenceTerm = new ReferenceTermImpl(new URL("http://example.com"),
        Set.of(EntityType.PLACE, EntityType.TIMESPAN));
    assertResolveByUri(referenceTerm, null);
  }

  @Test
  void resolveByUri_throwsException() throws MalformedURLException {
    final ReferenceTerm referenceTerm = new ReferenceTermImpl(
        new URL(enrichmentObjectUtils.placeTerm1.getEnrichmentEntity().getAbout()),
        Set.of(EntityType.PLACE, EntityType.TIMESPAN));

    when(enrichmentDao.getAllEnrichmentTermsByFields(anyMap()))
        .thenThrow(new RuntimeException("Example"));
    assertEquals(0, persistentEntityResolver.resolveByUri(Set.of(referenceTerm)).size());
  }

  @SuppressWarnings("unchecked")
  void assertResolveByUri(ReferenceTerm referenceTerm, EnrichmentTerm expectedEnrichmentTerm) {
    ArgumentCaptor<Map<String, List<Pair<String, String>>>> fieldNameMapCaptor = ArgumentCaptor
        .forClass(Map.class);

    final Matcher<Iterable<Pair<String, String>>> pairsMatcher;
    if (expectedEnrichmentTerm != null && referenceTerm.getCandidateTypes()
        .contains(expectedEnrichmentTerm.getEntityType())) {
      pairsMatcher = hasItems(
          (Pair<String, String>) new ImmutablePair(EnrichmentDao.ENTITY_ABOUT_FIELD,
              referenceTerm.getReference().toString()),
          (Pair<String, String>) new ImmutablePair(EnrichmentDao.ENTITY_TYPE_FIELD,
              expectedEnrichmentTerm.getEntityType().name()));
    } else {
      pairsMatcher = hasItems(
          (Pair<String, String>) new ImmutablePair(EnrichmentDao.ENTITY_ABOUT_FIELD,
              referenceTerm.getReference().toString()));
    }

    final Map<String, List<Pair<String, String>>> matcherAboutAndEntityType = (Map<String, List<Pair<String, String>>>) argThat(
        hasEntry(is(nullValue()), pairsMatcher));

    when(enrichmentDao.getAllEnrichmentTermsByFields(matcherAboutAndEntityType)).thenReturn(
        Optional.ofNullable(expectedEnrichmentTerm).map(List::of)
            .orElseGet(Collections::emptyList));

    final Map<ReferenceTerm, List<EnrichmentBase>> referenceTermEnrichmentBaseListMap = persistentEntityResolver
        .resolveByUri(Set.of(referenceTerm));

    verify(enrichmentDao, atLeast(1)).getAllEnrichmentTermsByFields(fieldNameMapCaptor.capture());
    assertEnrichmentBaseResolveByUri(referenceTerm, expectedEnrichmentTerm,
        referenceTermEnrichmentBaseListMap, fieldNameMapCaptor.getAllValues());

    final Set<EntityType> candidateTypes = new HashSet<>(referenceTerm.getCandidateTypes());
    if (expectedEnrichmentTerm != null) {
      candidateTypes.remove(expectedEnrichmentTerm.getEntityType());
    }

    for (EntityType entityType : candidateTypes) {
      //Verify that owl same as was checked for timespan as a failover search
      assertTrue(fieldNameMapCaptor.getAllValues().stream().map(list -> list.get(null)).anyMatch(
          pairs -> checkFieldNamePairs(referenceTerm, pairs, EnrichmentDao.ENTITY_OWL_SAME_AS_FIELD,
              entityType)));
    }
  }

  private void assertEnrichmentBaseResolveByUri(ReferenceTerm referenceTerm,
      EnrichmentTerm expectedEnrichmentTerm,
      Map<ReferenceTerm, List<EnrichmentBase>> referenceTermEnrichmentBaseListMap,
      List<Map<String, List<Pair<String, String>>>> fieldNameMapList) {

    assertNotNull(fieldNameMapList);

    for (EntityType entityType : referenceTerm.getCandidateTypes()) {
      assertTrue(fieldNameMapList.stream().map(list -> list.get(null)).anyMatch(
          pairs -> checkFieldNamePairs(referenceTerm, pairs, EnrichmentDao.ENTITY_ABOUT_FIELD,
              entityType)));
    }

    if (expectedEnrichmentTerm == null) {
      assertEquals(0, referenceTermEnrichmentBaseListMap.size());
    } else {
      final List<EnrichmentBase> enrichmentBases = referenceTermEnrichmentBaseListMap
          .get(referenceTerm);
      assertNotNull(enrichmentBases.get(0));
      assertEquals(expectedEnrichmentTerm.getEnrichmentEntity().getAbout(),
          enrichmentBases.get(0).getAbout());
    }
  }

  private boolean checkFieldNamePairs(ReferenceTerm referenceTerm, List<Pair<String, String>> pairs,
      String fieldName, EntityType entityType) {
    assertEquals(2, pairs.size());
    final boolean matchesFieldName = pairs.stream().anyMatch(
        pair -> pair.getKey().equals(fieldName) && pair.getValue()
            .equals(referenceTerm.getReference().toString()));

    final boolean matchesEntityType = pairs.stream().anyMatch(
        pair -> pair.getKey().equals(EnrichmentDao.ENTITY_TYPE_FIELD) && entityType == EntityType
            .valueOf(pair.getValue()));
    return matchesEntityType && matchesFieldName;
  }

  @Test
  void saveOrganization() {
    final ObjectId objectId = new ObjectId();
    when(enrichmentDao.getEnrichmentTermObjectIdByField(EnrichmentDao.ENTITY_ABOUT_FIELD,
        enrichmentObjectUtils.customOrganizationTerm.getEnrichmentEntity().getAbout()))
        .thenReturn(Optional.of(objectId));
    when(enrichmentDao.saveEnrichmentTerm(any(EnrichmentTerm.class)))
        .thenReturn(objectId.toString());
    when(enrichmentDao.getEnrichmentTermByField(EnrichmentDao.ID_FIELD, objectId.toString()))
        .thenReturn(Optional.of(enrichmentObjectUtils.customOrganizationTerm));
    final OrganizationEnrichmentEntity organizationEnrichmentEntity = persistentEntityResolver
        .saveOrganization(
            (OrganizationEnrichmentEntity) enrichmentObjectUtils.customOrganizationTerm
                .getEnrichmentEntity(), new Date(), new Date());

    verify(enrichmentDao, times(1))
        .getEnrichmentTermByField(EnrichmentDao.ID_FIELD, objectId.toString());
    assertEquals(enrichmentObjectUtils.customOrganizationTerm.getEnrichmentEntity().getAbout(),
        organizationEnrichmentEntity.getAbout());
  }

  @Test
  void findExistingOrganizations() {
    final List<String> organizationIds = new ArrayList<>();
    organizationIds
        .add(enrichmentObjectUtils.customOrganizationTerm.getEnrichmentEntity().getAbout());
    organizationIds.add("non_existent_id");
    when(enrichmentDao.getAllEnrichmentTermsByFields(anyMap()))
        .thenReturn(List.of(enrichmentObjectUtils.customOrganizationTerm))
        .thenReturn(Collections.emptyList());
    final List<String> existingOrganizations = persistentEntityResolver
        .findExistingOrganizations(organizationIds);

    assertEquals(1, existingOrganizations.size());
    assertTrue(existingOrganizations.stream().anyMatch(id -> id
        .equals(enrichmentObjectUtils.customOrganizationTerm.getEnrichmentEntity().getAbout())));
  }

  @Test
  void getOrganizationByUri() {
    when(enrichmentDao.getAllEnrichmentTermsByFields(anyMap()))
        .thenReturn(List.of(enrichmentObjectUtils.customOrganizationTerm))
        .thenReturn(Collections.emptyList());
    final Optional<OrganizationEnrichmentEntity> organizationByUri = persistentEntityResolver
        .getOrganizationByUri(
            enrichmentObjectUtils.customOrganizationTerm.getEnrichmentEntity().getAbout());

    assertTrue(organizationByUri.isPresent());
    assertEquals(organizationByUri.get().getAbout(),
        enrichmentObjectUtils.customOrganizationTerm.getEnrichmentEntity().getAbout());
  }

  @Test
  void deleteOrganizations() {
    final List<String> organizationIds = List.of("example_id");
    persistentEntityResolver.deleteOrganizations(organizationIds);
    verify(enrichmentDao, times(1)).deleteEnrichmentTerms(EntityType.ORGANIZATION, organizationIds);
    verifyNoMoreInteractions(enrichmentDao);
  }

  @Test
  void deleteOrganization() {
    final String organizationId = "example_id";
    persistentEntityResolver.deleteOrganization(organizationId);
    verify(enrichmentDao, times(1)).deleteEnrichmentTerms(argThat(is(EntityType.ORGANIZATION)),
        (List) argThat(hasItems(organizationId)));
    verifyNoMoreInteractions(enrichmentDao);
  }

  @Test
  void getDateOfLastUpdatedOrganization() {
    final Date date = new Date();
    when(enrichmentDao.getDateOfLastUpdatedEnrichmentTerm(EntityType.ORGANIZATION))
        .thenReturn(date);
    final Date dateOfLastUpdatedOrganization = persistentEntityResolver
        .getDateOfLastUpdatedOrganization();
    assertEquals(date, dateOfLastUpdatedOrganization);
    verify(enrichmentDao, times(1)).getDateOfLastUpdatedEnrichmentTerm(EntityType.ORGANIZATION);
    verifyNoMoreInteractions(enrichmentDao);

  }
}