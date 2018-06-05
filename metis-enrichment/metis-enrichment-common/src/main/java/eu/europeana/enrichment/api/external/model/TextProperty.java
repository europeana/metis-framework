package eu.europeana.enrichment.api.external.model;

/**
 * This interface supports implementation of common methods for
 * conversion of different organization fields to different organization objects.
 * 
 * @author GrafR
 *
 */
public interface TextProperty {
  
  String getKey();
  
  String getValue();

}
