package eu.europeana.enrichment.service;

import eu.europeana.enrichment.api.external.ReferenceValue;
import eu.europeana.enrichment.api.external.SearchValue;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultBaseWrapper;
import eu.europeana.enrichment.api.internal.EntityResolver;
import eu.europeana.enrichment.api.internal.ReferenceTerm;
import eu.europeana.enrichment.api.internal.ReferenceTermImpl;
import eu.europeana.enrichment.api.internal.SearchTerm;
import eu.europeana.enrichment.api.internal.SearchTermImpl;
import eu.europeana.enrichment.service.dao.EnrichmentDao;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Contains functionality for accessing entities from the enrichment database using {@link EnrichmentDao}.
 *
 * @author Simon Tzanakis
 * @since 2020-07-16
 */
@Service
public class EnrichmentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(EnrichmentService.class);

  private final EntityResolver entityResolver;

  /**
   * Parameter constructor.
   *
   * @param entityResolver the entity resolver
   */
  @Autowired
  public EnrichmentService(EntityResolver entityResolver) {
    this.entityResolver = entityResolver;
  }

  /**
   * Get an enrichment by providing a list of {@link SearchValue}s.
   *
   * @param searchValues a list of structured search values with parameters
   * @return the enrichment values in a structured list
   */
  public List<EnrichmentResultBaseWrapper> enrichByEnrichmentSearchValues(
      List<SearchValue> searchValues) {
    final List<SearchTerm> orderedSearchTerms = searchValues.stream().map(
        search -> new SearchTermImpl(search.getValue(), search.getLanguage(),
            Set.copyOf(search.getEntityTypes()))).collect(Collectors.toList());
    final Map<SearchTerm, List<EnrichmentBase>> result = entityResolver
        .resolveByText(new HashSet<>(orderedSearchTerms));
    return orderedSearchTerms.stream().map(result::get).map(EnrichmentResultBaseWrapper::new)
                             .collect(Collectors.toList());
  }

  /**
   * Get an enrichment by providing a URI, might match owl:sameAs.
   *
   * @param referenceValue The URI to check for match
   * @return the structured result of the enrichment
   */
  public List<EnrichmentBase> enrichByEquivalenceValues(ReferenceValue referenceValue) {
    try {
      final ReferenceTerm referenceTerm = new ReferenceTermImpl(
          new URL(referenceValue.getReference()), Set.copyOf(referenceValue.getEntityTypes()));
      return entityResolver.resolveByUri(Set.of(referenceTerm))
                           .getOrDefault(referenceTerm, Collections.emptyList());
    } catch (MalformedURLException e) {
      LOGGER.debug("There was a problem converting the input to ReferenceTermType");
      throw new IllegalArgumentException("The input values are invalid", e);
    }
  }

  /**
   * Get an enrichment by providing a URI.
   *
   * @param entityAbout The URI to check for match
   * @return the structured result of the enrichment
   */
  public EnrichmentBase enrichById(String entityAbout) {
    try {
      final ReferenceTerm referenceTerm = new ReferenceTermImpl(new URL(entityAbout),
          new HashSet<>());
      return entityResolver.resolveById(Set.of(referenceTerm)).get(referenceTerm);
    } catch (MalformedURLException e) {
      LOGGER.debug("There was a problem converting the input to ReferenceTermType");
      throw new IllegalArgumentException("The input values are invalid", e);
    }
  }
}
