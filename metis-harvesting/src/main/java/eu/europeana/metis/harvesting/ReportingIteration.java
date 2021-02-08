package eu.europeana.metis.harvesting;

public interface ReportingIteration<T> {

  boolean acceptAndContinue(T data);

}
