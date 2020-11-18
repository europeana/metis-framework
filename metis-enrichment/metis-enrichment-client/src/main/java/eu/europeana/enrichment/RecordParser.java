package eu.europeana.enrichment;

import eu.europeana.enrichment.api.external.EnrichmentReference;
import eu.europeana.enrichment.api.external.EnrichmentSearch;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.List;

public interface RecordParser {

  List<EnrichmentSearch> parseSearchTerms(RDF rdf);

  List<EnrichmentReference> parseReferences(RDF rdf);

}
