package eu.europeana.enrichment.rest.client.report;

import java.util.HashSet;

public interface ProcessEnriched<T> {
  T getEnrichedRecord();
  HashSet<ErrorMessage> getReport();
}
