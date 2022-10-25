package eu.europeana.enrichment.rest.client.report;

import java.util.HashSet;

public class ProcessedEncrichment<T> implements ProcessEnriched<T> {
  private final T enrichedRecord;
  private HashSet<ErrorMessage> errorMessages;

  public ProcessedEncrichment(T enrichedRecord) {
    this.enrichedRecord = enrichedRecord;
  }

  public ProcessedEncrichment(T enrichedRecord, HashSet<ErrorMessage> errorMessages) {
    this.enrichedRecord = enrichedRecord;
    this.errorMessages = errorMessages;
  }

  public ProcessedEncrichment(ProcessedEncrichment<T> processedEncrichment) {
    this.enrichedRecord = processedEncrichment.enrichedRecord;
    this.errorMessages = processedEncrichment.errorMessages;
  }

  @Override
  public T getEnrichedRecord() {
    return enrichedRecord;
  }

  @Override
  public HashSet<ErrorMessage> getReport() {
    return errorMessages;
  }
}
