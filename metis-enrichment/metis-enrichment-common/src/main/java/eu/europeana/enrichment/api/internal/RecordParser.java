package eu.europeana.enrichment.api.internal;

import eu.europeana.metis.schema.jibx.RDF;
import java.util.Set;

/**
 * Implementations of this interface provide functionality to parse records and extract search terms
 * and references for enrichment.
 */
public interface RecordParser {

  /**
   * This method parses records to obtain search terms for enrichment.
   *
   * @param rdf The record.
   * @return The search terms. It is recommended to return one object per term-language combination.
   */
  Set<SearchTermContext> parseSearchTerms(RDF rdf);

  /**
   * This method parses records to obtain references for enrichment.
   *
   * @param rdf The record.
   * @return The references. It is recommended to return one object per reference.
   */
  Set<ReferenceTermContext> parseReferences(RDF rdf);
}
