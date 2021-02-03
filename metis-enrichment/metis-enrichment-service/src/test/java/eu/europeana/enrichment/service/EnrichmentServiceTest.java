package eu.europeana.enrichment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.europeana.enrichment.api.external.ReferenceValue;
import eu.europeana.enrichment.api.external.SearchValue;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.api.internal.SearchTermImpl;
import eu.europeana.enrichment.internal.model.OrganizationEnrichmentEntity;
import eu.europeana.enrichment.utils.EntityType;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javax.ws.rs.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EnrichmentServiceTest {

  private static EnrichmentObjectUtils enrichmentObjectUtils;
  private static PersistentEntityResolver persistentEntityResolver;
  private static EnrichmentService enrichmentService;


  @BeforeAll
  static void prepare() {
    enrichmentObjectUtils = new EnrichmentObjectUtils();
    persistentEntityResolver = mock(PersistentEntityResolver.class);
    enrichmentService = new EnrichmentService(persistentEntityResolver);
  }

  @BeforeEach
  void setUp() {
  }

  @AfterEach
  void tearDown() {
    Mockito.reset(persistentEntityResolver);
  }

  @Test
  void enrichByEnrichmentSearchValues() {
    final SearchValue searchValue = new SearchValue("Clarence Williams", "pl", EntityType.AGENT,
        EntityType.TIMESPAN);
    final SearchTerm searchTerm = new SearchTermImpl(searchValue.getValue(),
        searchValue.getLanguage(), new HashSet<>(searchValue.getEntityTypes()));
    final EnrichmentBase agentEnrichmentBase = Converter.convert(enrichmentObjectUtils.agentTerm1);
    assertNotNull(agentEnrichmentBase);
    final Map<SearchTerm, List<EnrichmentBase>> searchTermListHashMap = new HashMap<>();
    searchTermListHashMap.put(searchTerm, List.of(agentEnrichmentBase));
    when(persistentEntityResolver.resolveByText(Set.of(searchTerm)))
        .thenReturn(searchTermListHashMap);
    final List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrappers = enrichmentService
        .enrichByEnrichmentSearchValues(List.of(searchValue));

    assertEquals(1, enrichmentResultBaseWrappers.size());
    assertEquals(1, enrichmentResultBaseWrappers.get(0).getEnrichmentBaseList().size());
    final EnrichmentBase enrichmentBase = enrichmentResultBaseWrappers.get(0)
        .getEnrichmentBaseList().get(0);
    assertEquals(agentEnrichmentBase.getAbout(), enrichmentBase.getAbout());
    verify(persistentEntityResolver, times(1)).resolveByText(Set.of(searchTerm));
  }

  @Test
  void enrichByEquivalenceValues() throws MalformedURLException {
    final ReferenceValue referenceValue = new ReferenceValue(
        enrichmentObjectUtils.agentTerm1.getEnrichmentEntity().getAbout(),
        Set.of(EntityType.AGENT, EntityType.TIMESPAN));
    final ReferenceTerm referenceTerm = new ReferenceTermImpl(
        new URL(referenceValue.getReference()), Set.copyOf(referenceValue.getEntityTypes()));
    final EnrichmentBase agentEnrichmentBase = Converter.convert(enrichmentObjectUtils.agentTerm1);
    assertNotNull(agentEnrichmentBase);
    final Map<ReferenceTerm, List<EnrichmentBase>> referenceTermListHashMap = new HashMap<>();
    referenceTermListHashMap.put(referenceTerm, List.of(agentEnrichmentBase));
    when(persistentEntityResolver.resolveByUri(Set.of(referenceTerm)))
        .thenReturn(referenceTermListHashMap);
    final List<EnrichmentBase> enrichmentBases = enrichmentService
        .enrichByEquivalenceValues(referenceValue);

    assertEquals(1, enrichmentBases.size());
    final EnrichmentBase enrichmentBase = enrichmentBases.get(0);
    assertEquals(agentEnrichmentBase.getAbout(), enrichmentBase.getAbout());
    verify(persistentEntityResolver, times(1)).resolveByUri(Set.of(referenceTerm));
  }

  @Test
  void enrichByEquivalenceValues_throwsException() {
    final ReferenceValue referenceValue = new ReferenceValue("invalidUrl",
        Set.of(EntityType.AGENT, EntityType.TIMESPAN));
    final BadRequestException badRequestException = assertThrows(BadRequestException.class,
        () -> enrichmentService.enrichByEquivalenceValues(referenceValue));
    assertEquals(MalformedURLException.class, badRequestException.getCause().getClass());
  }

  @Test
  void enrichById() throws MalformedURLException {
    final String entityAbout = enrichmentObjectUtils.agentTerm1.getEnrichmentEntity().getAbout();
    final ReferenceTerm referenceTerm = new ReferenceTermImpl(new URL(entityAbout),
        new HashSet<>());
    final EnrichmentBase agentEnrichmentBase = Converter.convert(enrichmentObjectUtils.agentTerm1);
    assertNotNull(agentEnrichmentBase);
    final Map<ReferenceTerm, EnrichmentBase> referenceTermListHashMap = new HashMap<>();
    referenceTermListHashMap.put(referenceTerm, agentEnrichmentBase);
    when(persistentEntityResolver.resolveById(Set.of(referenceTerm)))
        .thenReturn(referenceTermListHashMap);
    final EnrichmentBase enrichmentBase = enrichmentService.enrichById(entityAbout);

    assertNotNull(enrichmentBase);
    assertEquals(agentEnrichmentBase.getAbout(), enrichmentBase.getAbout());
    verify(persistentEntityResolver, times(1)).resolveById(Set.of(referenceTerm));
  }

  @Test
  void enrichById_throwsException() {
    final String entityAbout = "InvalidUrl";
    final BadRequestException badRequestException = assertThrows(BadRequestException.class,
        () -> enrichmentService.enrichById(entityAbout));
    assertEquals(MalformedURLException.class, badRequestException.getCause().getClass());
  }

  @Test
  void saveOrganization() {
    final OrganizationEnrichmentEntity organizationEnrichmentEntity = (OrganizationEnrichmentEntity) enrichmentObjectUtils.organizationTerm1
        .getEnrichmentEntity();
    final Date created = new Date();
    final Date updated = new Date();
    when(persistentEntityResolver.saveOrganization(organizationEnrichmentEntity, created, updated))
        .thenReturn(organizationEnrichmentEntity);
    final OrganizationEnrichmentEntity organizationEnrichmentEntityResult = enrichmentService
        .saveOrganization(organizationEnrichmentEntity, created, updated);

    assertNotNull(organizationEnrichmentEntityResult);
    assertEquals(organizationEnrichmentEntity.getAbout(),
        organizationEnrichmentEntityResult.getAbout());
  }

  @Test
  void findExistingOrganizations() {
    final List<String> toSearch = List
        .of(enrichmentObjectUtils.organizationTerm1.getEnrichmentEntity().getAbout(),
            enrichmentObjectUtils.customOrganizationTerm.getEnrichmentEntity().getAbout());
    when(persistentEntityResolver.findExistingOrganizations(toSearch)).thenReturn(
        List.of(enrichmentObjectUtils.organizationTerm1.getEnrichmentEntity().getAbout()));
    final List<String> existingOrganizations = enrichmentService
        .findExistingOrganizations(toSearch);

    assertNotNull(existingOrganizations);
    assertEquals(1, existingOrganizations.size());
    assertEquals(existingOrganizations.get(0),
        enrichmentObjectUtils.organizationTerm1.getEnrichmentEntity().getAbout());
  }

  @Test
  void getOrganizationByUri() {
    final String toSearch = enrichmentObjectUtils.organizationTerm1.getEnrichmentEntity()
        .getAbout();
    when(persistentEntityResolver.getOrganizationByUri(toSearch)).thenReturn(Optional
        .of((OrganizationEnrichmentEntity) enrichmentObjectUtils.organizationTerm1
            .getEnrichmentEntity()));
    final Optional<OrganizationEnrichmentEntity> organizationByUri = enrichmentService
        .getOrganizationByUri(toSearch);

    assertTrue(organizationByUri.isPresent());
    assertEquals(organizationByUri.get().getAbout(),
        enrichmentObjectUtils.organizationTerm1.getEnrichmentEntity().getAbout());
  }

  @Test
  void deleteOrganizations() {
    final List<String> toDelete = List
        .of(enrichmentObjectUtils.organizationTerm1.getEnrichmentEntity().getAbout(),
            enrichmentObjectUtils.customOrganizationTerm.getEnrichmentEntity().getAbout());
    enrichmentService.deleteOrganizations(toDelete);
    verify(persistentEntityResolver, times(1)).deleteOrganizations(toDelete);
  }

  @Test
  void deleteOrganization() {
    final String toDelete = enrichmentObjectUtils.organizationTerm1.getEnrichmentEntity()
        .getAbout();
    enrichmentService.deleteOrganization(toDelete);
    verify(persistentEntityResolver, times(1)).deleteOrganization(toDelete);
  }

  @Test
  void getDateOfLastUpdatedOrganization() {
    when(persistentEntityResolver.getDateOfLastUpdatedOrganization())
        .thenReturn(enrichmentObjectUtils.organizationTerm1.getUpdated());
    final Date dateOfLastUpdatedOrganization = enrichmentService.getDateOfLastUpdatedOrganization();
    verify(persistentEntityResolver, times(1)).getDateOfLastUpdatedOrganization();
    assertEquals(enrichmentObjectUtils.organizationTerm1.getUpdated(),
        dateOfLastUpdatedOrganization);
  }
}