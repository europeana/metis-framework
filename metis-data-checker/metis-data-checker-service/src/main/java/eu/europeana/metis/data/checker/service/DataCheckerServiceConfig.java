package eu.europeana.metis.data.checker.service;

/**
 * Created by erikkonijnenburg on 06/07/2017.
 */
public interface DataCheckerServiceConfig {
  String getDataCheckerUrl();
  int getThreadCount();
}
