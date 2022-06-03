package eu.europeana.enrichment.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import eu.europeana.enrichment.api.external.ReferenceValue;
import eu.europeana.enrichment.api.external.SearchValue;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.api.internal.SearchTermImpl;
import eu.europeana.enrichment.service.utils.EnrichmentTermsToEnrichmentBaseConverter;
import eu.europeana.enrichment.utils.EntityType;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class EnrichmentServiceTest {

  private static EnrichmentObjectUtils enrichmentObjectUtils;
  private static EntityResolver entityResolver;
  private static EnrichmentService enrichmentService;

  @BeforeAll
  static void prepare() {
    enrichmentObjectUtils = new EnrichmentObjectUtils();
    entityResolver = mock(EntityResolver.class);
    enrichmentService = new EnrichmentService(entityResolver);
  }

  @AfterEach
  void tearDown() {
    Mockito.reset(entityResolver);
  }

  @Test
  void enrichByEnrichmentSearchValues() {
    final SearchValue searchValue = new SearchValue("Clarence Williams", "pl", EntityType.AGENT,
        EntityType.TIMESPAN);
    final SearchTerm searchTerm = new SearchTermImpl(searchValue.getValue(),
        searchValue.getLanguage(), new HashSet<>(searchValue.getEntityTypes()));
    final EnrichmentBase agentEnrichmentBase = EnrichmentTermsToEnrichmentBaseConverter.convert(enrichmentObjectUtils.agentTerm1);
    assertNotNull(agentEnrichmentBase);
    final Map<SearchTerm, List<EnrichmentBase>> mockResultResolveByText = new HashMap<>();
    mockResultResolveByText.put(searchTerm, List.of(agentEnrichmentBase));
    when(entityResolver.resolveByText(Set.of(searchTerm)))
        .thenReturn(mockResultResolveByText);
    final List<EnrichmentResultBaseWrapper> enrichmentResultBaseWrappers = enrichmentService
        .enrichByEnrichmentSearchValues(List.of(searchValue));

    assertEquals(1, enrichmentResultBaseWrappers.size());
    assertEquals(1, enrichmentResultBaseWrappers.get(0).getEnrichmentBaseList().size());
    final EnrichmentBase enrichmentBase = enrichmentResultBaseWrappers.get(0)
        .getEnrichmentBaseList().get(0);
    assertEquals(agentEnrichmentBase.getAbout(), enrichmentBase.getAbout());
    verify(entityResolver, times(1)).resolveByText(Set.of(searchTerm));
  }

  @Test
  void enrichByEquivalenceValues() throws MalformedURLException {
    final ReferenceValue referenceValue = new ReferenceValue(
        enrichmentObjectUtils.agentTerm1.getEnrichmentEntity().getAbout(),
        Set.of(EntityType.AGENT, EntityType.TIMESPAN));
    final ReferenceTerm referenceTerm = new ReferenceTermImpl(
        new URL(referenceValue.getReference()), Set.copyOf(referenceValue.getEntityTypes()));
    final EnrichmentBase agentEnrichmentBase = EnrichmentTermsToEnrichmentBaseConverter.convert(enrichmentObjectUtils.agentTerm1);
    assertNotNull(agentEnrichmentBase);
    final Map<ReferenceTerm, List<EnrichmentBase>> mockResultResolveByUri = new HashMap<>();
    mockResultResolveByUri.put(referenceTerm, List.of(agentEnrichmentBase));
    when(entityResolver.resolveByUri(Set.of(referenceTerm)))
        .thenReturn(mockResultResolveByUri);
    final List<EnrichmentBase> enrichmentBases = enrichmentService
        .enrichByEquivalenceValues(referenceValue);

    assertEquals(1, enrichmentBases.size());
    final EnrichmentBase enrichmentBase = enrichmentBases.get(0);
    assertEquals(agentEnrichmentBase.getAbout(), enrichmentBase.getAbout());
    verify(entityResolver, times(1)).resolveByUri(Set.of(referenceTerm));
  }

  @Test
  void enrichByEquivalenceValues_throwsException() {
    final ReferenceValue referenceValue = new ReferenceValue("invalidUrl",
        Set.of(EntityType.AGENT, EntityType.TIMESPAN));
    final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
        () -> enrichmentService.enrichByEquivalenceValues(referenceValue));
    assertEquals(MalformedURLException.class, illegalArgumentException.getCause().getClass());
  }

  @Test
  void enrichById() throws MalformedURLException {
    final String entityAbout = enrichmentObjectUtils.agentTerm1.getEnrichmentEntity().getAbout();
    final ReferenceTerm referenceTerm = new ReferenceTermImpl(new URL(entityAbout),
        new HashSet<>());
    final EnrichmentBase agentEnrichmentBase = EnrichmentTermsToEnrichmentBaseConverter.convert(enrichmentObjectUtils.agentTerm1);
    assertNotNull(agentEnrichmentBase);
    final Map<ReferenceTerm, EnrichmentBase> mockResultResolveById = new HashMap<>();
    mockResultResolveById.put(referenceTerm, agentEnrichmentBase);
    when(entityResolver.resolveById(Set.of(referenceTerm)))
        .thenReturn(mockResultResolveById);
    final EnrichmentBase enrichmentBase = enrichmentService.enrichById(entityAbout);

    assertNotNull(enrichmentBase);
    assertEquals(agentEnrichmentBase.getAbout(), enrichmentBase.getAbout());
    verify(entityResolver, times(1)).resolveById(Set.of(referenceTerm));
  }

  @Test
  void enrichById_throwsException() {
    final String entityAbout = "InvalidUrl";
    final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
        () -> enrichmentService.enrichById(entityAbout));
    assertEquals(MalformedURLException.class, illegalArgumentException.getCause().getClass());
  }
}