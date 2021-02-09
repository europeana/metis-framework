package eu.europeana.metis.harvesting;

public interface ReportingIteration<T> {

  enum IterationResult {TERMINATE, CONTINUE}

  IterationResult process(T data);

}
