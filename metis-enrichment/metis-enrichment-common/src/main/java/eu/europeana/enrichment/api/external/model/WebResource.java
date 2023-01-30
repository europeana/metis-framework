package eu.europeana.enrichment.api.external.model;

/**
 * This interface supports implementation of common methods for conversion of different organization resource fields to different
 * organization objects.
 */
public interface WebResource {

  String getResourceUri();

  void setResourceUri(String resource);

}
