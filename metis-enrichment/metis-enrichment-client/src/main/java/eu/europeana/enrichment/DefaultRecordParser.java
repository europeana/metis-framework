package eu.europeana.enrichment;

import eu.europeana.enrichment.api.external.EnrichmentReference;
import eu.europeana.enrichment.api.external.EnrichmentSearch;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.List;

public class DefaultRecordParser implements  RecordParser{

  @Override
  public List<EnrichmentSearch> parseSearchTerms(RDF rdf) {
    return null;
  }

  @Override
  public List<EnrichmentReference> parseReferences(RDF rdf) {
    return null;
  }
}
